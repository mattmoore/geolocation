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

object ServerResource {
  def make[F[_]: {Async, Network}](
      config: AppConfig,
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
      .withHttpApp(routes.orNotFound)
      .build
}
