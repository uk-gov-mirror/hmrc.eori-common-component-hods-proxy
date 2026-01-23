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

package uk.gov.hmrc.customs.hodsproxy.controllers

import play.api.http.MimeTypes
import play.api.mvc._
import uk.gov.hmrc.customs.hodsproxy.connectors._
import uk.gov.hmrc.customs.managesubscription.controllers.Permissions.internalAuthPermission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

class ProxyGetController(val connector: ProxyConnector, val auth: BackendAuthComponents, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BackendController(cc) {

  def get(): Action[AnyContent] = auth.authorizedAction(internalAuthPermission("get")).async { implicit request =>
    connector.get(request.queryString) map {
      response =>
        Results.Status(response.status)(response.body).as(MimeTypes.JSON)
    }
  }

}

@Singleton
class SubscriptionStatusController @Inject() (
  connector: SubscriptionStatusConnector,
  auth: BackendAuthComponents,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ProxyGetController(connector, auth, cc)

@Singleton
class SubscriptionDisplayController @Inject() (
  connector: SubscriptionDisplayConnector,
  auth: BackendAuthComponents,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ProxyGetController(connector, auth, cc)

@Singleton
class VatKnownFactsControlListController @Inject() (
  override val connector: VatKnownFactsControlListConnector,
  auth: BackendAuthComponents,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ProxyGetController(connector, auth, cc) {

  override def get(): Action[AnyContent] = auth.authorizedAction(internalAuthPermission("vat")).async {
    implicit request =>
      val vrn = request.getQueryString("vrn").getOrElse(throw new IllegalStateException("VRN is not in the request"))
      connector.get(vrn) map {
        response =>
          Results.Status(response.status)(response.body).as(MimeTypes.JSON)
      }
  }

}
