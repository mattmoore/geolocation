package geolocation.repositories

import cats.effect.*
import cats.syntax.all.*
import geolocation.domain.Address
import geolocation.domain.AddressQuery
import geolocation.domain.GpsCoords
import geolocation.repositories.Stubs.*
import geolocation.repositories.codecs.*
import org.typelevel.otel4s.trace.Tracer
import weaver.*

object AddressRepositorySuite extends SimpleIOSuite {
  type F[A] = IO[A]

  test("getByAddress returns an address") {
    given Tracer[F] = Tracer.Implicits.noop

    val config      = emptyConfig
    val session     = new StubSession[F]
    val sessionR    = Resource.eval(session.pure)
    val addressRepo = AddressRepository(config, sessionR)

    val query = AddressQuery(
      street = "123 Anywhere St.",
      city = "New York",
      state = "NY",
    )

    for {
      result: Option[Address] <- addressRepo.getByAddress(query)
    } yield expect.all(
      result == AddressRow(
        id = 1,
        street = "123 Anywhere St.",
        city = "New York",
        state = "NY",
        lat = 1,
        lon = 1,
      ).some,
    )
  }

  test("insert saves an address") {
    given Tracer[F] = Tracer.Implicits.noop

    val config      = emptyConfig
    val session     = new StubSession[F]
    val sessionR    = Resource.eval(session.pure)
    val addressRepo = AddressRepository(config, sessionR)

    val address = Address(
      id = 1,
      street = "123 Anywhere St.",
      city = "New York",
      state = "NY",
      coords = GpsCoords(1, 1),
    )

    for {
      result <- addressRepo.insert(address).attempt
    } yield expect.all(
      result.isRight,
    )
  }

  test("insert fails") {
    given Tracer[F] = Tracer.Implicits.noop

    val config      = emptyConfig
    val session     = new StubFailingSession[F]
    val sessionR    = Resource.eval(session.pure)
    val addressRepo = AddressRepository(config, sessionR)

    val address = Address(
      id = 1,
      street = "123 Anywhere St.",
      city = "New York",
      state = "NY",
      coords = GpsCoords(1, 1),
    )

    for {
      result <- addressRepo.insert(address).attempt
    } yield expect.all(
      result.isLeft,
    )
  }
}
