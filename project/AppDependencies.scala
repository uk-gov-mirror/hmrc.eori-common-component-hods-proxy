import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"              %% "bootstrap-backend-play-30"      % bootstrapVersion exclude("org.apache.commons", "commons-lang3"),
    "org.apache.commons"        % "commons-lang3"                  % "3.18.0",
    "uk.gov.hmrc"              %% "internal-auth-client-play-30"   % "4.3.0",
    "at.yawk.lz4"               %  "lz4-java"                      % "1.10.3",
    "ch.qos.logback"            % "logback-core"                   % "1.5.21",
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"            %% "scalatest"                      % "3.2.19"             % Test,
    "org.playframework"        %% "play-test"                      % PlayVersion.current  % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"             % "7.0.2"              % Test,
    "org.scalatestplus"        %% "mockito-4-6"                    % "3.2.15.0"           % Test,
    "org.mockito"              %  "mockito-core"                   % "5.21.0"             % Test,
    "com.vladsch.flexmark"     %  "flexmark-all"                   % "0.64.8"             % Test,
    "uk.gov.hmrc"              %% "bootstrap-test-play-30"         % bootstrapVersion     % Test
  )
}