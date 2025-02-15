package geolocation.it.services

import cats.effect.*
import cats.effect.std.AtomicCell
import com.dimafeng.testcontainers.PostgreSQLContainer
import geolocation.MigrationRunner
import geolocation.MockLogger
import geolocation.MockLogger.*
import geolocation.domain.*
import geolocation.it.containers.PostgresContainer
import geolocation.repositories.AddressRepository
import geolocation.services.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.extras.LogLevel
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import skunk.Session
import weaver.*

object GeolocationServiceSuite extends IOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  final case class TestResource(
      config: Config,
      postgresContainer: PostgreSQLContainer,
  )
  override final type Res = TestResource
  override final val sharedResource: Resource[F, Res] =
    for {
      postgresContainer <- Resource.fromAutoCloseable(F.delay(PostgresContainer().start()))
      config = Config(
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
      migrationResult <- MigrationRunner(config.databaseConfig)
    } yield TestResource(
      config,
      postgresContainer,
    )

  private def pooledSessionR(config: Config): Resource[F, Resource[F, Session[F]]] =
    Session
      .pooled(
        host = config.databaseConfig.host,
        port = config.databaseConfig.port,
        user = config.databaseConfig.username,
        password = Some(config.databaseConfig.password),
        database = config.databaseConfig.database,
        max = 1,
      )

  test("getCoords returns GPS coordinates for a given address") { r =>
    pooledSessionR(r.config).use { session =>
      for {
        logMessages <- AtomicCell[F].of(List.empty[LogMessage])
        given Config                       = r.config
        given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
        addressRepo: AddressRepository[F]  = AddressRepository(r.config, session)
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
          logMessagesBefore.size == 0,
          logMessagesAfter.size == 1,
          logMessagesAfter == List(
            LogMessage(
              LogLevel.Info,
              "Invoked getCoords(AddressQuery(20 W 34th St.,New York,NY))",
            ),
          ),
        )
      }
    }
  }

  test("create stores a new address") { r =>
    pooledSessionR(r.config).use { session =>
      for {
        logMessages <- AtomicCell[F].of(List.empty[LogMessage])
        given Config                       = r.config
        given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
        addressRepo: AddressRepository[F]  = AddressRepository(r.config, session)
        geolocationService                 = GeolocationService[F](addressRepo)
        newAddress = Address(
          id = 3,
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          GpsCoords(40.689247, -74.044502),
        )

        logMessagesBefore <- logMessages.get
        result            <- geolocationService.create(newAddress).attempt
        logMessagesAfter  <- logMessages.get
      } yield {
        expect.all(
          result.isRight,
          logMessagesBefore.size == 0,
          logMessagesAfter.size == 1,
          logMessagesAfter == List(
            LogMessage(
              LogLevel.Info,
              "Invoked create(Address(3,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
            ),
          ),
        )
      }
    }
  }
}
