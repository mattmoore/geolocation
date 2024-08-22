package geolocation

import cats.*
import cats.effect.*
import cats.effect.std.Console
import fs2.io.net.Network
import geolocation.http.ServerResource
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer
import skunk.Session

import domain.*
import repositories.*

object Resources {
  def make[F[_]: Async: LiftIO: Console: Network]: Resource[F, Server] =
    for {
      config <- Resource.eval(Config.load[F])
      serviceName = "geolocation"
      otel            <- OtelJava.autoConfigured[F]()
      given Meter[F]  <- Resource.eval(otel.meterProvider.get(serviceName))
      given Tracer[F] <- Resource.eval(otel.tracerProvider.get(serviceName))
      given SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F](
        name = LoggerName("geolocation"),
      )
      _ <- Migrations.migrate(config.databaseConfig)
      session <- Session.pooled(
        host = config.databaseConfig.host,
        port = config.databaseConfig.port,
        user = config.databaseConfig.username,
        password = Some(config.databaseConfig.password),
        database = config.databaseConfig.database,
        max = config.databaseConfig.maxConnections,
      )
      addressRepo: AddressRepository[F]         = AddressRepository(config, session)
      helloService: HelloService[F]             = HelloService.apply
      geolocationService: GeolocationService[F] = GeolocationService(addressRepo)
      httpServer: Server <- ServerResource.make[F](config, helloService, geolocationService)
    } yield httpServer
}
