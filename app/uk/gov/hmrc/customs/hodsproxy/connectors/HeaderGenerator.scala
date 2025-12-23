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

import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import uk.gov.hmrc.customs.hodsproxy.connectors.HeaderGenerator.X_CORRELATION_ID

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import java.util.UUID
import javax.inject.Singleton

@Singleton
class HeaderGenerator {

  val clock: Clock = Clock.systemDefaultZone()

  def generate(bearerToken: String): Seq[(String, String)] =
    Seq(
      DATE -> DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock.withZone(ZoneId.of("GMT")))),
      X_CORRELATION_ID -> UUID.randomUUID().toString,
      X_FORWARDED_HOST -> "MDTP",
      CONTENT_TYPE     -> MimeTypes.JSON,
      ACCEPT           -> MimeTypes.JSON,
      AUTHORIZATION    -> s"Bearer $bearerToken"
    )

  def headersForMDG(bearerToken: String): Seq[(String, String)] =
    generate(bearerToken)

  def headersForMDGGet(bearerToken: String): Seq[(String, String)] = {
    val contentTypeFilter = CONTENT_TYPE -> MimeTypes.JSON
    headersForMDG(bearerToken).filter(_ != contentTypeFilter)
  }

  def headersForDESGet(bearerToken: String, environment: String): Seq[(String, String)] =
    Seq(AUTHORIZATION -> s"Bearer $bearerToken", "Environment" -> environment)

}

object HeaderGenerator {
  val X_CORRELATION_ID = "X-Correlation-ID"
}
