package geolocation.http

import cats.*
import cats.effect.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import geolocation.domain.*
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.otel4s.trace.Tracer

import Tracing.traced

object ServerResource {
  def make[F[_]: {Async, Network, Tracer}](
      config: Config,
      routes: HttpRoutes[F],
  ): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(
        Port
          .fromInt(config.port)
          .getOrElse(port"8080"),
      )
      .withHttpApp(routes.orNotFound.traced)
      .build
}
