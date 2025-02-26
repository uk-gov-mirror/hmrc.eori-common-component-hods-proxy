/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum._
import uk.gov.hmrc.customs.hodsproxy.metrics.{CdsMetrics, MetricsEnum}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RegisterWithoutIdConnector @Inject() (
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
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
  http: HttpClient,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends ProxyConnector(http, config, metrics, headerGenerator) {

  override val serviceName: String    = "register-subscribe-without-id"
  override val metricsId: MetricsEnum = MetricsEnum.REGISTER_WITHOUT_ID
}
