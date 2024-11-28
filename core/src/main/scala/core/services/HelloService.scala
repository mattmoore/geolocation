package geolocation.services

import cats.effect.kernel.Async
import cats.syntax.all.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.otel4s.trace.Tracer

trait HelloService[F[_]] {
  def hello(name: String): F[String]
}

object HelloService {
  def apply[F[_]: Async: SelfAwareStructuredLogger: Tracer]: HelloService[F] = new HelloService[F] {
    def hello(name: String): F[String] =
      for {
        _      <- SelfAwareStructuredLogger[F].info(s"Invoked hello($name)")
        result <- Async[F].pure(s"Hello, $name.")
      } yield result
  }
}
