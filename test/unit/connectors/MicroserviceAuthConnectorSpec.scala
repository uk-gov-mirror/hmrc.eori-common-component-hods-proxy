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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.MissingBearerToken
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.customs.hodsproxy.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MicroserviceAuthConnectorSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  var mockHttpClientV2: HttpClientV2           = mock[HttpClientV2]
  var mockServicesConfig: ServicesConfig       = mock[ServicesConfig]
  var authConnector: MicroserviceAuthConnector = mock[MicroserviceAuthConnector]

  override def beforeEach(): Unit = {
    mockHttpClientV2 = mock[HttpClientV2]
    mockServicesConfig = mock[ServicesConfig]
    when(mockServicesConfig.baseUrl("auth")).thenReturn("http://localhost:9000")

    authConnector = new MicroserviceAuthConnector(mockServicesConfig, mockHttpClientV2)
  }

  "MicroserviceAuthConnector" must {

    "handle a successful authorization response" in {
      val mockHttpResponse   = HttpResponse(200, body = "valid response")
      val mockRequestBuilder = mock[RequestBuilder](org.mockito.Answers.RETURNS_SELF)
      val predicate          = mock[Predicate]
      val retrieval          = uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId

      implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer test-token")))

      when(mockHttpClientV2.post(any[URL])(using any[HeaderCarrier]))
        .thenReturn(mockRequestBuilder)

      when(mockRequestBuilder.execute(using any(), any()))
        .thenReturn(Future.successful(mockHttpResponse))

      val result: Future[Option[String]] = authConnector.authorise(predicate, retrieval)

      // Assert: Ensure the result is a successful response with the expected body
      result.onComplete {
        case scala.util.Success(res) =>
          res.get shouldBe "valid response"
        case scala.util.Failure(exception) =>
          fail(s"Expected success but got failure: $exception")
      }
    }

    "return a failed Future when authorization header is missing" in {
      val predicate = mock[Predicate]
      val retrieval = mock[Retrieval[String]]

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val result = authConnector.authorise(predicate, retrieval)

      // Assert: Ensure the result is a failure with the expected error
      recoverToExceptionIf[MissingBearerToken] {
        result
      } map { ex =>
        ex.getMessage should include("MissingBearerToken")
      }
    }

    "handle a failed authorization with 401 response" in {
      val mockHttpResponse   = HttpResponse(401, body = "Unauthorized")
      val mockRequestBuilder = mock[RequestBuilder]
      val predicate          = mock[Predicate]
      val retrieval          = mock[Retrieval[String]]

      implicit val hc: HeaderCarrier = HeaderCarrier()

      when(mockHttpClientV2.post(any[URL])(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(using any(), any())).thenReturn(Future.successful(mockHttpResponse))

      val result = authConnector.authorise(predicate, retrieval)

      // Assert: Ensure the result is a failure with the expected error
      recoverToExceptionIf[UpstreamErrorResponse] {
        result
      } map { ex =>
        ex.getMessage should include("Unauthorized")
      }
    }
  }

}
