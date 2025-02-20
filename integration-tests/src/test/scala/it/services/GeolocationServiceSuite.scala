package geolocation.it.services

import cats.effect.*
import cats.effect.std.AtomicCell
import com.dimafeng.testcontainers.PostgreSQLContainer
import geolocation.{MigrationRunner, MockLogger, TransactorR}
import geolocation.MockLogger.*
import geolocation.domain.*
import geolocation.it.containers.PostgresContainer
import geolocation.repositories.AddressRepository
import geolocation.services.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.extras.LogLevel
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import weaver.*

object GeolocationServiceSuite extends IOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  final case class TestResource(
      config: AppConfig,
      postgresContainer: PostgreSQLContainer,
  )
  override final type Res = TestResource
  override final val sharedResource: Resource[F, Res] =
    for {
      postgresContainer <- Resource.fromAutoCloseable(F.delay(PostgresContainer().start()))
      config = AppConfig(
        port = 5432,
        databaseConfig = DatabaseConfig(
          host = postgresContainer.host,
          port = postgresContainer.firstMappedPort,
          username = postgresContainer.username,
          password = postgresContainer.password,
          database = postgresContainer.databaseName,
          maxConnections = 10,
          migrationsLocation = "filesystem:../geolocation/src/main/resources/db",
        ),
      )
      migrationRunner <- MigrationRunner()
      _               <- Resource.eval(migrationRunner.migrate(config.databaseConfig))
    } yield TestResource(
      config,
      postgresContainer,
    )

  test("getCoords returns GPS coordinates for a given address") { r =>
    TransactorR(r.config).use { xa =>
      for {
        logMessages <- AtomicCell[F].of(List.empty[LogMessage])
        given AppConfig                    = r.config
        given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
        addressRepo: AddressRepository[F]  = AddressRepository(r.config, xa)
        geolocationService                 = GeolocationService[F](addressRepo)
        query = AddressQuery(
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
        )

        logMessagesBefore <- logMessages.get
        result            <- geolocationService.getCoords(query)
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
  }

  test("create stores a new address") { r =>
    TransactorR(r.config).use { xa =>
      for {
        logMessages <- AtomicCell[F].of(List.empty[LogMessage])
        given AppConfig                    = r.config
        given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
        addressRepo: AddressRepository[F]  = AddressRepository(r.config, xa)
        geolocationService                 = GeolocationService[F](addressRepo)
        newAddress = Address(
          id = 3,
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          GpsCoords(40.689247, -74.044502),
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
              "Invoked create(Address(3,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
            ),
            LogMessage(
              LogLevel.Info,
              "Completed create(Address(3,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
            ),
          ),
        )
      }
    }
  }
}
