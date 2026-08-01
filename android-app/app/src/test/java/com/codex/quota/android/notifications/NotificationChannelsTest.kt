package com.codex.quota.android.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationChannelsTest {
  @Test
  fun bothTaskChannelsRequestHeadsUpWhileOnlyAuthorizationVibrates() {
    val authorization =
      NotificationChannels.specs.single {
        it.id == NotificationChannels.NEEDS_AUTHORIZATION_CHANNEL_ID
      }
    val waiting =
      NotificationChannels.specs.single {
        it.id == NotificationChannels.WAITING_FOR_REVIEW_CHANNEL_ID
      }

    assertEquals(ChannelImportance.High, authorization.importance)
    assertTrue(authorization.vibrate)
    assertEquals(ChannelImportance.High, waiting.importance)
    assertFalse(waiting.vibrate)
  }

  @Test
  fun headsUpUpgradeUsesFreshChannelIdentifiers() {
    assertEquals(
      listOf("needs-authorization-v2", "waiting-for-review-v2", "band8-quota-status-v2"),
      NotificationChannels.specs.map(NotificationChannelSpec::id),
    )
  }

  @Test
  fun band8QuotaChannelUsesDefaultImportanceWithoutPhoneVibration() {
    val quota =
      NotificationChannels.specs.single {
        it.id == NotificationChannels.BAND8_QUOTA_STATUS_CHANNEL_ID
      }

    assertEquals(ChannelImportance.Default, quota.importance)
    assertFalse(quota.vibrate)
  }
}
