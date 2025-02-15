package geolocation.repositories

import cats.*
import cats.effect.*
import cats.effect.std.Console
import doobie.*
import doobie.implicits.*
import geolocation.domain.*

trait AddressRepository[F[_]] {
  def getByAddress(addressQuery: AddressQuery): F[Option[Address]]

  def insert(address: Address): F[Int]
}

object AddressRepository {
  def apply[F[_]: {Async, Console}](
      config: Config,
      xa: Transactor[F],
  ): AddressRepository[F] = new AddressRepository[F] {
    override def getByAddress(query: AddressQuery): F[Option[Address]] =
      Fragments.getByAddress(query).query[Address].option.transact(xa)

    override def insert(address: Address): F[Int] =
      Fragments.insert(address).update.run.transact(xa)
  }

  object Fragments {
    val getByAddress = { (query: AddressQuery) =>
      sql"""|SELECT
            |  id,
            |  street,
            |  city,
            |  state,
            |  ST_Y(coords) AS lat,
            |  ST_X(coords) AS lon
            |FROM addresses
            |WHERE city LIKE ${query.city}
            |  AND state LIKE ${query.state}
            |LIMIT 1
            |""".stripMargin
    }
    val insert = { (address: Address) =>
      sql"""|INSERT INTO addresses(
            |  id,
            |  street,
            |  city,
            |  state,
            |  coords
            |) VALUES (
            |  ${address.id},
            |  ${address.street},
            |  ${address.city},
            |  ${address.state},
            |  ST_SetSRID(ST_MakePoint(${address.coords.lat}, ${address.coords.lon}), 4326)
            |)
            |""".stripMargin
    }
  }
}
