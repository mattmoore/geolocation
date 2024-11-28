package geolocation.http.routes

import cats.effect.*
import cats.syntax.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import geolocation.domain.GpsCoords
import geolocation.http.requests.CoordsRequest
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.http4s.Charset
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.dsl.Http4sDsl
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.metrics.prometheus.PrometheusExportService
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.EndpointMetric
import sttp.tapir.server.metrics.Metric
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

object MeteredRouter {
  def apply[F[_]: Async: Tracer](
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
  ): Resource[F, HttpRoutes[F]] = {

    for {
      metricsService <- PrometheusExportService.build[F]
      metricsOps     <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "geolocation")
      dsl                                 = Http4sDsl[F]
      given JsonValueCodec[CoordsRequest] = JsonCodecMaker.make[CoordsRequest]
      given JsonValueCodec[GpsCoords]     = JsonCodecMaker.make[GpsCoords]
      helloServiceEndpoint = endpoint.post
        .in("api" / "hello")
        .in(query[String]("name"))
        .out(stringBody)
        .serverLogicSuccess[F] { name =>
          helloService.hello(name)
        }
      geolocationEndpoint = endpoint.post
        .in("api" / "coords")
        .in(jsonBody[CoordsRequest])
        .out(jsonBody[GpsCoords])
        .serverLogicOption[F] { request =>
          geolocationService
            .getCoords(request.toDomain)
            .map(_.toOption)
        }
      byLocationAndStatus = Counter
        .builder()
        .name("geolocation_by_location_and_status_total")
        .help("Total Requests.")
        .labelNames("path", "method", "status", "state", "city")
        .register(PrometheusRegistry.defaultRegistry)
      customMetrics = Metric[F, Counter](
        byLocationAndStatus,
        onRequest = { (req, counter, _) =>
          Async[F].delay(
            EndpointMetric()
              .onResponseBody { (ep, res) =>
                val path   = ep.showPathTemplate()
                val method = req.method.method
                val status = res.code.toString()
                (req.method.method, path) match {
                  case ("POST", p) if p.startsWith("/api/coords") => {
                    val underlying = req.underlying.asInstanceOf[Request[F]]
                    val bodyStringF = underlying
                      .bodyText(implicitly, underlying.charset.getOrElse(Charset.`UTF-8`))
                      .compile
                      .string
                    for {
                      bodyString <- bodyStringF
                      parsed = decode[CoordsRequest](bodyString)
                    } yield {
                      parsed match {
                        case Left(error)    => counter.labelValues(path, method, status, "", "", "").inc()
                        case Right(request) => counter.labelValues(path, method, status, request.state, request.city).inc()
                      }
                    }
                  }
                  case _ => Async[F].unit
                }
              },
          )
        },
      )
      prometheusMetrics = PrometheusMetrics
        .default[F]("geolocation")
        .addCustom(customMetrics)
      serverOptions: Http4sServerOptions[F] = Http4sServerOptions
        .customiseInterceptors[F]
        .metricsInterceptor(prometheusMetrics.metricsInterceptor())
        .options
      endpoints = List(
        helloServiceEndpoint,
        geolocationEndpoint,
        prometheusMetrics.metricsEndpoint,
      )
      routes2: HttpRoutes[F] = Http4sServerInterpreter[F](serverOptions)
        .toRoutes(endpoints)
    } yield routes2
  }
}
