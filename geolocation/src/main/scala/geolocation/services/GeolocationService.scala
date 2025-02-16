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
  def create(address: Address): F[Int]
}

object GeolocationService {
  def apply[F[_]: {Async, SelfAwareStructuredLogger, Tracer}](
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    private val logger = summon[SelfAwareStructuredLogger[F]]
    private val tracer = summon[Tracer[F]]

    override def getCoords(query: AddressQuery): F[Either[String, GpsCoords]] =
      (for {
        _ <- logger.info(
          Map(
            "function_name" -> "getCoords",
            "function_args" -> s"$query",
          ),
        )(s"Invoked getCoords($query)")
        maybeAddress <- tracer.span("getCoords").surround(repo.getByAddress(query))
        result <- maybeAddress match {
          case Some(address) => address.coords.asRight.pure
          case None =>
            SelfAwareStructuredLogger[F].error(
              Map(
                "function_name" -> "getCoords",
                "function_args" -> s"$query",
              ),
            )(s"Invoked getCoords($query)")
              >> "No address found.".asLeft.pure
        }
        _ <- logger.info(
          Map(
            "function_name" -> "getCoords",
            "function_args" -> s"$query",
          ),
        )(s"Completed getCoords($query)")
      } yield result)

    override def create(address: Address): F[Int] =
      (for {
        _ <- logger.info(
          Map(
            "function_name" -> "create",
            "function_args" -> s"$address",
          ),
        )(s"Invoked create($address)")
        result <- tracer.span("create").surround(repo.insert(address))
        _ <- logger.info(
          Map(
            "function_name" -> "create",
            "function_args" -> s"$address",
          ),
        )(s"Completed create($address)")
      } yield result).handleErrorWith { error =>
        logger.error(
          Map(
            "function_name" -> "create",
            "function_args" -> s"$address",
          ),
        )(error.getMessage)
          >> 0.pure
      }
  }
}
