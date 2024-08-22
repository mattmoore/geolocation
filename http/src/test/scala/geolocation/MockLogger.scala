package geolocation

import cats.effect.*
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.extras.LogLevel

object MockLogger {
  case class LogMessage(level: LogLevel, msg: String)

  def apply[F[_]](state: AtomicCell[F, List[LogMessage]]): SelfAwareStructuredLogger[F] =
    new SelfAwareStructuredLogger[F] {
      override def isErrorEnabled: F[Boolean] = ???
      override def isWarnEnabled: F[Boolean]  = ???
      override def isInfoEnabled: F[Boolean]  = ???
      override def isDebugEnabled: F[Boolean] = ???
      override def isTraceEnabled: F[Boolean] = ???

      override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Error, msg) +: messages)

      override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Error, msg) +: messages)

      override def error(t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Error, msg) +: messages)

      override def error(msg: => String): F[Unit] =
        error(Throwable(msg))(msg)

      override def warn(t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Warn, msg) +: messages)

      override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Warn, msg) +: messages)

      override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Warn, msg) +: messages)

      override def warn(msg: => String): F[Unit] =
        warn(Throwable(msg))(msg)

      override def info(t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Info, msg) +: messages)

      override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Info, msg) +: messages)

      override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Info, msg) +: messages)

      override def info(msg: => String): F[Unit] =
        info(Throwable(msg))(msg)

      override def debug(t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Debug, msg) +: messages)

      override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Debug, msg) +: messages)

      override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Debug, msg) +: messages)

      override def debug(msg: => String): F[Unit] =
        debug(Throwable(msg))(msg)

      override def trace(t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Trace, msg) +: messages)

      override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Trace, msg) +: messages)

      override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        state.update(messages => LogMessage(LogLevel.Trace, msg) +: messages)

      override def trace(msg: => String): F[Unit] =
        trace(Throwable(msg))(msg)
    }
}
