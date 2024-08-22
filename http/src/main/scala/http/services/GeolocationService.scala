package geolocation.services

import cats.*
import cats.effect.*
import cats.syntax.all.*
import geolocation.domain.*
import geolocation.repositories.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.otel4s.trace.Tracer

trait GeolocationService[F[_]] {
  def getCoords(query: AddressQuery): F[Either[String, GpsCoords]]
  def create(address: Address): F[Unit]
}

object GeolocationService {
  def apply[F[_]: Async: SelfAwareStructuredLogger: Tracer](
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    val logger = summon[SelfAwareStructuredLogger[F]]
    val tracer = summon[Tracer[F]]

    override def getCoords(query: AddressQuery): F[Either[String, GpsCoords]] =
      for {
        _ <- logger.info(
          Map("function_name" -> "getCoords", "function_args" -> s"$query"),
        )(
          s"Invoked getCoords($query)",
        )
        result <- tracer.span("getCoords").surround(repo.getByAddress(query)).flatMap {
          case Some(address) =>
            address.coords.asRight.pure
          case None =>
            SelfAwareStructuredLogger[F].error(
              Map("function_name" -> "getCoords", "function_args" -> s"$query"),
            )(
              s"Invoked getCoords($query)",
            ) *>
              "No address found.".asLeft.pure
        }
      } yield result

    override def create(address: Address): F[Unit] =
      for {
        _ <- logger.info(
          Map("function_name" -> "create", "function_args" -> s"$address"),
        )(
          s"Invoked create($address)",
        )
        result <- tracer
          .span("create")
          .surround(repo.insert(address))
          .handleErrorWith { error =>
            logger.error(
              Map("function_name" -> "create", "function_args" -> s"$address"),
            )(
              error.getMessage,
            )
          }
      } yield result
  }
}
