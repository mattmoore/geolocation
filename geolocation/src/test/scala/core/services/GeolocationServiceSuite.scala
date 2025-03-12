package geolocation.services

import cats.effect.*
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import geolocation.MockLogger
import geolocation.MockLogger.LogMessage
import geolocation.domain.*
import geolocation.repositories.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.extras.LogLevel
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer
import weaver.*

object GeolocationServiceSuite extends SimpleIOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  test("getCoords returns GPS coordinates for a given address") {
    for {
      logMessages <- AtomicCell[F].of(List.empty[LogMessage])
      given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
      given Meter[F]                     = Meter.noop
      given Tracer[F]                    = Tracer.noop

      addresses = List(
        Address(
          id = 1,
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          coords = GpsCoords(40.689247, -74.044502),
        ),
      )
      addressState <- F.ref(addresses)
      addressRepo: AddressRepository[F] = new AddressRepository[F] {
        override def getByAddress(query: AddressQuery): F[Option[Address]] =
          addressState.get.map { addresses =>
            addresses.find(_.street == query.street)
          }

        override def insert(address: Address): F[Int] =
          Async[F].raiseError(new Exception("Should not be called"))
      }
      geolocationService = GeolocationService[F](addressRepo)

      addressQuery = AddressQuery(
        street = addresses.head.street,
        city = addresses.head.city,
        state = addresses.head.state,
      )

      logMessagesBefore <- logMessages.get
      result            <- geolocationService.getCoords(addressQuery)
      logMessagesAfter  <- logMessages.get
    } yield {
      expect.all(
        result == Right(GpsCoords(40.689247, -74.044502)),
        logMessagesBefore.isEmpty,
        logMessagesAfter.size == 2,
        logMessagesAfter == List(
          LogMessage(
            LogLevel.Info,
            "Invoked getCoords(AddressQuery(20 W 34th St.,New York,NY))",
          ),
          LogMessage(
            LogLevel.Info,
            "Completed getCoords(AddressQuery(20 W 34th St.,New York,NY))",
          ),
        ),
      )
    }
  }

  test("create stores a new address") {
    for {
      logMessages <- AtomicCell[F].of(List.empty[LogMessage])
      given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
      given Meter[F]                     = Meter.noop
      given Tracer[F]                    = Tracer.noop

      addressState <- F.ref(List.empty[Address])
      addressRepo: AddressRepository[F] = new AddressRepository[F] {
        override def getByAddress(query: AddressQuery): F[Option[Address]] =
          addressState.get.map { addresses =>
            addresses.find(_.street == query.street)
          }

        override def insert(address: Address): F[Int] =
          addressState.update(_ :+ address) >> 1.pure
      }
      geolocationService = GeolocationService[F](addressRepo)

      newAddress = Address(
        id = 1,
        street = "20 W 34th St.",
        city = "New York",
        state = "NY",
        coords = GpsCoords(40.689247, -74.044502),
      )

      logMessagesBefore <- logMessages.get
      result            <- geolocationService.create(newAddress)
      logMessagesAfter  <- logMessages.get
    } yield {
      expect.all(
        result == 1,
        logMessagesBefore.isEmpty,
        logMessagesAfter.size == 2,
        logMessagesAfter == List(
          LogMessage(
            LogLevel.Info,
            "Invoked create(Address(1,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
          ),
          LogMessage(
            LogLevel.Info,
            "Completed create(Address(1,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
          ),
        ),
      )
    }
  }
}
