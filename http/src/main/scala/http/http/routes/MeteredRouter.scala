package geolocation.http.routes

import cats.effect.*
import cats.syntax.all.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
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
  ): Resource[F, HttpRoutes[F]] = for {
    metricsService <- PrometheusExportService.build[F]
    metrics        <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "geolocation")
    dsl = Http4sDsl[F]
    routes = GeolocationRoutes(dsl, geolocationService)
      <+> HelloRoutes(dsl, helloService)
    router = Router[F](
      "/api" -> Metrics[F](metrics)(routes),
      "/"    -> metricsService.routes,
    )
  } yield router
}
