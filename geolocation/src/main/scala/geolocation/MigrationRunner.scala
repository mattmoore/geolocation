package geolocation

import cats.effect.*
import geolocation.domain.DatabaseConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

trait MigrationRunner[F[_]] {
  def migrate(config: DatabaseConfig): F[MigrateResult]
}

object MigrationRunner {
  def apply[F[_]: Async](): Resource[F, MigrationRunner[F]] =
    Resource.eval[F, MigrationRunner[F]] {
      Async[F].delay {
        new MigrationRunner[F] {
          override def migrate(config: DatabaseConfig): F[MigrateResult] =
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
    }
}
