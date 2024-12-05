package geolocation.http.routes

import cats.effect.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import io.prometheus.metrics.core.metrics.Counter
import org.http4s.HttpRoutes
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.metrics.prometheus.PrometheusExportService
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.Metric
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

object MeteredRouter {
  def apply[F[_]: Async: Tracer](
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
  ): Resource[F, HttpRoutes[F]] = for {
    metricsService <- PrometheusExportService.build[F]
    metricsOps     <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "geolocation")
  } yield {
    val prometheusMetrics = PrometheusMetrics
      .default[F]("geolocation")

    val endpoints = prometheusMetrics.metricsEndpoint +: (
      HelloEndpoints(helloService) ++
        GeolocationEndpoints(geolocationService)
    )

    val prometheusMetricsWithCustom: PrometheusMetrics[F] =
      endpoints.foldLeft(prometheusMetrics) { (prometheusMetrics, serverEndpoint) =>
        serverEndpoint.attribute(AttributeKey[Metric[F, Counter]]) match
          case None         => prometheusMetrics
          case Some(metric) => prometheusMetrics.addCustom(metric)
      }

    val serverOptions = Http4sServerOptions
      .customiseInterceptors[F]
      .metricsInterceptor(prometheusMetricsWithCustom.metricsInterceptor())
      .options

    Http4sServerInterpreter[F](serverOptions).toRoutes(endpoints)
  }
}
