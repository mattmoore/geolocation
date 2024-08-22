package geolocation.http

import cats.*
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import geolocation.domain.*
import geolocation.http.routes.*
import geolocation.services.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.Tracer

object ServerResource {
  def make[F[_]: Async: Network: Tracer](
      config: Config,
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
  ): Resource[F, Server] = {
    val dsl = Http4sDsl[F]

    val routes: HttpRoutes[F] = List(
      HelloRoutes(dsl, helloService),
      GeolocationRoutes(dsl, geolocationService),
    ).foldK

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
}

extension [F[_]: Async: Tracer](service: HttpApp[F])
  def traced: HttpApp[F] = {
    Kleisli { (req: Request[F]) =>
      Tracer[F]
        .rootSpan(
          "request",
          Attribute("http.method", req.method.name),
          Attribute("http.url", req.uri.renderString),
        )
        .use { span =>
          for {
            response <- service(req)
            _        <- span.addAttribute(Attribute("http.status-code", response.status.code.toLong))
            _ <- {
              if (response.status.isSuccess) span.setStatus(StatusCode.Ok) else span.setStatus(StatusCode.Error)
            }
          } yield {
            val traceIdHeader = Header.Raw(CIString("traceId"), span.context.traceIdHex)
            response.putHeaders(traceIdHeader)
          }
        }
    }
  }
