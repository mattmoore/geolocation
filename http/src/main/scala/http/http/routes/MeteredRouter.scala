package geolocation.http.routes

import cats.effect.*
import geolocation.http.metrics.CustomMetrics.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import org.http4s.HttpRoutes
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.metrics.prometheus.PrometheusExportService
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
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
      .addCustom(geolocationByLocationAndStatusTotal)

    val serverOptions = Http4sServerOptions
      .customiseInterceptors[F]
      .metricsInterceptor(prometheusMetrics.metricsInterceptor())
      .options

    val endpoints = prometheusMetrics.metricsEndpoint +: (
      HelloEndpoints(helloService) ++
        GeolocationEndpoints(geolocationService)
    )

    Http4sServerInterpreter[F](serverOptions).toRoutes(endpoints)
  }
}
