import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "10.1.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"              %% "bootstrap-backend-play-30"      % bootstrapVersion,
    "uk.gov.hmrc"              %% "internal-auth-client-play-30"   % "2.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"            %% "scalatest"                      % "3.2.18"             % Test,
    "org.pegdown"              %  "pegdown"                        % "1.6.0"              % Test,
    "org.playframework"        %% "play-test"                      % PlayVersion.current  % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"             % "7.0.1"              % Test,
    "org.scalatestplus"        %% "mockito-4-6"                    % "3.2.15.0"           % Test,
    "org.mockito"              %  "mockito-core"                   % "5.12.0"             % Test,
    "com.vladsch.flexmark"     %  "flexmark-all"                   % "0.64.8"             % Test,
    "uk.gov.hmrc"              %% "bootstrap-test-play-30"         % bootstrapVersion     % Test
  )
}