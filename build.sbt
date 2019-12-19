lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.rust",
  version := "1.0",
  scalaVersion := Dependencies.scalaVersion,
  scalacOptions ++= Seq(
    "-Werror",
    "-deprecation",
    "-explaintypes",
    "-feature",
    "-opt:l:method,inline",
    "-unchecked",
    "-Xcheckinit",
    "-Xlint:adapted-args",
    "-Xlint:nullary-unit",
    "-Xlint:inaccessible",
    "-Xlint:nullary-override",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:doc-detached",
    "-Xlint:private-shadow",
    "-Xlint:type-parameter-shadow",
    "-Xlint:poly-implicit-overload",
    "-Xlint:option-implicit",
    "-Xlint:delayedinit-select",
    "-Xlint:package-object-classes",
    "-Xlint:stars-align",
    "-Xlint:constant",
    "-Xlint:unused",
    "-Xlint:nonlocal-return",
    "-Xlint:deprecation",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-unused:patvars,params"
  )
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "counterlist"
  )
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    name := "core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test
  )
