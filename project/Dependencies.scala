import sbt._

object Dependencies {
  val scalaVersion = "2.13.1"

  val logbackVersion = "1.2.3"

  val akkaHttpCirceVersion = "1.30.0"
  val akkaHttpVersion = "10.1.11"
  val akkaPersistenceJdbcVersion = "3.5.2"
  val akkaVersion = "2.6.1"

  val circeVersion = "0.12.3"

  val slickVersion = "3.3.2"
  val postgresqlVersion = "42.2.9"

  val scalaTestVersion = "3.1.0"

  val core = Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaPersistenceJdbcVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "org.postgresql" % "postgresql" % postgresqlVersion
  )

  val test = Seq(
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion
  ) map (_ % Test)
}
