package geolocation

import cats.effect.*
import geolocation.domain.DatabaseConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

trait MigrationRunner {
  def apply[F[_]: Async](config: DatabaseConfig): Resource[F, MigrateResult]
}

object MigrationRunner extends MigrationRunner {
  def apply[F[_]: Async](config: DatabaseConfig): Resource[F, MigrateResult] =
    Resource.eval {
      Async[F].delay {
        Flyway
          .configure()
          .baselineOnMigrate(true)
          .locations(config.migrationsLocation)
          .dataSource(
            s"jdbc:postgresql://${config.host}:${config.port}/${config.database}",
            config.username,
            config.password,
          )
          .load()
          .migrate()
      }
    }
}
