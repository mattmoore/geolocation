package geolocation.repositories.codecs

import geolocation.domain.Address

case class AddressRow(
    id: Int,
    street: String,
    city: String,
    state: String,
    lat: Double,
    lon: Double,
)

object AddressRow {
  def fromDomain(address: Address): AddressRow = AddressRow(
    id = address.id,
    street = address.street,
    city = address.city,
    state = address.state,
    lat = address.coords.lat,
    lon = address.coords.lon,
  )
}
