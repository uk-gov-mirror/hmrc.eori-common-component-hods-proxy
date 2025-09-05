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
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.customs.hodsproxy.connectors.HeaderGenerator.X_CORRELATION_ID
import uk.gov.hmrc.customs.hodsproxy.metrics.CdsMetrics
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URI
import javax.inject.Inject
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

abstract class ProxyConnector @Inject() (
  http: HttpClientV2,
  config: ServicesConfig,
  metrics: CdsMetrics,
  headerGenerator: HeaderGenerator
)(implicit ec: ExecutionContext)
    extends RawResponseReads {

  private val logger = Logger(this.getClass)

  val serviceName: String
  val metricsId: MetricsEnum

  def baseUrl(serviceName: String): String =
    config.baseUrl(serviceName) + config.getString(s"microservice.services.$serviceName.context")

  lazy val bearerToken: String = config.getString(s"microservice.services.$serviceName.bearer-token")

  def get(queryParams: Map[String, Seq[String]]): Future[HttpResponse] = {
    val params: immutable.Iterable[(String, String)] = queryParams.flatten[(String, String)](a => a._2.map((a._1, _)))
    val url                                          = baseUrl(serviceName) + "?" + params.map(a => a._1 + "=" + a._2).mkString("&")

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = headerGenerator.headersForMDGGet(bearerToken))
    // $COVERAGE-OFF$Loggers
    logger.info(s"[$serviceName][Connector] GET url: $url")
    // $COVERAGE-ON

    makeRequest(http.get(URI.create(url).toURL).execute)
  }

  def post(requestData: JsValue): Future[HttpResponse] = {
    val url = baseUrl(serviceName)

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = headerGenerator.headersForMDG(bearerToken))
    // $COVERAGE-OFF$Loggers
    logger.info(
      s"[$serviceName][Connector] POST Url: $url Correlation ID: ${hc.extraHeaders.find(_._1 == X_CORRELATION_ID)}"
    )
    // $COVERAGE-ON

    makeRequest(http.post(URI.create(url).toURL).withBody(Json.toJson(requestData)).execute[HttpResponse])
  }

  def put(requestData: JsValue): Future[HttpResponse] = {
    val url = baseUrl(serviceName)

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = headerGenerator.headersForMDG(bearerToken))
    // $COVERAGE-OFF$Loggers
    logger.info(s"[$serviceName][Connector] PUT Url: $url")
    // $COVERAGE-ON

    makeRequest(http.put(URI.create(url).toURL).withBody(requestData).execute[HttpResponse])
  }

  protected def generateHeaders: Seq[(String, String)] = headerGenerator.generate(bearerToken)

  protected[ProxyConnector] def makeRequest(httpRequest: => Future[HttpResponse]): Future[HttpResponse] = {
    val timerContext = metrics.startTimer(metricsId)
    httpRequest map { response =>
      timerContext.stop()
      logger.info(s"[$serviceName][Connector] - status: ${response.status}")
      response.status match {
        case Status.OK =>
          metrics.incrementSuccessCounter(metricsId)
          response
        case status if Status.isServerError(status) =>
          logger.error(s"[$serviceName][Connector] - status: ${response.status}")
          metrics.incrementFailedCounter(metricsId)
          response
        case _ =>
          logger.warn(s"[$serviceName][Connector] - status: ${response.status}")
          metrics.incrementFailedCounter(metricsId)
          response
      }
    }
  }

}
