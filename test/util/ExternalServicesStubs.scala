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

package util

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPattern
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON

trait ExternalServicesStubs {

  val RegisterWithEoriAndIdServiceContext = "registrations/registerwitheoriandid/v1"
  val RegisterWithEoriAndIdServiceUrl     = urlMatching("/" + RegisterWithEoriAndIdServiceContext)

  val RegWithIdServiceContext = "registrations/registerwithid/1.0.0"
  val RegWithIdServiceUrl     = urlMatching("/" + RegWithIdServiceContext)

  val RegWithoutIdServiceContext = "registrations/registerwithoutid/v1"
  val RegWithoutIdServiceUrl     = urlMatching("/" + RegWithoutIdServiceContext)

  val RegSubWithoutIdServiceContext = "taxud/txe13/eori/subscription/v1"
  val RegSubWithoutIdServiceUrl     = urlMatching("/" + RegSubWithoutIdServiceContext)

  val UpdateVerifiedEmailContext = "subscriptions/updateverifiedemail/v1"
  val UpdateVerifiedEmailUrl     = urlMatching("/" + UpdateVerifiedEmailContext)

  val authServiceUrl     = "auth/authorise"
  val defaultBearerToken = "AUTHORISED_BEARER_TOKEN"

  val SubscriptionStatusServiceContext = "subscriptions/subscriptionstatus/v1"

  val SubscriptionDisplayContext = "subscriptions/subscriptiondisplay/v1"

  val SubscribeServiceContext = "subscriptions/subscriptioncreate/v1"
  val SubscribeServiceUrl     = urlMatching("/" + SubscribeServiceContext)

  val VatKnownFactsContext = "vat/known-facts/control-list"

  def setRegisterWithIdToReturnTheResponse(response: String, status: Int): Unit =
    stubPostCall(RegWithIdServiceUrl, response, status)

  def setRegisterWithEoriAndIdToReturnTheResponse(response: String, status: Int): Unit =
    stubPostCall(RegisterWithEoriAndIdServiceUrl, response, status)

  def setRegWithoutIdToReturnTheResponse(response: String, status: Int): Unit =
    stubPostCall(RegWithoutIdServiceUrl, response, status)

  def setRegSubWithoutIdToReturnTheResponse(response: String, status: Int): Unit =
    stubPostCall(RegSubWithoutIdServiceUrl, response, status)

  def stubUpdateVerifiedEmailResponse(response: String, status: Int): Unit =
    stubPutCall(UpdateVerifiedEmailUrl, response, status)

  def setSubscriptionStatusToReturnTheResponse(queryParams: String, response: String, status: Int): Unit =
    stubGetCall(queryParams, SubscriptionStatusServiceContext, response, status)

  def setSubscriptionDisplayToReturnTheResponse(queryParams: String, response: String, status: Int): Unit =
    stubGetCall(queryParams, SubscriptionDisplayContext, response, status)

  def setSubscribeToReturnTheResponse(response: String, status: Int): Unit =
    stubPostCall(SubscribeServiceUrl, response, status)

  def setVatKnownFactsToReturnTheResponse(vrn: String, response: String, status: Int): Unit =
    stubGetCall(s"/$vrn", VatKnownFactsContext, response, status)

  def stubBearerTokenAuth(bearerToken: String = defaultBearerToken): Unit =
    stubFor(
      post(urlEqualTo(authServiceUrl))
        .withHeader(AUTHORIZATION, equalTo(s"Bearer $bearerToken"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("{}")
        )
    )

  def verifyBearerTokenAuthorised(bearerToken: String = defaultBearerToken): Unit =
    verify(
      postRequestedFor(urlEqualTo(authServiceUrl))
        .withHeader(AUTHORIZATION, equalTo(s"Bearer $bearerToken"))
    )

  private def stubPostCall(url: UrlPattern, response: String, status: Int): Unit =
    stubFor(
      post(url)
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

  private def stubGetCall(queryParams: String, context: String, response: String, status: Int): Unit =
    stubFor(
      get(urlEqualTo("/" + context + queryParams))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

  private def stubPutCall(url: UrlPattern, response: String, status: Int): Unit =
    stubFor(
      put(url)
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

}
