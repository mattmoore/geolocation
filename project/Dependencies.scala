import sbt.*

object Dependencies {
  object Projects {
    lazy val geolocation = Seq(
      Circe.circeCore,
      Circe.circeGeneric,
      Circe.circeLiteral,
      Ciris.ciris,
      Flyway.flywayCore,
      Ip4s.ip4s,
      PostgreSql.postgresql,
      Doobie.doobieCore,
      Doobie.doobieHikari,
      Typelevel.catsCore,
      Typelevel.catsEffect,
      Typelevel.log4catsCore,
      Typelevel.otel4sCoreCommon,
      Typelevel.otel4sCoreMetrics,
      Typelevel.otel4sCoreTrace,
      Typelevel.otel4sOteljava,
      Typelevel.otel4sOteljavaCommon,
      Weaver.weaverCats,
    )

    lazy val geolocationHttp = Seq(
      Circe.circeCore,
      Circe.circeGeneric,
      Circe.circeParser,
      Flyway.flywayCore,
      Flyway.flywayDatabasePostgresql % Runtime,
      FS2.fs2Core,
      FS2.fs2Io,
      Http4s.http4sCore,
      Http4s.http4sServer,
      Http4s.http4sEmberServer,
      Http4s.http4sPrometheusMetrics,
      Ip4s.ip4s,
      Logback.logback                % Runtime,
      Logback.logstashLogbackEncoder % Runtime,
      OpenTelemetry.opentelemetrySdkExtensionAutoconfigure,
      OpenTelemetry.opentelemetryExporterOtlp,
      Prometheus.prometheusMetricsCore,
      Prometheus.prometheusMetricsModel,
      Prometheus.prometheusSimpleClient,
      Tapir.tapirModelCore,
      Tapir.tapirSharedCore,
      Tapir.tapirSharedFs2,
      Tapir.tapirCore,
      Tapir.tapirServer,
      Tapir.tapirHttp4sServer,
      Tapir.tapirJsoniterScala,
      Tapir.tapirPrometheusMetrics,
      Tapir.jsoniterScalaCore,
      Tapir.jsoniterScalaMacro,
      Doobie.doobieCore,
      Doobie.doobieHikari,
      Typelevel.catsCore,
      Typelevel.catsEffect,
      Typelevel.catsEffectKernel,
      Typelevel.catsEffectStd,
      Typelevel.caseInsensitive,
      Typelevel.log4catsCore,
      Typelevel.log4catsSlf4j,
      Typelevel.otel4sCoreCommon,
      Typelevel.otel4sCoreMetrics,
      Typelevel.otel4sCoreTrace,
      Typelevel.otel4sOteljava,
      Typelevel.otel4sOteljavaCommon,
    )

    lazy val integrationTests = Seq(
      Flyway.flywayCore               % Test,
      Flyway.flywayDatabasePostgresql % Test,
      Logback.logback                 % Runtime,
      Logback.logstashLogbackEncoder  % Runtime,
      TestContainers.testContainersScala,
      TestContainers.testContainersScalaPostgresql,
    )

    lazy val loadTests = Seq(
      Flyway.flywayCore               % Test,
      Flyway.flywayDatabasePostgresql % Test,
      Gatling.gatlingCharts,
      Gatling.gatlingTestFramework,
      Logback.logback                % Runtime,
      Logback.logstashLogbackEncoder % Runtime,
      TestContainers.testContainersScala,
      TestContainers.testContainersScalaPostgresql,
    )
  }

  object Typelevel {
    lazy val catsCore             = "org.typelevel" %% "cats-core"              % Versions.catsCore
    lazy val catsKernel           = "org.typelevel" %% "cats-kernel"            % Versions.catsCore
    lazy val catsEffect           = "org.typelevel" %% "cats-effect"            % Versions.catsEffect
    lazy val catsEffectStd        = "org.typelevel" %% "cats-effect-std"        % Versions.catsEffect
    lazy val catsEffectKernel     = "org.typelevel" %% "cats-effect-kernel"     % Versions.catsEffect
    lazy val caseInsensitive      = "org.typelevel" %% "case-insensitive"       % Versions.caseInsensitive
    lazy val log4catsCore         = "org.typelevel" %% "log4cats-core"          % Versions.log4cats
    lazy val log4catsSlf4j        = "org.typelevel" %% "log4cats-slf4j"         % Versions.log4cats
    lazy val otel4sCoreCommon     = "org.typelevel" %% "otel4s-core-common"     % Versions.otel4s
    lazy val otel4sCoreMetrics    = "org.typelevel" %% "otel4s-core-metrics"    % Versions.otel4s
    lazy val otel4sCoreTrace      = "org.typelevel" %% "otel4s-core-trace"      % Versions.otel4s
    lazy val otel4sOteljava       = "org.typelevel" %% "otel4s-oteljava"        % Versions.otel4s
    lazy val otel4sOteljavaCommon = "org.typelevel" %% "otel4s-oteljava-common" % Versions.otel4s
  }

  object Circe {
    lazy val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
    lazy val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
    lazy val circeLiteral = "io.circe" %% "circe-literal" % Versions.circe
    lazy val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe
  }

  object Ciris {
    lazy val ciris = "is.cir" %% "ciris" % Versions.ciris
  }

