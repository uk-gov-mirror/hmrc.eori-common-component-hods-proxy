/*
 * Copyright 2025 HM Revenue & Customs
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

package integration

import com.github.tomakehurst.wiremock.client.WireMock.{verify => wVerify, _}
import org.mockito.ArgumentMatchers.{anyString, endsWith}
import org.mockito.Mockito.{reset => mreset, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors.{HeaderGenerator, UpdateVerifiedEmailConnector}
import uk.gov.hmrc.customs.hodsproxy.metrics.CdsMetrics
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum.UPDATE_VERIFIED_EMAIL
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.ExternalServicesStubs

import scala.concurrent.ExecutionContext.Implicits.global

class UpdateVerifiedEmailConnectorSpec extends IntegrationTestSpec with ExternalServicesStubs with MockitoSugar {

  private val serviceBearerToken = "1234ABCD"

  private val mockHeaderGenerator = mock[HeaderGenerator]
  private val mockServicesConfig  = mock[ServicesConfig]

  private val metrics    = app.injector.instanceOf[CdsMetrics]
  private val httpClient = app.injector.instanceOf[HttpClient]

  private val connector = new UpdateVerifiedEmailConnector(httpClient, mockServicesConfig, metrics, mockHeaderGenerator)

  private val requestJson  = """{"request":true}"""
  private val responseJson = """ {"response" :true} """

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockHeaderGenerator.headersForMDG(serviceBearerToken)).thenReturn(Seq("testHeader" -> serviceBearerToken))
    when(mockServicesConfig.getString(endsWith("bearer-token"))).thenReturn(serviceBearerToken)
    when(mockServicesConfig.baseUrl(anyString)).thenReturn(s"http://$Host:$Port/")
    when(mockServicesConfig.getString(endsWith("context"))).thenReturn("subscriptions/updateverifiedemail/v1")
  }

  override protected def afterEach(): Unit = {
    mreset(mockHeaderGenerator)
    mreset(mockServicesConfig)
    super.afterEach()
  }

  "ProxyConnector" when {

    "calling put" must {

      "return correct response when posted to correct url with correct payload and headers" in {

        stubUpdateVerifiedEmailResponse(responseJson, OK)

        val result = connector.put(Json.parse(requestJson)).futureValue

        result.status shouldBe OK
        result.body shouldBe responseJson
        verifyCorrectRequestWasMade(requestJson)
      }

      "record timing and increase the Success Counter when response is OK" in {

        val previousTimerCount   = metrics.timers(UPDATE_VERIFIED_EMAIL).getCount
        val previousSuccessCount = metrics.successCounters(UPDATE_VERIFIED_EMAIL).getCount
        val previousFailedCount  = metrics.failedCounters(UPDATE_VERIFIED_EMAIL).getCount
        stubUpdateVerifiedEmailResponse(responseJson, OK)

        await(connector.put(Json.parse(requestJson)))

        metrics.successCounters(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousSuccessCount + 1
        metrics.timers(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousTimerCount + 1
        metrics.failedCounters(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousFailedCount
      }

      "return correct result when service response is not OK(200)" in {

        stubUpdateVerifiedEmailResponse(responseJson, BAD_REQUEST)

        val result = connector.put(Json.parse(requestJson)).futureValue

        result.status shouldBe BAD_REQUEST
        result.body shouldBe responseJson
      }

      "return correct result when service response is 5xx" in {

        stubUpdateVerifiedEmailResponse(responseJson, INTERNAL_SERVER_ERROR)

        val result = connector.put(Json.parse(requestJson)).futureValue

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body shouldBe responseJson
      }

      "record timing and increase the Fail Counter when response is not OK (200)" in {

        val previousTimerCount   = metrics.timers(UPDATE_VERIFIED_EMAIL).getCount
        val previousSuccessCount = metrics.successCounters(UPDATE_VERIFIED_EMAIL).getCount
        val previousFailedCount  = metrics.failedCounters(UPDATE_VERIFIED_EMAIL).getCount
        stubUpdateVerifiedEmailResponse(responseJson, INTERNAL_SERVER_ERROR)

        await(connector.put(Json.parse(requestJson)))

        metrics.failedCounters(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousFailedCount + 1
        metrics.timers(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousTimerCount + 1
        metrics.successCounters(UPDATE_VERIFIED_EMAIL).getCount shouldBe previousSuccessCount
      }
    }
  }

  def verifyCorrectRequestWasMade(expectedRequest: String): Unit =
    wVerify(
      putRequestedFor(urlMatching("/" + UpdateVerifiedEmailContext))
        .withRequestBody(equalToJson(expectedRequest))
        .withHeader("testHeader", equalTo(serviceBearerToken))
    )

}
