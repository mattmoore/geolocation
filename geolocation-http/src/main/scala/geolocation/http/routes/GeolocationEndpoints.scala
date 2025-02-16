package geolocation.http.routes

import cats.effect.*
import cats.syntax.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import geolocation.domain.Address
import geolocation.domain.GpsCoords
import Http4sTapirImplicits.*
import geolocation.http.metrics.NewAddressMetric
import geolocation.http.requests.{CoordsRequest, CreateAddressRequest}
import geolocation.services.GeolocationService
import geolocation.http.metrics.GeolocationByLocationAndStatusTotal
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.ServerEndpoint

object GeolocationEndpoints {
  given JsonValueCodec[CoordsRequest]        = JsonCodecMaker.make[CoordsRequest]
  given JsonValueCodec[GpsCoords]            = JsonCodecMaker.make[GpsCoords]
  given JsonValueCodec[CreateAddressRequest] = JsonCodecMaker.make[CreateAddressRequest]
  given JsonValueCodec[Address]              = JsonCodecMaker.make[Address]

  def apply[F[_]: Async](geolocationService: GeolocationService[F]): List[ServerEndpoint[Any, F]] = List(
    endpoint.post
      .name("getCoords")
      .metric(GeolocationByLocationAndStatusTotal[F])
      .in("api" / "coords")
      .in(jsonBody[CoordsRequest])
      .out(jsonBody[GpsCoords])
      .serverLogicOption[F] { request =>
        geolocationService
          .getCoords(request.toDomain)
          .map(_.toOption)
      },
    endpoint.post
      .name("newCoords")
      .metric(NewAddressMetric[F])
      .in("api" / "coords" / "new")
      .in(jsonBody[CreateAddressRequest])
      .out(jsonBody[CreateAddressRequest])
      .errorOut(plainBody[String])
      .serverLogic[F] { request =>
        geolocationService
          .create(request.toDomain)
          .attempt
          .map {
            case Left(e)  => e.getMessage.asLeft
            case Right(_) => request.asRight
          }
      },
  )
}
