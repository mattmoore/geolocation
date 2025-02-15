package geolocation.domain

case class AddressQuery(
    street: String,
    city: String,
    state: String,
)

case class Address(
    id: Int,
    street: String,
    city: String,
    state: String,
    coords: GpsCoords,
)
