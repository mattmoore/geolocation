package geolocation.http.routes

import cats.effect.*
import geolocation.http.metrics.CustomMetricConfig
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import org.http4s.HttpRoutes
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

object Routes {
  def apply[F[_]: {Async, Meter, Tracer}](
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
      prometheusMetrics: PrometheusMetrics[F],
  ): HttpRoutes[F] = {
    val endpoints = prometheusMetrics.metricsEndpoint +: (
      HelloEndpoints(helloService) ++
        GeolocationEndpoints(geolocationService)
    )

    val prometheusMetricsWithCustom: PrometheusMetrics[F] =
      endpoints.foldLeft(prometheusMetrics) { (prometheusMetrics, serverEndpoint) =>
        serverEndpoint.attribute(AttributeKey[CustomMetricConfig[F]]) match
          case None => prometheusMetrics
          case Some(customMetricConfig) => {
            prometheusMetrics.registry.register(customMetricConfig.counter)
            prometheusMetrics.addCustom(customMetricConfig.tapirMetric)
          }
      }

    val serverOptions = Http4sServerOptions
      .customiseInterceptors[F]
      .metricsInterceptor(prometheusMetricsWithCustom.metricsInterceptor())
      .options

    Http4sServerInterpreter[F](serverOptions).toRoutes(endpoints)
  }
}

case class GeolocationApp[F[_]](endpoints: List[ServerEndpoint[Any, F]]) {}
