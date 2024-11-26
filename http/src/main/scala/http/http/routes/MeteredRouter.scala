package geolocation.http.routes

import cats.effect.*
import cats.syntax.all.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import io.prometheus.client.Counter
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.server.Router
import org.http4s.server.middleware.Metrics
import org.typelevel.otel4s.trace.Tracer

object MeteredRouter {
  def apply[F[_]: Async: Tracer](
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
  ): Resource[F, HttpRoutes[F]] =
    for {
      metricsService <- PrometheusExportService.build[F]
      metricsOps     <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "geolocation")
      byLocationAndStatus = Counter
        .build()
        .name("geolocation_by_location_and_status_total")
        .help("Total Requests.")
        .labelNames("path", "method", "status", "state", "city")
        .register(metricsService.collectorRegistry)
      dsl = Http4sDsl[F]
      routes = GeolocationRoutes(dsl, geolocationService, byLocationAndStatus)
        <+> HelloRoutes(dsl, helloService)
      router = Router[F](
        "/api" -> Metrics[F](metricsOps)(routes),
        "/"    -> metricsService.routes,
      )
    } yield router
}
