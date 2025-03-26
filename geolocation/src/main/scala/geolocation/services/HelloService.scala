package geolocation.services

import cats.effect.Async
import cats.syntax.all.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

trait HelloService[F[_]] {
  def hello(name: String): F[String]
}

object HelloService {
  def apply[F[_]: {Async, Meter, SelfAwareStructuredLogger, Tracer}](): HelloService[F] =
    new HelloService[F] {
      private val logger: SelfAwareStructuredLogger[F] = summon[SelfAwareStructuredLogger[F]]
      private val meter                                = summon[Meter[F]]
      private val counterF                             = meter.counter[Long]("hello_counter").create

      def hello(name: String): F[String] =
        for {
          counter <- counterF
          _       <- counter.add(1)
          _       <- logger.info(s"Invoked hello($name)")
          result  <- Async[F].pure(s"Hello, $name.")
        } yield result
    }
}
