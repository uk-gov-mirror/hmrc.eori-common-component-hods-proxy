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

import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.hodsproxy.connectors._
import uk.gov.hmrc.customs.hodsproxy.controllers._
import uk.gov.hmrc.internalauth.client.BackendAuthComponents
import uk.gov.hmrc.internalauth.client.test.BackendAuthComponentsStub
import util.ExternalServicesStubs

import scala.concurrent.ExecutionContext.global

class ControllersSpec extends IntegrationTestSpec with ExternalServicesStubs {

  private def fakeRequest(method: String, uri: String) =
    FakeRequest(method, uri).withHeaders("Authorization" -> s"Bearer $defaultBearerToken")

  override protected def beforeEach(): Unit =
    super.beforeEach()

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[BackendAuthComponents].toInstance(BackendAuthComponentsStub(mockStubBehaviour)(using cc, global)))
    .configure(
      appConfig ++ Seq(
        "microservice.services.vat-known-facts-control-list.port" -> Port,
        "microservice.services.vat-known-facts-control-list.host" -> Host,
        "microservice.services.register-with-eori-and-id.port"    -> Port,
        "microservice.services.register-with-eori-and-id.host"    -> Host,
        "microservice.services.register-with-id.port"             -> Port,
        "microservice.services.register-with-id.host"             -> Host,
        "microservice.services.register-without-id.port"          -> Port,
        "microservice.services.register-without-id.host"          -> Host,
        "microservice.services.subscription-service.port"         -> Port,
        "microservice.services.subscription-service.host"         -> Host,
        "microservice.services.subscription-status.port"          -> Port,
        "microservice.services.subscription-status.host"          -> Host,
        "microservice.services.subscription-display.port"         -> Port,
        "microservice.services.subscription-display.host"         -> Host,
        "microservice.services.update-verified-email.host"        -> Host,
        "microservice.services.update-verified-email.port"        -> Port
      )
    ).build()

  "Controllers are configured correctly and securely in routes" must {

    "RegisterWithoutIdController is configured with correct url and correct connector" in {
      setRegWithoutIdToReturnTheResponse("""{"response":123}""", OK)

      app.injector.instanceOf[RegisterWithoutIdController].connector shouldBe a[RegisterWithoutIdConnector]

      val result = route(
        app,
        fakeRequest("POST", "/register-without-id").withBody("""{"response":123}""").withHeaders(
          ("Content-Type", "application/json")
        )
      ).get

      status(result) should be(OK)
    }

    "RegisterWithIdController is configured with correct url and correct connector" in {
      setRegisterWithIdToReturnTheResponse("""{"response":true}""", OK)

      app.injector.instanceOf[RegisterWithIdController].connector shouldBe a[RegisterWithIdConnector]

      val result = route(
        app,
        fakeRequest("POST", "/register-with-id").withBody("""{"request":true}""").withHeaders(
          ("Content-Type", "application/json")
        )
      ).get

      status(result) should be(OK)
    }

    "RegisterWithEoriAndIdController is configured with correct url and correct connector" in {
      setRegisterWithEoriAndIdToReturnTheResponse("""{"response" :true}""", OK)

      app.injector.instanceOf[RegisterWithEoriAndIdController].connector shouldBe a[RegisterWithEoriAndIdConnector]

      val result = route(
        app,
        fakeRequest("POST", "/register-with-eori-and-id").withBody("""{"request":true}""").withHeaders(
          ("Content-Type", "application/json")
        )
      ).get

      status(result) should be(OK)
    }

    "SubscriptionController is configured with correct url and correct connector" in {
      setSubscribeToReturnTheResponse("""{"response" :true}""", OK)

      app.injector.instanceOf[SubscriptionController].connector shouldBe a[SubscriptionConnector]

      val result = route(
        app,
        fakeRequest("POST", "/subscribe").withBody("""{"request":true}""").withHeaders(
          ("Content-Type", "application/json")
        )
      ).get

      status(result) should be(OK)
    }

    "SubscriptionStatusController is configured with correct connector" in {
      setSubscriptionStatusToReturnTheResponse("", """ {"response":true} """, OK)

      app.injector.instanceOf[SubscriptionStatusController].connector shouldBe a[SubscriptionStatusConnector]

      val result = route(app, fakeRequest("GET", "/subscription-status")).get

      status(result) should be(OK)
    }

    "SubscriptionDisplayController is configured with correct connector" in {
      setSubscriptionDisplayToReturnTheResponse("", """ {"response":true} """, OK)

      app.injector.instanceOf[SubscriptionDisplayController].connector shouldBe a[SubscriptionDisplayConnector]

      val result = route(app, fakeRequest("GET", "/subscription-display")).get

      status(result) should be(OK)
    }

    "VatKnownFactsControlListController is configured with correct connector" in {
      setVatKnownFactsToReturnTheResponse("12345678", """ {"response":true} """, OK)

      app.injector.instanceOf[VatKnownFactsControlListController].connector shouldBe a[
        VatKnownFactsControlListConnector
      ]

      val result = route(app, fakeRequest("GET", "/vat-known-facts-control-list?vrn=12345678")).get

      status(result) should be(OK)
    }

    "UpdateVerifiedEmailController is configured with correct url and correct connector" in {
      stubUpdateVerifiedEmailResponse("""{"response":123}""", OK)

      app.injector.instanceOf[UpdateVerifiedEmailController].connector shouldBe a[UpdateVerifiedEmailConnector]

      val result = route(
        app,
        fakeRequest("PUT", "/update-verified-email").withBody("""{"request":123234342}""").withHeaders(
          ("Content-Type", "application/json")
        )
      ).get

      status(result) should be(OK)
    }
  }
}
