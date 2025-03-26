import sbtwelcome.*

ThisBuild / scalaVersion                        := "3.6.3"
ThisBuild / Test / parallelExecution            := true
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("22"))
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

lazy val root = (project in file("."))
  .enablePlugins(
    GitBranchPrompt,
    GitVersioning,
  )
  .settings(
    name := "geolocation",
    welcomeSettings,
  )
  .aggregate(
    geolocationHttp,
    integrationTests,
  )

lazy val geolocation = (project in file("geolocation"))
  .enablePlugins(
    JavaAgent,
    JavaAppPackaging,
    DockerPlugin,
  )
  .settings(
    name := "geolocation",
    libraryDependencies ++= Dependencies.Projects.geolocation,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    fork                       := true,
    coverageHighlighting       := true,
    coverageFailOnMinimum      := true,
    coverageMinimumStmtTotal   := 10,
    coverageMinimumBranchTotal := 10,
  )

resolvers += Resolver.mavenLocal

lazy val geolocationHttp = (project in file("geolocation-http"))
  .enablePlugins(
    JavaAgent,
    JavaAppPackaging,
    DockerPlugin,
  )
  .dependsOn(geolocation)
  .settings(
    name := "geolocation-http",
    libraryDependencies ++= Dependencies.Projects.geolocationHttp,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Compile / mainClass        := Some("geolocation.http.Main"),
    fork                       := true,
    coverageHighlighting       := true,
    coverageFailOnMinimum      := true,
    coverageMinimumStmtTotal   := 10,
    coverageMinimumBranchTotal := 10,
    assemblyMergeStrategy := {
      case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last.contains("okio")       => MergeStrategy.first
      case x                                                   => (ThisBuild / assemblyMergeStrategy).value(x)
    },
    Docker / packageName := "geolocation",
    Docker / version     := "0.1.0",
    dockerExposedPorts ++= Seq(8080),
    dockerBaseImage := "openjdk:22",
    javaAgents ++= Seq(
      "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "2.14.0",
    ),
    javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      s"-Dotel.service.name=${name.value}",
    ),
  )

lazy val integrationTests = (project in file("integration-tests"))
  .dependsOn(geolocation % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Dependencies.Projects.integrationTests,
    fork := true,
  )

lazy val loadTests = (project in file("load-tests"))
  .enablePlugins(GatlingPlugin)
  .dependsOn(geolocation % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Dependencies.Projects.loadTests,
    fork := true,
  )

addCommandAlias("test", "coverageOn; geolocation/test; geolocationHttp/test; coverageAggregate; coverageOff")
addCommandAlias("formatAll", "scalafmtAll; scalafmtSbt")

lazy val welcomeSettings = Seq(
  logo      := WelcomeScreen(version.value, scalaVersion.value),
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("welcome", "Display this welcome menu.").alias("w"),
    UsefulTask("reload", "Reload sbt.").alias("r"),
    UsefulTask("clean;compile", "Clean compile.").alias("cc"),
    UsefulTask("formatAll", "Format all Scala code.").alias("f"),
    UsefulTask("test", "Run geolocation unit tests with coverage.").alias("t"),
    UsefulTask("integrationTests/test", "Run geolocation integration tests.").alias("it"),
    UsefulTask("geolocationHttp/run", "Run geolocation.").alias("http"),
    UsefulTask("geolocationHttp/Docker/publishLocal", "Publish local docker image.").alias("p"),
    UsefulTask("githubWorkflowGenerate", "Generate GitHub CI/CD.").alias("cicd"),
  ),
)
