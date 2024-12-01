package geolocation.http.metrics

import cats.effect.*
import cats.implicits.*
import fs2.RaiseThrowable
import geolocation.http.requests.CoordsRequest
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.http4s.Charset
import org.http4s.Request
import sttp.monad.MonadError
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.metrics.EndpointMetric
import sttp.tapir.server.metrics.Metric

import scala.annotation.nowarn

object CustomMetrics {
  def extractRequestBodyString[F[_]: Async](serverRequest: ServerRequest): F[String] =
    serverRequest.underlying match {
      case underlying: Request[?] =>
        for {
          underlying <- underlying.asInstanceOf[Request[F]].pure
          body <- underlying
            .bodyText(summon[RaiseThrowable[F]], underlying.charset.getOrElse(Charset.`UTF-8`))
            .compile
            .string
        } yield body
      case _ =>
        Async[F].raiseError(Throwable("Invalid underlying request type."))
    }

  def geolocationByLocationAndStatusTotal[F[_]: Async]: Metric[F, Counter] = {
    val counter = Counter
      .builder()
      .name("geolocation_by_location_and_status_total")
      .help("Total Requests.")
      .labelNames("path", "method", "status", "state", "city")
      .register(PrometheusRegistry.defaultRegistry)

    Metric[F, Counter](counter, onRequest)
  }

  private def onRequest[F[_]: Async, M](
      req: ServerRequest,
      counter: Counter,
      @nowarn _me: MonadError[F],
  ): F[EndpointMetric[F]] = Async[F].delay {
    EndpointMetric().onResponseBody { (ep, res) =>
      val path   = ep.showPathTemplate()
      val method = req.method.method
      val status = s"${res.code.code}"

      (method, path) match {
        case ("POST", "/api/coords") => {
          for {
            bodyString <- extractRequestBodyString(req)
            parsed = decode[CoordsRequest](bodyString)
          } yield parsed match {
            case Left(error)    => counter.labelValues(path, method, status, "", "", "").inc()
            case Right(request) => counter.labelValues(path, method, status, request.state, request.city).inc()
          }
        }
        case _ => Async[F].unit
      }
    }
  }
}
