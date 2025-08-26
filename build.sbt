import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}

import scala.language.postfixOps
name := "eori-common-component-hods-proxy"
ThisBuild / majorVersion := 0
PlayKeys.devSettings := Seq("play.server.http.port" -> "6753")
ThisBuild / scalaVersion := "2.13.16"

lazy val commonSettings: Seq[Setting[_]] = defaultSettings()

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(commonSettings, scoverageSettings)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings
  )

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      Test / unmanagedSourceDirectories := Seq((Test / baseDirectory).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>"
    , "Reverse.*"
    , ".*(BuildInfo|Routes).*"
    , ".*Logger.*"
  ).mkString(";"),
  coverageMinimumStmtTotal := 94,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)
scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

scalacOptions ++= Seq (
  "-Wconf:cat=unused-imports&src=routes/.*:s"
)

val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)
