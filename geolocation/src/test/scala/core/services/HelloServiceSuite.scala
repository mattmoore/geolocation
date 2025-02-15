package geolocation.services

import cats.effect.*
import cats.effect.std.AtomicCell
import geolocation.MockLogger
import geolocation.MockLogger.LogMessage
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.extras.LogLevel
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import weaver.*

object HelloServiceSuite extends SimpleIOSuite {
  private type F[A] = IO[A]

  test("hello returns a greeting with the name interpolated") {
    for {
      logMessages <- AtomicCell[F].of(List.empty[LogMessage])
      given SelfAwareStructuredLogger[F] = MockLogger[F](logMessages)
      helloService: HelloService[F]      = HelloService.apply

      logMessagesBefore <- logMessages.get
      result            <- helloService.hello("Matt")
      logMessagesAfter  <- logMessages.get
    } yield {
      expect.all(
        result == "Hello, Matt.",
        logMessagesBefore.size == 0,
        logMessagesAfter.size == 1,
        logMessagesAfter == List(LogMessage(LogLevel.Info, "Invoked hello(Matt)")),
        logMessagesAfter.head.level == LogLevel.Info,
        logMessagesAfter.head.msg == "Invoked hello(Matt)",
      )
    }
  }
}
