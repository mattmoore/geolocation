package geolocation.http

import cats.*
import cats.effect.*
import cats.effect.std.Console
import fs2.io.net.Network
import geolocation.*
import geolocation.domain.*
import geolocation.repositories.*
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import geolocation.http.metrics.CustomMetricConfig
import geolocation.http.routes.{GeolocationEndpoints, HelloEndpoints}
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.AttributeKey
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Resources {
  def make[F[_]: {Async, LiftIO, Console, Network}]: Resource[F, Server] =
    for {
      config <- AppConfig.load[F]
      serviceName = "geolocation"
      otel            <- OtelJava.autoConfigured[F]()
      given Meter[F]  <- Resource.eval(otel.meterProvider.get(serviceName))
      given Tracer[F] <- Resource.eval(otel.tracerProvider.get(serviceName))
      given SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F](name = LoggerName("geolocation"))
      prometheusMetrics <- Resource.eval(Async[F].delay(PrometheusMetrics.default[F]("geolocation")))
      migrationRunner   <- MigrationRunner()
      _                 <- Resource.eval(migrationRunner.migrate(config.databaseConfig))
      xa                <- TransactorR(config)
      addressRepo: AddressRepository[F]         = AddressRepository(config, xa)
      helloService: HelloService[F]             = HelloService()
      geolocationService: GeolocationService[F] = GeolocationService(addressRepo)
      endpoints                                 = HelloEndpoints(helloService) ++ GeolocationEndpoints(geolocationService)
      metricsInterceptor = endpoints
        .foldLeft(prometheusMetrics) { (prometheusMetrics, serverEndpoint) =>
          serverEndpoint.attribute(AttributeKey[CustomMetricConfig[F]]) match
            case None => prometheusMetrics
            case Some(customMetricConfig) => {
              prometheusMetrics.registry.register(customMetricConfig.counter)
              prometheusMetrics.addCustom(customMetricConfig.tapirMetric)
            }
        }
        .metricsInterceptor()
      serverOptions = Http4sServerOptions
        .customiseInterceptors[F]
        .metricsInterceptor(metricsInterceptor)
        .options
      serverInterpreter = Http4sServerInterpreter[F](serverOptions).toRoutes(
        prometheusMetrics.metricsEndpoint +: endpoints,
      )
      httpServer: Server <- ServerResource.make(config, serverInterpreter)
    } yield httpServer
}