  object Flyway {
    lazy val flywayCore               = "org.flywaydb" % "flyway-core"                % Versions.flyway
    lazy val flywayDatabasePostgresql = "org.flywaydb" % "flyway-database-postgresql" % Versions.flyway
  }

  object FS2 {
    lazy val fs2Core            = "co.fs2" %% "fs2-core"             % Versions.fs2
    lazy val fs2Io              = "co.fs2" %% "fs2-io"               % Versions.fs2
    lazy val fs2ReactiveStreams = "co.fs2" %% "fs2-reactive-streams" % Versions.fs2
    lazy val fs2Codec           = "co.fs2" %% "fs2-scodec"           % Versions.fs2
  }

  object Gatling {
    lazy val gatlingCharts        = "io.gatling.highcharts" % "gatling-charts-highcharts" % Versions.gatling % "test"
    lazy val gatlingTestFramework = "io.gatling"            % "gatling-test-framework"    % Versions.gatling % "test"
  }

  object Http4s {
    lazy val http4sCore              = "org.http4s" %% "http4s-core"               % Versions.http4s
    lazy val http4sServer            = "org.http4s" %% "http4s-server"             % Versions.http4s
    lazy val http4sEmberClient       = "org.http4s" %% "http4s-ember-client"       % Versions.http4s
    lazy val http4sEmberServer       = "org.http4s" %% "http4s-ember-server"       % Versions.http4s
    lazy val http4sDsl               = "org.http4s" %% "http4s-dsl"                % Versions.http4s
    lazy val http4sCirce             = "org.http4s" %% "http4s-circe"              % Versions.http4s
    lazy val http4sPrometheusMetrics = "org.http4s" %% "http4s-prometheus-metrics" % Versions.http4sPrometheusMetrics
  }

  object Ip4s {
    lazy val ip4s = "com.comcast" %% "ip4s-core" % Versions.ip4s
  }

  object Logback {
    lazy val logback                = "ch.qos.logback"       % "logback-classic"          % Versions.logback
    lazy val logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstashLogbackEncoder
  }

  object OpenTelemetry {
    lazy val opentelemetryExporterOtlp              = "io.opentelemetry" % "opentelemetry-exporter-otlp"               % Versions.opentelemetry % Runtime
    lazy val opentelemetryExporterLogging           = "io.opentelemetry" % "opentelemetry-exporter-logging"            % Versions.opentelemetry % Runtime
    lazy val opentelemetrySdkExtensionAutoconfigure = "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % Versions.opentelemetry
  }

  object PostgreSql {
    lazy val postgresql = "org.postgresql" % "postgresql" % Versions.postgres
  }

  object Prometheus {
    lazy val prometheusMetricsCore  = "io.prometheus" % "prometheus-metrics-core"  % Versions.prometheus
    lazy val prometheusMetricsModel = "io.prometheus" % "prometheus-metrics-model" % Versions.prometheus
    lazy val prometheusSimpleClient = "io.prometheus" % "simpleclient"             % Versions.prometheusSimpleClient
  }

  object Tapir {
    lazy val tapirModelCore         = "com.softwaremill.sttp.model"           %% "core"                     % Versions.tapirModelCore
    lazy val tapirSharedCore        = "com.softwaremill.sttp.shared"          %% "core"                     % Versions.tapirShared
    lazy val tapirSharedFs2         = "com.softwaremill.sttp.shared"          %% "fs2"                      % Versions.tapirShared
    lazy val tapirCore              = "com.softwaremill.sttp.tapir"           %% "tapir-core"               % Versions.tapir
    lazy val tapirServer            = "com.softwaremill.sttp.tapir"           %% "tapir-server"             % Versions.tapir
    lazy val tapirHttp4sServer      = "com.softwaremill.sttp.tapir"           %% "tapir-http4s-server"      % Versions.tapir
    lazy val tapirSwaggerUiBundle   = "com.softwaremill.sttp.tapir"           %% "tapir-swagger-ui-bundle"  % Versions.tapir
    lazy val tapirJsoniterScala     = "com.softwaremill.sttp.tapir"           %% "tapir-jsoniter-scala"     % Versions.tapir
    lazy val tapirJsonCirce         = "com.softwaremill.sttp.tapir"           %% "tapir-json-circe"         % Versions.tapir
    lazy val tapirPrometheusMetrics = "com.softwaremill.sttp.tapir"           %% "tapir-prometheus-metrics" % Versions.tapir
    lazy val jsoniterScalaCore      = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"      % Versions.jsoniterScala
    lazy val jsoniterScalaMacro     = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"    % Versions.jsoniterScala
  }

  object Doobie {
    lazy val doobieCore     = "org.tpolecat" %% "doobie-core"     % Versions.doobie
    lazy val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % Versions.doobie
    lazy val doobieHikari   = "org.tpolecat" %% "doobie-hikari"   % Versions.doobie
  }

  object TestContainers {
    lazy val testContainersScala           = "com.dimafeng" %% "testcontainers-scala"            % Versions.testContainers % Test
    lazy val testContainersScalaPostgresql = "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testContainers % Test
  }

  object Weaver {
    lazy val weaverCats = "com.disneystreaming" %% "weaver-cats" % Versions.weaver
  }
}
