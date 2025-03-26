package geolocation.http

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import geolocation.*
import geolocation.domain.*
import geolocation.http.routes.GeolocationEndpoints
import geolocation.http.routes.HelloEndpoints
import geolocation.repositories.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import io.opentelemetry.api.GlobalOpenTelemetry
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import sttp.tapir.server.tracing.otel4s.Otel4sTracing
import doobie.util.log.LogHandler
import doobie.otel4s.*

object Resources {
  def make[F[_]: {Async, LiftIO, Console, Network}]: Resource[F, Server] =
    for {
      appConfig <- AppConfig.load[F]
      logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F](name = LoggerName(appConfig.serviceName))
      given SelfAwareStructuredLogger[F]   = logger
      otel4s <- Resource.eval {
        Async[F].delay(GlobalOpenTelemetry.get)
          >>= OtelJava.fromJOpenTelemetry[F]
      }
      given Meter[F] <- Resource.eval(otel4s.meterProvider.get(appConfig.serviceName))
      tracer         <- Resource.eval(otel4s.tracerProvider.get(appConfig.serviceName))
      given Tracer[F] = tracer
      migrationRunner <- MigrationRunner()
      _               <- Resource.eval(migrationRunner.migrate(appConfig.databaseConfig))
      transactor      <- TransactorR(appConfig).map(transactor => TracedTransactor[F](transactor, LogHandler.noop))
      addressRepo: AddressRepository[F]         = AddressRepository(appConfig, transactor)
      helloService: HelloService[F]             = HelloService()
      geolocationService: GeolocationService[F] = GeolocationService(addressRepo)
      endpoints =
        HelloEndpoints(helloService) ++
          GeolocationEndpoints(geolocationService)
      metrics = OpenTelemetryMetrics.default[F](
        otel4s.underlying.getMeterProvider().get(appConfig.serviceName),
      )
      serverOptions = Http4sServerOptions
        .customiseInterceptors[F]
        .metricsInterceptor(metrics.metricsInterceptor())
        .prependInterceptor(Otel4sTracing(tracer))
        .options
      routes = Http4sServerInterpreter[F](serverOptions).toRoutes(endpoints)
      httpServer: Server <- ServerResource.make(appConfig, routes)
    } yield httpServer
}
