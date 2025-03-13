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

package uk.gov.hmrc.customs.hodsproxy.controllers

import play.api.http.MimeTypes
import play.api.mvc.{Action, AnyContent, ControllerComponents, Results}
import uk.gov.hmrc.customs.hodsproxy.connectors._
import uk.gov.hmrc.customs.managesubscription.controllers.Permissions.internalAuthPermission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class ProxyPutController(val connector: ProxyConnector, val auth: BackendAuthComponents, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BackendController(cc) {

  def put(): Action[AnyContent] = auth.authorizedAction(internalAuthPermission("put")).async { implicit request =>
    val json = request.body.asJson
    json match {
      case Some(theRequest) =>
        connector.put(theRequest) map { response =>
          Results.Status(response.status)(response.body).as(MimeTypes.JSON)
        }
      case None => Future.successful(BadRequest)
    }
  }

}

@Singleton
class UpdateVerifiedEmailController @Inject() (
  connector: UpdateVerifiedEmailConnector,
  auth: BackendAuthComponents,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ProxyPutController(connector, auth, cc)
