package com.codex.quota.android.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPolicyTest {
  @Test
  fun defaultPolicyKeepsPhoneUrgencySeparateFromSystemControlledBandBehavior() {
    val policy = NotificationPolicy.default()
    val background =
      AlertContext(
        chatGptFocused = false,
        androidForeground = false,
        reconnect = false,
      )

    assertEquals(
      AlertDelivery(
        phone = true,
        band = true,
        band8 = false,
        phoneUrgency = PhoneUrgency.Silent,
        bandBehavior = BandAlertBehavior.SystemControlled,
      ),
      policy.plan(TaskState.WaitingForReview, background),
    )
    assertEquals(
      AlertDelivery(
        phone = true,
        band = true,
        band8 = false,
        phoneUrgency = PhoneUrgency.Vibrate,
        bandBehavior = BandAlertBehavior.SystemControlled,
      ),
      policy.plan(TaskState.NeedsAuthorization, background),
    )
  }

  @Test
  fun band8CompatibilityIsAnIndependentDeliveryChannel() {
    val policy =
      NotificationPolicy.create(
        mode = NotificationMode.Always,
        waitingForReview = true,
        needsAuthorization = true,
        phone = false,
        band = false,
        band8 = true,
      )

    assertEquals(
      AlertDelivery(
        phone = false,
        band = false,
        band8 = true,
        phoneUrgency = null,
        bandBehavior = null,
      ),
      policy.plan(
        TaskState.WaitingForReview,
        AlertContext(
          chatGptFocused = false,
          androidForeground = false,
          reconnect = false,
        ),
      ),
    )
  }
}
