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

package unit.connectors

import java.time.{Clock, Instant, ZoneId}

import base.BaseSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import uk.gov.hmrc.customs.hodsproxy.connectors.HeaderGenerator

class HeaderGeneratorSpec extends BaseSpec with MockitoSugar {
  val authToken         = "someToken"
  val in: Instant       = Instant.parse("2015-08-13T13:28:22Z")
  val fixedClock: Clock = Clock.fixed(in, ZoneId.systemDefault())

  object testHeaderGenerator extends HeaderGenerator {
    override val clock: Clock = fixedClock
  }

  "HeadersCreator createHeaders" should {
    val headers = testHeaderGenerator.generate(authToken).toMap

    "create correct X-Correlation-ID,X_FORWARDED_HOST,CONTENT_TYPE,ACCEPT and Date(latest) headers" in {
      headers("X-Correlation-ID") should not be empty
      headers(X_FORWARDED_HOST) should be("MDTP")
      headers(CONTENT_TYPE) should be(MimeTypes.JSON)
      headers(ACCEPT) should be(MimeTypes.JSON)
      headers(AUTHORIZATION) should be(s"Bearer $authToken")
      headers(DATE) should be("Thu, 13 Aug 2015 13:28:22 GMT")
    }

    "create correct Headers for MDG get request X-Correlation-ID,X_FORWARDED_HOST,ACCEPT and Date(latest) headers" in {
      val headers           = testHeaderGenerator.headersForMDGGet(authToken).toMap
      val contentTypeFilter = CONTENT_TYPE -> MimeTypes.JSON

      headers("X-Correlation-ID") should not be empty
      headers(X_FORWARDED_HOST) should be("MDTP")
      headers(ACCEPT) should be(MimeTypes.JSON)
      headers(AUTHORIZATION) should be(s"Bearer $authToken")
      headers(DATE) should be("Thu, 13 Aug 2015 13:28:22 GMT")
      headers.find(_ == contentTypeFilter) shouldBe None
    }

    "create correct Headers for post for MDG request X-Correlation-ID,X_FORWARDED_HOST,ACCEPT and Date(latest) headers" in {
      val headers           = testHeaderGenerator.headersForMDG(authToken).toMap
      val contentTypeFilter = CONTENT_TYPE -> MimeTypes.JSON
      headers("X-Correlation-ID") should not be empty
      headers(X_FORWARDED_HOST) should be("MDTP")
      headers(ACCEPT) should be(MimeTypes.JSON)
      headers(AUTHORIZATION) should be(s"Bearer $authToken")
      headers(DATE) should be("Thu, 13 Aug 2015 13:28:22 GMT")
      headers.find(_ == contentTypeFilter) shouldBe Some(contentTypeFilter)
    }

    "create correct Headers for DES get request" in {
      val headers = testHeaderGenerator.headersForDESGet(authToken, "ist0").toMap
      headers("Environment") should be("ist0")
      headers(AUTHORIZATION) should be(s"Bearer $authToken")
    }

  }
}
