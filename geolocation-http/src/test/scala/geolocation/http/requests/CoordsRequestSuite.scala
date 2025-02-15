package geolocation.http.requests

import weaver.*
import geolocation.domain.*

object CoordsRequestSuite extends SimpleIOSuite {
  pureTest("toDomain") {
    val request = CoordsRequest(
      street = "123 Anywhere St.",
      city = "New York",
      state = "NY",
    )

    expect(
      request.toDomain == AddressQuery(
        street = "123 Anywhere St.",
        city = "New York",
        state = "NY",
      ),
    )
  }
}
