package geolocation.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import geolocation.domain.*
import geolocation.http.*
import geolocation.services.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.prometheus.client.Counter
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.*
import org.typelevel.otel4s.trace.Tracer

object GeolocationRoutes {
  def apply[F[_]: Async: Tracer](
      dsl: Http4sDsl[F],
      geolocationService: GeolocationService[F],
      byLocationAndStatus: Counter,
  ): HttpRoutes[F] = {
    import dsl.*

    HttpRoutes.of[F] {
      case req @ POST -> Root / "coords" =>
        {
          for {
            request <- req.as[requests.CoordsRequest]
            response <- geolocationService
              .getCoords(request.toDomain)
              .flatMap {
                case Right(coords) => Accepted(coords.asJson)
                case Left(error)   => NotFound(error)
              }
            _ <- Async[F].delay(
              byLocationAndStatus
                .labels(
                  s"${req.pathInfo}",
                  req.method.name,
                  s"${response.status.code}",
                  request.state,
                  request.city,
                )
                .inc(),
            )
          } yield response
        }.handleErrorWith(e => InternalServerError(e.getMessage))

      case req @ POST -> Root / "coords" / "new" =>
        {
          for {
            request  <- req.as[requests.CreateAddressRequest]
            response <- geolocationService.create(request.toDomain).flatMap(Accepted(_))
          } yield response
        }.handleErrorWith(e => InternalServerError(e.getMessage))

      case GET -> Root / "healthcheck" =>
        Ok()
    }
  }
}
