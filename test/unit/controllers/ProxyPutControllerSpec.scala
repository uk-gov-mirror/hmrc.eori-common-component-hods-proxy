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

package unit.controllers

import base.BaseSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors.ProxyConnector
import uk.gov.hmrc.customs.hodsproxy.controllers.ProxyPutController
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

import scala.concurrent.Future

class ProxyPutControllerSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val cc: ControllerComponents              = stubControllerComponents()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val mockConnector: ProxyConnector = mock[ProxyConnector]
  private val mockStubBehaviour             = mock[StubBehaviour]

  private val expectedPredicate = Predicate.Permission(
    Resource(ResourceType("eori-common-component-hods-proxy"), ResourceLocation("put")),
    IAAction("WRITE")
  )

  val controller =
    new ProxyPutController(mockConnector, BackendAuthComponentsStub(mockStubBehaviour), stubControllerComponents())

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockStubBehaviour.stubAuth(Some(expectedPredicate), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  }

  override protected def afterEach(): Unit = {
    reset(mockConnector)
    reset(mockStubBehaviour)

    super.afterEach()
  }

  val requestJson: JsValue  = Json.parse(""" { "request": "JSON" } """)
  val responseJson: JsValue = Json.parse(""" { "response": "JSON" } """)

  private def fakeRequest(): FakeRequest[AnyContentAsJson] =
    FakeRequest().withJsonBody(requestJson).withHeaders("Authorization" -> "Token some-token")

  "ProxyPutController put call" must {

    "return bad request for empty request payload" in {

      val result = controller.put()(FakeRequest().withHeaders("Authorization" -> "Token some-token"))

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) shouldBe ""
    }

    "return OK when receive OK(200)" in {
      givenConnectorReturns(OK)

      val result = controller.put()(fakeRequest())

      status(result) shouldBe OK
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT FOUND when receive NOT_FOUND(404)" in {
      givenConnectorReturns(NOT_FOUND)

      val result = controller.put()(fakeRequest())

      status(result) shouldBe NOT_FOUND
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe responseJson
    }

    "return BAD_REQUEST when receive BAD_REQUEST(400)" in {
      givenConnectorReturns(BAD_REQUEST)

      val result = controller.put()(fakeRequest())

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe responseJson
    }

    "internal server error, return internal server error" in {
      givenConnectorReturns(INTERNAL_SERVER_ERROR)

      val result = controller.put()(fakeRequest())

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe responseJson
    }
  }

  def givenConnectorReturns(status: Int): Unit =
    when(mockConnector.put(ArgumentMatchers.eq(requestJson))).thenReturn(
      Future.successful(HttpResponse(status = status, json = responseJson, headers = Map.empty))
    )

}
