package geolocation.http

import cats.*
import cats.data.Kleisli
import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.Tracer

object Tracing {
  extension [F[_]: {Async, Tracer}](service: HttpApp[F])
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
}
