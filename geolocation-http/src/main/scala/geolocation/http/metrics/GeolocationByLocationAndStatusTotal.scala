package geolocation.http.metrics

import cats.effect.*
import cats.implicits.*
import geolocation.http.requests.CoordsRequest
import geolocation.http.routes.Http4sTapirImplicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.prometheus.metrics.core.metrics.Counter
import sttp.tapir.Endpoint
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.model.ServerResponse

object GeolocationByLocationAndStatusTotal {
  case class UserIndividual(
      userId: Int,
      authType: String,
      confirmed: Boolean,
  )

  def apply[F[_]: Async]: CustomMetricConfig[F] = CustomMetricConfig(
    counter = Counter
      .builder()
      .name("geolocation_by_location_and_status_total")
      .help("Total Requests.")
      .labelNames("path", "method", "userId", "status", "state", "city")
      .build(),
    onResponseBody = { (ep: Endpoint[?, ?, ?, ?, ?], req: ServerRequest, res: ServerResponse[?], counter: Counter) =>
      for {
        bodyString <- req.bodyString
        decoded = decode[CoordsRequest](bodyString.getOrElse(""))
      } yield decoded match {
        case Left(_) => ()
        case Right(coordsRequest) => {
          val path   = ep.showPathTemplate()
          val method = req.method.method
          val status = s"${res.code.code}"
          val userIndividualHeaderValue =
            req
              .header("X-Auth-Token-Data-User-Individual")
              .getOrElse("")
          val userId = decode[UserIndividual](userIndividualHeaderValue).map(_.userId.toString).getOrElse("")
          counter
            .labelValues(
              path,
              method,
              userId,
              status,
              coordsRequest.state,
              coordsRequest.city,
            )
            .inc()
        }
      }
    },
  )
}
