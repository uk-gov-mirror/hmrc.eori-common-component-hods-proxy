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
import uk.gov.hmrc.customs.hodsproxy.connectors.{RegisterWithEoriAndIdConnector, _}
import uk.gov.hmrc.customs.hodsproxy.metrics.MetricsEnum
import util.Injector

class ConnectorsSpec extends BaseSpec with Injector {

  "Connectors" must {

    "be using correct service name and metricsId for RegisterWithoutIdConnector" in {

      val registerWithoutIdConnector = instanceOf[RegisterWithoutIdConnector]

      registerWithoutIdConnector.serviceName should be("register-without-id")
      registerWithoutIdConnector.metricsId should be(MetricsEnum.REGISTER_WITHOUT_ID)
    }

    "be using correct service name and metricsId for SubscribeConnector" in {

      val subscriptionConnector = instanceOf[SubscriptionConnector]

      subscriptionConnector.serviceName should be("subscription-service")
      subscriptionConnector.metricsId should be(MetricsEnum.SUBSCRIBE)
    }

    "be using correct service name and metricsId for RegisterWithIdConnector" in {

      val registerWithIdConnector = instanceOf[RegisterWithIdConnector]

      registerWithIdConnector.serviceName should be("register-with-id")
      registerWithIdConnector.metricsId should be(MetricsEnum.REGISTER_WITH_ID_MATCH)
    }

    "be using correct service name and metricsId for RegisterWithEoriAndIdConnector" in {

      val registerWithEoriAndIdConnector = instanceOf[RegisterWithEoriAndIdConnector]

      registerWithEoriAndIdConnector.serviceName should be("register-with-eori-and-id")
      registerWithEoriAndIdConnector.metricsId should be(MetricsEnum.REGISTER_WITH_EORI_AND_ID)
    }

    "be using correct service name and metricsId for SubscriptionStatusConnector" in {

      val subscriptionStatusConnector = instanceOf[SubscriptionStatusConnector]

      subscriptionStatusConnector.serviceName should be("subscription-status")
      subscriptionStatusConnector.metricsId should be(MetricsEnum.SUBSCRIPTION_STATUS)
    }

    "be using correct service name and metricsId for VatKnownFactsControlListConnector" in {

      val vatKnownFactsControlListConnector = instanceOf[VatKnownFactsControlListConnector]

      vatKnownFactsControlListConnector.serviceName should be("vat-known-facts-control-list")
      vatKnownFactsControlListConnector.metricsId should be(MetricsEnum.VAT_KNOWN_FACTS_CONTROL_LIST)
    }

    "be using correct service name and metricsId for SubscriptionDisplayConnector" in {

      val subscriptionDisplayConnector = instanceOf[SubscriptionDisplayConnector]

      subscriptionDisplayConnector.serviceName should be("subscription-display")
      subscriptionDisplayConnector.metricsId should be(MetricsEnum.SUBSCRIPTION_DISPLAY)
    }

    "be using correct service name and metricsId for RegistrationDisplayConnector" in {

      val registrationDisplayConnector = instanceOf[RegistrationDisplayConnector]

      registrationDisplayConnector.serviceName should be("registration-display")
      registrationDisplayConnector.metricsId should be(MetricsEnum.REGISTRATION_DISPLAY)
    }
  }
}
