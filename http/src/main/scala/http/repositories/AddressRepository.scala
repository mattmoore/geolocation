package geolocation.repositories

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import geolocation.domain.*
import geolocation.repositories.codecs.*
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import org.typelevel.otel4s.trace.Tracer

trait AddressRepository[F[_]] {
  def getByAddress(addressQuery: AddressQuery): F[Option[Address]]

  def insert(address: Address): F[Unit]
}

object AddressRepository {
  val addressCodec: Codec[Address] =
    (int4, varchar, varchar, varchar, float8, float8).tupled
      .imap { case (id, street, city, state, lat, lon) =>
        Address(id, street, city, state, GpsCoords(lat, lon))
      } { case Address(id, street, city, state, coords) =>
        (id, street, city, state, coords.lon, coords.lat)
      }

  val addressRowCodec: Codec[AddressRow] =
    (int4, varchar, varchar, varchar, float8, float8).tupled
      .imap { case (id, street, city, state, lon, lat) =>
        AddressRow(id, street, city, state, lon, lat)
      } { case AddressRow(id, street, city, state, lat, lon) =>
        (id, street, city, state, lon, lat)
      }

  def apply[F[_]: Async: Network: Console: Tracer](
      config: Config,
      sessionR: Resource[F, Session[F]],
  ): AddressRepository[F] = new AddressRepository[F] {
    override def getByAddress(query: AddressQuery): F[Option[Address]] = {
      val getByAddressQuery: Query[(String, String), Address] =
        sql"""|SELECT
              |  id,
              |  street,
              |  city,
              |  state,
              |  ST_Y(coords) AS lat,
              |  ST_X(coords) AS lon
              |FROM addresses
              |WHERE city LIKE $varchar
              |  AND state LIKE $varchar
              |""".stripMargin.query(addressCodec).to[Address]
      sessionR.use { session =>
        for {
          statement <- session.prepare(getByAddressQuery)
          result    <- statement.stream((query.city, query.state), 16).compile.toList
        } yield result.headOption
      }
    }

    override def insert(address: Address): F[Unit] = {
      val insertCommand: Command[AddressRow] =
        sql"""|INSERT INTO addresses(
              |  id,
              |  street,
              |  city,
              |  state,
              |  coords
              |) VALUES (
              |  $int4,
              |  $varchar,
              |  $varchar,
              |  $varchar,
              |  ST_SetSRID(ST_MakePoint($float8, $float8), 4326)
              |)
              |""".stripMargin.command.to[AddressRow]
      sessionR.use { session =>
        for {
          statement <- session.prepare(insertCommand)
          _ <- statement
            .execute(AddressRow.fromDomain(address))
            .flatMap(_ => Right(()).pure)
        } yield ()
      }
    }
  }
}
