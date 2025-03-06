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
import org.mockito.Mockito.{when, reset => mreset}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE, DATE, X_FORWARDED_HOST}
import play.api.http.MimeTypes
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors.{HeaderGenerator, RegisterSubscribeWithoutIdConnector}
import uk.gov.hmrc.customs.hodsproxy.metrics.CdsMetrics
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum.REGISTER_WITHOUT_ID
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.ExternalServicesStubs

import scala.concurrent.ExecutionContext.Implicits.global

class RegisterSubscribeWithoutIdConnectorSpec extends IntegrationTestSpec with ExternalServicesStubs with MockitoSugar {

  private val serviceBearerToken = "1234ABCD"

  private val mockHeaderGenerator = mock[HeaderGenerator]
  private val mockServicesConfig = mock[ServicesConfig]

  private val metrics = app.injector.instanceOf[CdsMetrics]
  private val httpClient = app.injector.instanceOf[HttpClient]

  private val connector = new RegisterSubscribeWithoutIdConnector(httpClient, mockServicesConfig, metrics, mockHeaderGenerator)

  private val requestJson = """{"request":true}"""
  private val responseJson = """ {"response" :true} """

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockServicesConfig.getString(endsWith("bearer-token"))).thenReturn(serviceBearerToken)
    when(mockServicesConfig.baseUrl(anyString)).thenReturn(s"http://$Host:$Port/")
    when(mockServicesConfig.getString(endsWith("context"))).thenReturn(RegSubWithoutIdServiceContext)
  }

  override protected def afterEach(): Unit = {
    mreset(mockHeaderGenerator)
    mreset(mockServicesConfig)
    super.afterEach()
  }

  "RegisterSubscribeWithoutIdConnector" when {

    "calling post" must {

      "return correct response when posted to correct url with correct payload and headers" in {

        setRegSubWithoutIdToReturnTheResponse(responseJson, OK)

        val result = connector.post(Json.parse(requestJson)).futureValue

        result.status shouldBe OK
        result.body shouldBe responseJson
        verifyCorrectRequestWasMade(requestJson)
      }

      "record timing and increase the Success Counter when response is OK" in {

        val previousTimerCount = metrics.timers(REGISTER_WITHOUT_ID).getCount
        val previousSuccessCount = metrics.successCounters(REGISTER_WITHOUT_ID).getCount
        val previousFailedCount = metrics.failedCounters(REGISTER_WITHOUT_ID).getCount
        setRegSubWithoutIdToReturnTheResponse(responseJson, OK)

        await(connector.post(Json.parse(requestJson)))

        metrics.successCounters(REGISTER_WITHOUT_ID).getCount shouldBe previousSuccessCount + 1
        metrics.timers(REGISTER_WITHOUT_ID).getCount shouldBe previousTimerCount + 1
        metrics.failedCounters(REGISTER_WITHOUT_ID).getCount shouldBe previousFailedCount
      }

      "return correct result when service response is not OK(200)" in {

        setRegSubWithoutIdToReturnTheResponse(responseJson, BAD_REQUEST)

        val result = connector.post(Json.parse(requestJson)).futureValue

        result.status shouldBe BAD_REQUEST
        result.body shouldBe responseJson
      }

      "return correct result when service response is 5xx" in {

        setRegSubWithoutIdToReturnTheResponse(responseJson, INTERNAL_SERVER_ERROR)

        val result = connector.post(Json.parse(requestJson)).futureValue

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body shouldBe responseJson
      }

      "record timing and increase the Fail Counter when response is not OK (200)" in {

        val previousTimerCount = metrics.timers(REGISTER_WITHOUT_ID).getCount
        val previousSuccessCount = metrics.successCounters(REGISTER_WITHOUT_ID).getCount
        val previousFailedCount = metrics.failedCounters(REGISTER_WITHOUT_ID).getCount
        setRegSubWithoutIdToReturnTheResponse(responseJson, INTERNAL_SERVER_ERROR)

        await(connector.post(Json.parse(requestJson)))

        metrics.failedCounters(REGISTER_WITHOUT_ID).getCount shouldBe previousFailedCount + 1
        metrics.timers(REGISTER_WITHOUT_ID).getCount shouldBe previousTimerCount + 1
        metrics.successCounters(REGISTER_WITHOUT_ID).getCount shouldBe previousSuccessCount
      }
    }
  }

  def verifyCorrectRequestWasMade(expectedRequest: String): Unit =
    wVerify(
      postRequestedFor(urlMatching("/" + RegSubWithoutIdServiceContext))
        .withRequestBody(equalToJson(expectedRequest))
        .withHeader(AUTHORIZATION, equalTo(s"Bearer $serviceBearerToken"))
        .withHeader(X_FORWARDED_HOST, equalTo("MDTP"))
        .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
        .withHeader(ACCEPT, equalTo(MimeTypes.JSON))
        .withHeader(DATE, matching("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), [0-9]{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} (UTC|GMT)$"))
    )
}
