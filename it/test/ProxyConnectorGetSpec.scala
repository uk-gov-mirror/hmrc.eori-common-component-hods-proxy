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

package integration

import com.github.tomakehurst.wiremock.client.WireMock.{verify => wVerify, _}
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.{reset => mreset, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors.{HeaderGenerator, ProxyConnector}
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum.SUBSCRIPTION_STATUS
import uk.gov.hmrc.customs.hodsproxy.metrics.{CdsMetrics, MetricsEnum}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.ExternalServicesStubs

import scala.concurrent.ExecutionContext.Implicits.global

class ProxyConnectorGetSpec extends IntegrationTestSpec with ExternalServicesStubs with MockitoSugar {

  private val mockHeaderGenerator = mock[HeaderGenerator]
  private val mockServicesConfig  = mock[ServicesConfig]
  private val metrics             = app.injector.instanceOf[CdsMetrics]
  private val httpClient          = app.injector.instanceOf[HttpClientV2]

  private val serviceBearerToken = "1One2Two"

  private val connector = new ProxyConnector(httpClient, mockServicesConfig, metrics, mockHeaderGenerator) {
    val metricsId           = MetricsEnum.SUBSCRIPTION_STATUS
    val serviceName: String = "some-get-service"
  }

  private val responseJson                     = """ {"response" :true} """
  private val expectedQueryParamString: String = "?queryParam1=value1&queryParam1=value2&queryParam2=value3"

  private val queryParamMap: Map[String, List[String]] =
    Map("queryParam1" -> List("value1", "value2"), "queryParam2" -> List("value3"))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockHeaderGenerator.headersForMDGGet(meq(serviceBearerToken))).thenReturn(
      Seq("dummyHeader" -> serviceBearerToken)
    )
    when(mockServicesConfig.getString(endsWith("bearer-token"))).thenReturn(serviceBearerToken)
    when(mockServicesConfig.baseUrl(anyString)).thenReturn(s"http://$Host:$Port/")
    when(mockServicesConfig.getString(endsWith("context"))).thenReturn(SubscriptionStatusServiceContext)
  }

  override protected def afterEach(): Unit = {
    mreset(mockHeaderGenerator)
    mreset(mockServicesConfig)

    super.afterEach()
  }

  "ProxyGetConnector" when {

    "calling get" must {

      "return correct response when correct url and headers" in {

        setSubscriptionStatusToReturnTheResponse(expectedQueryParamString, responseJson, OK)

        val result = connector.get(queryParamMap).futureValue

        result.status shouldBe OK
        result.body shouldBe responseJson
        verifyCorrectRequestWasMade(expectedQueryParamString)
      }

      "return correct response when correct url and headers but no query params" in {

        setSubscriptionStatusToReturnTheResponse("", responseJson, OK)

        val result = connector.get(Map.empty).futureValue

        result.status shouldBe OK
        result.body shouldBe responseJson
        wVerify(getRequestedFor(urlEqualTo("/" + SubscriptionStatusServiceContext)))
      }

      "record timing and increase the Success Counter when response is OK" in {

        val previousTimerCount   = metrics.timers(SUBSCRIPTION_STATUS).getCount
        val previousSuccessCount = metrics.successCounters(SUBSCRIPTION_STATUS).getCount
        val previousFailedCount  = metrics.failedCounters(SUBSCRIPTION_STATUS).getCount
        setSubscriptionStatusToReturnTheResponse(expectedQueryParamString, responseJson, OK)

        await(connector.get(queryParamMap))

        metrics.successCounters(SUBSCRIPTION_STATUS).getCount shouldBe previousSuccessCount + 1
        metrics.timers(SUBSCRIPTION_STATUS).getCount shouldBe previousTimerCount + 1
        metrics.failedCounters(SUBSCRIPTION_STATUS).getCount shouldBe previousFailedCount
      }

      "return correct result when service response is not OK(200)" in {

        setSubscriptionStatusToReturnTheResponse(expectedQueryParamString, responseJson, BAD_REQUEST)

        val result = connector.get(queryParamMap).futureValue

        result.status shouldBe BAD_REQUEST
        result.body shouldBe responseJson
      }

      "return correct result when service response is 5xx" in {

        setSubscriptionStatusToReturnTheResponse(expectedQueryParamString, responseJson, INTERNAL_SERVER_ERROR)

        val result = connector.get(queryParamMap).futureValue

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body shouldBe responseJson
      }

      "record timing and increase the Fail Counter when response is not OK (200)" in {

        val previousTimerCount   = metrics.timers(SUBSCRIPTION_STATUS).getCount
        val previousSuccessCount = metrics.successCounters(SUBSCRIPTION_STATUS).getCount
        val previousFailedCount  = metrics.failedCounters(SUBSCRIPTION_STATUS).getCount
        setSubscriptionStatusToReturnTheResponse(expectedQueryParamString, responseJson, INTERNAL_SERVER_ERROR)

        await(connector.get(queryParamMap))

        metrics.failedCounters(SUBSCRIPTION_STATUS).getCount should be(previousFailedCount + 1)
        metrics.timers(SUBSCRIPTION_STATUS).getCount should be(previousTimerCount + 1)
        metrics.successCounters(SUBSCRIPTION_STATUS).getCount should be(previousSuccessCount)
      }
    }
  }

  private def verifyCorrectRequestWasMade(expectedQueryParam: String): Unit =
    wVerify(
      getRequestedFor(urlEqualTo("/" + SubscriptionStatusServiceContext + expectedQueryParam))
        .withHeader("dummyHeader", equalTo(serviceBearerToken))
    )

}
