package geolocation.domain

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import ciris.*

final case class DatabaseConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
    maxConnections: Int,
    migrationsLocation: String = "db",
)

final case class AppConfig(
    port: Int,
    databaseConfig: DatabaseConfig,
)

val databaseConfig: ConfigValue[Effect, DatabaseConfig] =
  (
    env("DB_HOST").as[String].default("localhost"),
    env("DB_PORT").as[Int].default(5432),
    env("DB_USERNAME").as[String].default("scala"),
    env("DB_PASSWORD").as[String].default("scala"),
    env("DB_DATABASE").as[String].default("geolocation"),
    env("DB_MAX_CONNECTIONS").as[Int].default(10),
    env("DB_MIGRATIONS_LOCATION").as[String].default("db"),
  ).parMapN(DatabaseConfig.apply)

val config: ConfigValue[Effect, AppConfig] =
  (
    env("PORT").as[Int].default(8080),
    databaseConfig,
  ).parMapN(AppConfig.apply)

object AppConfig {
  def load[F[_]: Async]: Resource[F, AppConfig] =
    Resource.eval(config.load)
}
