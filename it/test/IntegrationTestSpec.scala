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

import base.BaseSpec
import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import util.WireMockRunner
import play.api.test.Helpers._
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.inject.bind

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.global

trait IntegrationTestSpec
  extends BaseSpec with ScalaFutures with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneAppPerSuite
    with WireMockRunner with MockitoSugar {

  SharedMetricRegistries.clear()

  val appConfig: Map[String, String] =
    Map("microservice.services.auth.host" -> Host, "microservice.services.auth.port" -> Port.toString)

  implicit lazy val cc  = stubControllerComponents()
  val mockStubBehaviour = mock[StubBehaviour]

  def expectedPredicate(location: String): Predicate.Permission = Predicate.Permission(
    Resource(ResourceType("eori-common-component-hods-proxy"), ResourceLocation(location)),
    IAAction("WRITE")
  )

  when(mockStubBehaviour.stubAuth(Some(expectedPredicate("get")), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  when(mockStubBehaviour.stubAuth(Some(expectedPredicate("post")), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  when(mockStubBehaviour.stubAuth(Some(expectedPredicate("put")), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  when(mockStubBehaviour.stubAuth(Some(expectedPredicate("vat")), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[BackendAuthComponents].toInstance(BackendAuthComponentsStub(mockStubBehaviour)(cc, global)))
    .configure(appConfig).build()

  override def beforeAll(): Unit = startMockServer()

  override protected def beforeEach(): Unit = resetMockServer()

  override def afterAll(): Unit = stopMockServer()
}