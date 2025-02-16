package geolocation

import cats.effect.Async
import cats.effect.kernel.Resource
import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import geolocation.domain.AppConfig

object TransactorR {
  def apply[F[_]: Async](config: AppConfig): Resource[F, Transactor[F]] =
    for {
      hikariConfig <- Resource.eval {
        Async[F].delay {
          val hc = new HikariConfig
          hc.setDriverClassName("org.postgresql.Driver")
          hc.setJdbcUrl(s"jdbc:postgresql://${config.databaseConfig.host}:${config.databaseConfig.port}/geolocation")
          hc.setUsername(config.databaseConfig.username)
          hc.setPassword(config.databaseConfig.password)
          hc
        }
      }
      xa <- HikariTransactor.fromHikariConfig(hikariConfig)
    } yield xa
}
