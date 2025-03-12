package geolocation.services

import cats.*
import cats.effect.*
import cats.syntax.all.*
import geolocation.domain.*
import geolocation.repositories.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

trait GeolocationService[F[_]] {
  def getCoords(query: AddressQuery): F[Either[String, GpsCoords]]
  def create(address: Address): F[Int]
}

object GeolocationService {
  def apply[F[_]: {Async, Meter, SelfAwareStructuredLogger, Tracer}](
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    private val logger   = summon[SelfAwareStructuredLogger[F]]
    private val meter    = summon[Meter[F]]
    private val tracer   = summon[Tracer[F]]
    private val counterF = meter.counter[Long]("get_coords_count").create

    override def getCoords(query: AddressQuery): F[Either[String, GpsCoords]] =
      tracer.span("getCoords", Attribute("query", s"$query")).surround {
        for {
          _       <- logger.info(s"Invoked getCoords($query)")
          counter <- counterF
          _       <- counter.add(1)
          maybeAddress <- tracer.span("getByAddress", Attribute("query", s"$query")).surround {
            repo.getByAddress(query)
          }
          result <- maybeAddress match {
            case Some(address) => address.coords.asRight.pure
            case None =>
              SelfAwareStructuredLogger[F].error(
                Map(
                  "function_name" -> "getCoords",
                  "function_args" -> s"$query",
                ),
              )(
                s"Invoked getCoords($query)",
              ) >> "No address found.".asLeft.pure
          }
          _ <- logger.info(s"Completed getCoords($query)")
        } yield result
      }

    override def create(address: Address): F[Int] =
      for {
        _ <- logger.info(s"Invoked create($address)")
        result <- repo.insert(address).handleErrorWith { error =>
          logger.error(
            Map(
              "function_name" -> "create",
              "function_args" -> s"$address",
            ),
          )(error.getMessage)
            >> 0.pure
        }
        _ <- logger.info(s"Completed create($address)")
      } yield result
  }
}
