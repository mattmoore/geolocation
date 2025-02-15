package geolocation.http.routes

import cats.effect.*
import cats.implicits.*
import fs2.RaiseThrowable
import geolocation.http.metrics.CustomMetricConfig
import org.http4s.Charset
import org.http4s.Request
import sttp.tapir.*
import sttp.tapir.model.ServerRequest

object Http4sTapirImplicits {
  implicit class EndpointImplicits[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, -R](
      val endpoint: Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R],
  ) extends AnyVal {
    def metric[F[_]: Async](config: CustomMetricConfig[F]): Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R] = {
      endpoint.attribute(
        AttributeKey[CustomMetricConfig[F]],
        config.copy(endpoint = Option(endpoint)),
      )
    }
  }

  implicit class ServerRequestImplicits(val serverRequest: ServerRequest) extends AnyVal {
    def bodyString[F[_]: Async]: F[Either[String, String]] =
      serverRequest.underlying match {
        case underlying: Request[?] =>
          for {
            underlying <- underlying.asInstanceOf[Request[F]].pure
            body <- underlying
              .bodyText(summon[RaiseThrowable[F]], underlying.charset.getOrElse(Charset.`UTF-8`))
              .compile
              .string
          } yield Right(body)
        case _ =>
          Async[F].pure(Left("Invalid underlying request type."))
      }
  }
}
