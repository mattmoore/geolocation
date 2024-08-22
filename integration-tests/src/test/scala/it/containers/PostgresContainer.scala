package geolocation.it.containers

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresContainer {
  def apply(): PostgreSQLContainer.Def =
    PostgreSQLContainer.Def(
      dockerImageName = DockerImageName
        .parse("postgis/postgis:latest")
        .asCompatibleSubstituteFor("postgres"),
      databaseName = "geolocation",
      username = "scala",
      password = "scala",
      // Leaving this here as an example of how you can run postgres init SQL scripts within testcontainers
      // commonJdbcParams = JdbcDatabaseContainer.CommonParams(
      //   initScriptPath = "database/init.sql".some,
      // ),
    )
}
