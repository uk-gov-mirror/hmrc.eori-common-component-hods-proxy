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

package uk.gov.hmrc.customs.hodsproxy.connectors

import play.api.Logger
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE, DATE, X_FORWARDED_HOST}
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.customs.hodsproxy.connectors.HeaderGenerator.X_CORRELATION_ID
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum._
import uk.gov.hmrc.customs.hodsproxy.metrics.{CdsMetrics, MetricsEnum}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URI
import java.time.{Clock, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterWithoutIdConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "register-without-id"
  override val metricsId: MetricsEnum = MetricsEnum.REGISTER_WITHOUT_ID
}

@Singleton
class SubscriptionConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "subscription-service"
  override val metricsId: MetricsEnum = SUBSCRIBE
}

@Singleton
class RegisterWithIdConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "register-with-id"
  override val metricsId: MetricsEnum = REGISTER_WITH_ID_MATCH
}

@Singleton
class RegisterWithEoriAndIdConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "register-with-eori-and-id"
  override val metricsId: MetricsEnum = REGISTER_WITH_EORI_AND_ID
}

@Singleton
class SubscriptionStatusConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "subscription-status"
  override val metricsId: MetricsEnum = SUBSCRIPTION_STATUS
}

@Singleton
class SubscriptionDisplayConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "subscription-display"
  override val metricsId: MetricsEnum = SUBSCRIPTION_DISPLAY
}

@Singleton
class RegistrationDisplayConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "registration-display"
  override val metricsId: MetricsEnum = REGISTRATION_DISPLAY
}

@Singleton
class UpdateVerifiedEmailConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "update-verified-email"
  override val metricsId: MetricsEnum = UPDATE_VERIFIED_EMAIL
}

@Singleton
class RegisterSubscribeWithoutIdConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  private val logger       = Logger(this.getClass)
  private val clock: Clock = Clock.systemDefaultZone()

  override val serviceName: String    = "register-subscribe-without-id"
  override val metricsId: MetricsEnum = MetricsEnum.REGISTER_WITHOUT_ID

  override def post(requestData: JsValue): Future[HttpResponse] = {
    val url = baseUrl(serviceName)

    val headers = Seq(
      DATE -> DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").format(
        ZonedDateTime.now(clock.withZone(ZoneId.of("GMT")))
      ),
      X_CORRELATION_ID -> UUID.randomUUID().toString,
      X_FORWARDED_HOST -> "MDTP",
      CONTENT_TYPE     -> MimeTypes.JSON,
      ACCEPT           -> MimeTypes.JSON,
      AUTHORIZATION    -> s"Bearer $bearerToken"
    )

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = headers)
    // $COVERAGE-OFF$Loggers
    logger.info(
      s"[$serviceName][Connector] POST Url: $url Correlation ID: ${hc.extraHeaders.find(_._1 == X_CORRELATION_ID)}"
    )
    // $COVERAGE-ON

    makeRequest(http.post(URI.create(url).toURL).withBody(Json.toJson(requestData)).execute[HttpResponse])
  }

}
