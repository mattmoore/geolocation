package geolocation.http.requests

import geolocation.domain.AddressQuery

case class CoordsRequest(
    street: String,
    city: String,
    state: String,
)

object CoordsRequest {
  extension (request: CoordsRequest)
    def toDomain: AddressQuery =
      AddressQuery(
        street = request.street,
        city = request.city,
        state = request.state,
      )
}
