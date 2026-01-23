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

package unit.connectors

import base.BaseSpec
import ch.qos.logback.classic.Logger
import com.codahale.metrics.Timer
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.{mustBe, mustEqual}
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.LoggerFactory
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.Json
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION, CONTENT_TYPE, DATE, X_FORWARDED_HOST}
import uk.gov.hmrc.customs.hodsproxy.connectors.HeaderGenerator.X_CORRELATION_ID
import uk.gov.hmrc.customs.hodsproxy.connectors.{HeaderGenerator, ProxyConnector}
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum.MetricsEnum
import uk.gov.hmrc.customs.hodsproxy.metrics.{CdsMetrics, MetricsEnum}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import java.util.UUID
import scala.concurrent.Future

class ProxyConnectorSpec extends ScalaFutures with BaseSpec with MockitoSugar with LogCapturing {

  val mockHttpClientV2: HttpClientV2       = mock[HttpClientV2]
  val mockConfig: ServicesConfig           = mock[ServicesConfig]
  val mockMetrics: CdsMetrics              = mock[CdsMetrics]
  val mockHeaderGenerator: HeaderGenerator = mock[HeaderGenerator]
  val mockLogger: Logger                   = mock[Logger]
  val mockTimerContext: Timer.Context      = mock[Timer.Context]
  val clock: Clock                         = Clock.systemDefaultZone()
  implicit val hc: HeaderCarrier           = HeaderCarrier()

  val mdgHeaders = Seq(
    DATE -> DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock.withZone(ZoneId.of("GMT")))),
    X_CORRELATION_ID -> UUID.randomUUID().toString,
    X_FORWARDED_HOST -> "MDTP",
    CONTENT_TYPE     -> MimeTypes.JSON,
    ACCEPT           -> MimeTypes.JSON,
    AUTHORIZATION    -> s"Bearer Token"
  )

  val expectedHeaders = Seq("Authorization" -> "Bearer testBearerToken")

  // Mock the necessary configurations
  when(mockConfig.baseUrl(any[String])).thenReturn("http://localhost")
  when(mockConfig.getString(any[String])).thenReturn("bearer-token")
  when(mockMetrics.startTimer(any[MetricsEnum])).thenReturn(mockTimerContext)
  when(mockHeaderGenerator.headersForMDG(any())).thenReturn(mdgHeaders)
  when(mockHeaderGenerator.generate(any())).thenReturn(expectedHeaders)

  // Create a concrete subclass of ProxyConnector for testing
  class TestProxyConnector extends ProxyConnector(
        http = mockHttpClientV2,
        config = mockConfig,
        metrics = mockMetrics,
        headerGenerator = mockHeaderGenerator
      )(using scala.concurrent.ExecutionContext.global) {
    val serviceName            = "test-service"
    val metricsId: MetricsEnum = MetricsEnum.VAT_KNOWN_FACTS_CONTROL_LIST

    // Expose the protected generateHeaders method publicly for testing
    override def generateHeaders: Seq[(String, String)] = super.generateHeaders
  }

  val connector = new TestProxyConnector

  val connectorLogger: Logger =
    LoggerFactory
      .getLogger(classOf[TestProxyConnector])
      .asInstanceOf[Logger]

  "ProxyConnector" should {

    "log the correct GET request and return the response" in {
      val responseJson       = Json.obj("key" -> "value")
      val mockResponse       = HttpResponse(Status.OK, responseJson.toString)
      val mockRequestBuilder = mock[RequestBuilder]

      when(mockHttpClientV2.get(any[URL])(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(using any(), any())).thenReturn(Future.successful(mockResponse))

      withCaptureOfLoggingFrom(connectorLogger) { events =>
        val result = connector.get(Map("param" -> Seq("value")))

        whenReady(result) { res =>
          res.status mustEqual Status.OK
          res.body mustEqual responseJson.toString
          events.exists(_.getMessage.contains("GET url:")) mustBe true
          events.exists(_.getMessage.contains("status: 200")) mustBe true
        }
      }
    }

    "log the correct POST request and handle server errors" in {
      val requestJson        = Json.obj("data" -> "test")
      val responseJson       = Json.obj("error" -> "internal server error")
      val mockResponse       = HttpResponse(Status.INTERNAL_SERVER_ERROR, responseJson.toString)
      val mockRequestBuilder = mock[RequestBuilder](org.mockito.Answers.RETURNS_SELF)

      when(mockHttpClientV2.post(any[URL])(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(using any(), any())).thenReturn(Future.successful(mockResponse))

      withCaptureOfLoggingFrom(connectorLogger) { events =>

        val result = connector.post(requestJson)

        whenReady(result) { res =>
          res.status mustEqual Status.INTERNAL_SERVER_ERROR
          res.body mustEqual responseJson.toString
          events.exists(_.getMessage.contains("POST Url:")) mustBe true
          events.exists(_.getMessage.contains("status: 500")) mustBe true
        }
      }
    }

    "log the correct PUT request and handle warnings" in {
      val requestJson        = Json.obj("data" -> "test")
      val responseJson       = Json.obj("message" -> "bad request")
      val mockResponse       = HttpResponse(Status.BAD_REQUEST, responseJson.toString)
      val mockRequestBuilder = mock[RequestBuilder](org.mockito.Answers.RETURNS_SELF)

      when(mockHttpClientV2.put(any[URL])(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(using any(), any())).thenReturn(Future.successful(mockResponse))

      withCaptureOfLoggingFrom(connectorLogger) { events =>
        val result = connector.put(requestJson)

        whenReady(result) { res =>
          res.status mustEqual Status.BAD_REQUEST
          res.body mustEqual responseJson.toString

          events.exists(_.getMessage.contains("PUT Url:")) mustBe true
          events.exists(_.getMessage.contains("status: 400")) mustBe true
        }
      }
    }

    "invokes generateHeaders headers" in {
      connector.generateHeaders
      verify(mockHeaderGenerator).generate(any)
    }
  }
}
