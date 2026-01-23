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
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors.SubscriptionStatusConnector
import uk.gov.hmrc.customs.hodsproxy.controllers.SubscriptionStatusController
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

import scala.concurrent.Future

class SubscriptionStatusControllerSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val cc: ControllerComponents              = stubControllerComponents()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val mockConnector = mock[SubscriptionStatusConnector]

  private val mockStubBehaviour = mock[StubBehaviour]

  private val expectedPredicate = Predicate.Permission(
    Resource(ResourceType("eori-common-component-hods-proxy"), ResourceLocation("get")),
    IAAction("WRITE")
  )

  private val controller =
    new SubscriptionStatusController(
      mockConnector,
      BackendAuthComponentsStub(mockStubBehaviour),
      stubControllerComponents()
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockStubBehaviour.stubAuth(Some(expectedPredicate), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  }

  override protected def afterEach(): Unit = {
    reset(mockConnector)
    reset(mockStubBehaviour)
    super.afterEach()
  }

  private val someJson = Json.parse(""" {
                                                   |  "subscriptionStatusResponse": {
                                                   |    "responseCommon": {
                                                   |      "status": "OK",
                                                   |      "processingDate": "2016-03-17T09:30:47Z"
                                                   |    },
                                                   |    "responseDetail": {
                                                   |      "subscriptionStatus": "00"
                                                   |    }
                                                   |  }
                                                   |} """.stripMargin)

  private val fakeRequest = FakeRequest(
    method = "GET",
    path = "/subscription-status?receiptDate=2022-03-03T11%3A36%3A27Z&regime=CDS&SAFE=someSAFEID"
  ).withHeaders("Authorization" -> "Token some-token")

  "SubscriptionStatusController get call" must {

    "return connector status when vrn is provided in query param" in {

      givenConnectorReturns(OK)

      val result = controller.get()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe someJson
    }
  }

  def givenConnectorReturns(status: Int): Unit =
    when(
      mockConnector.get(
        Map("receiptDate" -> List("2022-03-03T11:36:27Z"), "regime" -> List("CDS"), "SAFE" -> List("someSAFEID"))
      )
    ).thenReturn(Future.successful(HttpResponse(status = status, json = someJson, headers = Map.empty)))

}
