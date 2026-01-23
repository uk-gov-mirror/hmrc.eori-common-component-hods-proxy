/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit

import base.BaseSpec
import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.TestData
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}

class MdgTokensConfigurationTest extends BaseSpec with GuiceOneAppPerTest with TableDrivenPropertyChecks {

  private val defaultBearerToken = "bearer_token_must_be_set_in_app-config-xxx"

  private val bearerTokenFromCommandLine = "Bearer token from command line"

  private val tokensOverrideFromCommandLine = Map("tokens.bearer-token" -> bearerTokenFromCommandLine)

  SharedMetricRegistries.clear()

  override def newAppForTest(testData: TestData): Application = {
    val extraSystemProperties: Map[String, String] =
      testData.name match {
        case fromCommandLine if fromCommandLine.contains("from command line") => tokensOverrideFromCommandLine
        case _                                                                => Map.empty
      }
    GuiceApplicationBuilder(loadConfiguration = env => Configuration.load(env, extraSystemProperties)).build()
  }

  private val configuredServices =
    Table(
      heading = "service configuration name",
      "subscription-service",
      "register-with-id",
      "register-with-eori-and-id",
      "register-without-id",
      "subscription-status",
      "subscription-display"
    )

  forAll(configuredServices) { serviceName =>
    s"Bearer token for $serviceName service" should {

      "be a default bearer token for default environment when there's no override" in {
        val bearerTokenForDefaultEnvironment =
          app.configuration.get[String](s"microservice.services.$serviceName.bearer-token")

        bearerTokenForDefaultEnvironment shouldBe defaultBearerToken
      }

      "override bearer token for default environment from command line" in {
        val bearerTokenForDefaultEnvironment =
          app.configuration.get[String](s"microservice.services.$serviceName.bearer-token")

        bearerTokenForDefaultEnvironment shouldBe bearerTokenFromCommandLine
      }
    }
  }
}
