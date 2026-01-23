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

import com.codahale.metrics.SharedMetricRegistries
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo, verify}
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.LoggerFactory
import play.api.Application
import ch.qos.logback.classic.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.customs.hodsproxy.connectors.VatKnownFactsControlListConnector
import uk.gov.hmrc.customs.hodsproxy.metrics.CdsMetrics
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum.VAT_KNOWN_FACTS_CONTROL_LIST
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import util.ExternalServicesStubs

class VatKnownFactsControlListConnectorSpec extends IntegrationTestSpec with MockitoSugar with ExternalServicesStubs with LogCapturing {

  private val responseJson = """ {"response" :true} """

  SharedMetricRegistries.clear()

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(
    appConfig ++ Seq(
      "microservice.services.vat-known-facts-control-list.host"         -> Host,
      "microservice.services.vat-known-facts-control-list.port"         -> Port,
      "microservice.services.vat-known-facts-control-list.bearer-token" -> "dummy-token",
      "auditing.enabled"                                                -> false,
      "auditing.consumer.baseUri.host"                                  -> Host,
      "auditing.consumer.baseUri.port"                                  -> Port
    )
  ).build()

  private val metrics = app.injector.instanceOf[CdsMetrics]

  private lazy val vatKnownFactsControlListConnector = app.injector.instanceOf[VatKnownFactsControlListConnector]

  val connectorLogger: Logger =
    LoggerFactory
      .getLogger(classOf[VatKnownFactsControlListConnector])
      .asInstanceOf[Logger]

  private val vrn = "123456789"

  "vat known facts connector" should {

    "have 'Environment' header in the headers" in {

      setVatKnownFactsToReturnTheResponse(vrn, responseJson, OK)

      withCaptureOfLoggingFrom(connectorLogger) { events =>
        val res = vatKnownFactsControlListConnector.get(vrn)
        whenReady(res) { result =>
          events.collectFirst {
            case event =>
              event.getLevel.levelStr shouldBe "INFO"
              event.getMessage.contains("[vat-known-facts-control-list][Connector] GET url:") shouldBe true
          }.getOrElse(fail("No log was captured"))

          result.status shouldBe OK
          result.body shouldBe responseJson
          verifyCorrectRequestWasMade("123456789")
        }
      }
    }
  }

  "record timing and increase the Fail Counter when response is not OK (200)" in {

    val previousTimerCount   = metrics.timers(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    val previousSuccessCount = metrics.successCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    val previousFailedCount  = metrics.failedCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    setVatKnownFactsToReturnTheResponse(vrn, responseJson, INTERNAL_SERVER_ERROR)
    
    withCaptureOfLoggingFrom(connectorLogger) { events =>
      val res = vatKnownFactsControlListConnector.get(vrn)
      whenReady(res) { _ =>
        events.collectFirst {
          case event =>
            event.getLevel.levelStr shouldBe "INFO"
            event.getMessage.contains("[vat-known-facts-control-list][Connector] GET url:") shouldBe true
        }.getOrElse(fail("No log was captured"))

        metrics.failedCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousFailedCount + 1)
        metrics.timers(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousTimerCount + 1)
        metrics.successCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousSuccessCount)
      }
    }
  }

  "record timing and increase the Fail Counter when response is not found" in {

    val previousTimerCount = metrics.timers(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    val previousSuccessCount = metrics.successCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    val previousFailedCount = metrics.failedCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount
    setVatKnownFactsToReturnTheResponse(vrn, responseJson, NOT_FOUND)

    withCaptureOfLoggingFrom(connectorLogger) { events =>
      val res = vatKnownFactsControlListConnector.get(vrn)
      whenReady(res) { _ =>
        events.collectFirst {
          case event =>
            event.getLevel.levelStr shouldBe "INFO"
            event.getMessage.contains("[vat-known-facts-control-list][Connector] GET url:") shouldBe true
        }.getOrElse(fail("No log was captured"))

        metrics.failedCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousFailedCount + 1)
        metrics.timers(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousTimerCount + 1)
        metrics.successCounters(VAT_KNOWN_FACTS_CONTROL_LIST).getCount should be(previousSuccessCount)
      }
    }
  }

  "return correct result when service response is 5xx" in {

    setVatKnownFactsToReturnTheResponse(vrn, responseJson, INTERNAL_SERVER_ERROR)

    withCaptureOfLoggingFrom(connectorLogger) { events =>
      val res = vatKnownFactsControlListConnector.get(vrn)
      whenReady(res) { result =>
        events.collectFirst {
          case event =>
            event.getLevel.levelStr shouldBe "INFO"
            event.getMessage.contains("[vat-known-facts-control-list][Connector] GET url:") shouldBe true
        }.getOrElse(fail("No log was captured"))

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body shouldBe responseJson
      }
    }
  }

  def verifyCorrectRequestWasMade(vrn: String): Unit =
    verify(
      getRequestedFor(urlEqualTo("/" + VatKnownFactsContext + "/" + vrn))
        .withHeader("Environment", equalTo("ist0"))
    )

}
