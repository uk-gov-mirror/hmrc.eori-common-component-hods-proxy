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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

trait WireMockRunner {

  lazy val wireMockServer: WireMockServer = {
    val server = new WireMockServer(wireMockConfig().dynamicPort())
    server.start()
    server
  }

  lazy val Port: Int    = wireMockServer.port()
  lazy val Host: String = "localhost"

  def startMockServer(): Unit = {
    if (!wireMockServer.isRunning) wireMockServer.start()
    WireMock.configureFor(Host, Port)
  }

  def resetMockServer(): Unit =
    wireMockServer.resetAll()

  def stopMockServer(): Unit =
    wireMockServer.stop()

}
