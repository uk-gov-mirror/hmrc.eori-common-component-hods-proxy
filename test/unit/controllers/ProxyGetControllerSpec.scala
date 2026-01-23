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
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.hodsproxy.connectors.ProxyConnector
import uk.gov.hmrc.customs.hodsproxy.controllers.ProxyGetController
import uk.gov.hmrc.http.HttpResponse
import play.api.test.Helpers._
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

import scala.concurrent.Future

class ProxyGetControllerSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val cc: ControllerComponents              = stubControllerComponents()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val mockConnector: ProxyConnector = mock[ProxyConnector]
  private val mockStubBehaviour             = mock[StubBehaviour]

  private val expectedPredicate = Predicate.Permission(
    Resource(ResourceType("eori-common-component-hods-proxy"), ResourceLocation("get")),
    IAAction("WRITE")
  )

  private val controller =
    new ProxyGetController(mockConnector, BackendAuthComponentsStub(mockStubBehaviour), stubControllerComponents())

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockStubBehaviour.stubAuth(Some(expectedPredicate), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  }

  override protected def afterEach(): Unit = {
    reset(mockConnector)
    reset(mockStubBehaviour)

    super.afterEach()
  }

  private val someJson    = Json.parse(""" { "response": "JSON" } """)
  private val queryParams = Map("some" -> List("JSON"))

  private val fakeRequest =
    FakeRequest(method = "GET", path = "/someRootPath?some=JSON").withHeaders("Authorization" -> "Token some-token")

  "ProxyGetController get call" must {

    "return OK when receive OK(200)" in {

      givenConnectorReturns(OK)

      val result = controller.get()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe someJson
    }

    "return NOT FOUND when receive NOT_FOUND(404)" in {

      givenConnectorReturns(NOT_FOUND)

      val result = controller.get()(fakeRequest)

      status(result) shouldBe NOT_FOUND
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe someJson
    }

    "return BAD_REQUEST when receive BAD_REQUEST(400)" in {

      givenConnectorReturns(BAD_REQUEST)

      val result = controller.get()(fakeRequest)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe someJson
    }

    "internal server error, return internal server error" in {

      givenConnectorReturns(INTERNAL_SERVER_ERROR)

      val result = controller.get()(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe someJson
    }

    def givenConnectorReturns(status: Int): Unit =
      when(mockConnector.get(ArgumentMatchers.eq(queryParams))).thenReturn(
        Future.successful(HttpResponse(status = status, json = someJson, headers = Map.empty))
      )
  }
}
