package geolocation.http.routes

import cats.effect.*
import cats.syntax.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import geolocation.domain.Address
import geolocation.domain.GpsCoords
import geolocation.http.metrics.CustomMetrics.*
import geolocation.http.requests.CoordsRequest
import geolocation.http.requests.CreateAddressRequest
import geolocation.services.GeolocationService
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
      .in("api" / "coords")
      .in(jsonBody[CoordsRequest])
      .out(jsonBody[GpsCoords])
      .serverLogicOption[F] { request =>
        geolocationService
          .getCoords(request.toDomain)
          .map(_.toOption)
      },
    endpoint.post
      .in("api" / "coords" / "new")
      .in(jsonBody[CreateAddressRequest])
      .out(jsonBody[Address])
      .serverLogicError[F] { request =>
        geolocationService
          .create(request.toDomain)
      },
  )
}
