package geolocation.http.requests

import weaver.*

object CreateAddressRequestSuite extends SimpleIOSuite {
  pureTest("toDomain") {
    val request = CreateAddressRequest(
      id = 1,
      street = "123 Anywhere St.",
      city = "New York",
      state = "NY",
      coords = geolocation.http.requests.GpsCoords(1, 1),
    )

    expect(
      request.toDomain == geolocation.domain.Address(
        id = 1,
        street = "123 Anywhere St.",
        city = "New York",
        state = "NY",
        coords = geolocation.domain.GpsCoords(1, 1),
      ),
    )
  }
}
