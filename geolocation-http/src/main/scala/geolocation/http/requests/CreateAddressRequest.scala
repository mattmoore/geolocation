package geolocation.http.requests

import geolocation.domain.{Address, GpsCoords as DomainGpsCoords}

case class GpsCoords(
    lat: Double,
    lon: Double,
)

case class CreateAddressRequest(
    id: Int,
    street: String,
    city: String,
    state: String,
    coords: GpsCoords,
)

object CreateAddressRequest {
  extension (request: CreateAddressRequest)
    def toDomain: Address =
      Address(
        id = request.id,
        street = request.street,
        city = request.city,
        state = request.state,
        coords = DomainGpsCoords(
          lat = request.coords.lat,
          lon = request.coords.lon,
        ),
      )
}
