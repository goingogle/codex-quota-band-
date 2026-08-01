package com.codex.quota.android.notifications

import com.codex.quota.android.protocol.ChatGptState
import com.codex.quota.android.ui.AppUiState
import com.codex.quota.android.ui.DeviceConnections
import com.codex.quota.android.ui.DeviceLinkState
import com.codex.quota.android.ui.FiveHourQuotaAvailability
import com.codex.quota.android.ui.SyncState
import com.codex.quota.android.ui.WeeklyQuota
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Band8QuotaNotificationTest {
  @Test
  fun formatsOnlyAvailableQuotaAndResetFacts() {
    val now = Instant.parse("2026-07-31T00:00:00Z").toEpochMilli()
    val state =
      state(
        syncState = SyncState.Synced,
        lastSyncAtMs = now,
        fiveHour = WeeklyQuota(64, Instant.parse("2026-07-31T03:00:00Z").toEpochMilli()),
        weekly = WeeklyQuota(81, Instant.parse("2026-08-01T00:00:00Z").toEpochMilli()),
      )

    val content =
      Band8QuotaNotificationContent.from(
        state = state,
        nowMs = now,
        zoneId = ZoneId.of("UTC"),
      )

    assertEquals("Codex 配额", content.title)
    assertEquals("5小时 64% · 周额度 81%", content.summary)
    assertEquals(
      "5小时 03:00重置 · 周额度 8月1日重置\n已同步 刚刚",
      content.details,
    )
  }

  @Test
  fun missingAndPendingQuotaAreNeverInvented() {
    val now = Instant.parse("2026-07-31T00:00:00Z").toEpochMilli()
    val state =
      state(
        syncState = SyncState.Offline,
        lastSyncAtMs = now - 120_000,
        fiveHour = null,
        weekly = null,
        fiveHourAvailability = FiveHourQuotaAvailability.Pending,
      )

    val content = Band8QuotaNotificationContent.from(state, now, ZoneId.of("UTC"))

    assertEquals("5小时 待同步 · 周额度 暂无数据", content.summary)
    assertEquals("离线 2分", content.details)
  }

  @Test
  fun manualPayloadCanShowAnExplicitCachedSnapshotWithoutEnablingAutomaticPush() {
    val now = Instant.parse("2026-07-31T00:00:00Z").toEpochMilli()
    val state =
      state(
        syncState = SyncState.Cached,
        lastSyncAtMs = now - 60_000,
        weekly = WeeklyQuota(42, Instant.parse("2026-08-01T00:00:00Z").toEpochMilli()),
      )

    assertNull(Band8QuotaSignal.from(state))
    assertEquals(
      "5小时 暂无数据 · 周额度 42%",
      Band8QuotaNotificationContent.from(state, now, ZoneId.of("UTC")).summary,
    )
  }

  @Test
  fun autoSignalRequiresCurrentlySyncedQuota() {
    assertNull(Band8QuotaSignal.from(state(syncState = SyncState.Cached, weekly = WeeklyQuota(50, 2))))
    assertNull(Band8QuotaSignal.from(state(syncState = SyncState.Synced)))
    assertNull(
      Band8QuotaSignal.from(
        state(
          syncState = SyncState.Synced,
          fiveHour = WeeklyQuota(50, 2),
          fiveHourAvailability = FiveHourQuotaAvailability.Pending,
        ),
      ),
    )
    assertEquals(
      Band8QuotaSignal(fiveHourPercent = null, fiveHourResetsAtMs = null, weeklyPercent = 50, weeklyResetsAtMs = 2),
      Band8QuotaSignal.from(state(syncState = SyncState.Synced, weekly = WeeklyQuota(50, 2))),
    )
  }

  @Test
  fun automaticUpdatesOnlyFireForFirstDataThresholdCrossingsAndNewResetCycles() {
    val initial = Band8QuotaSignal(80, 1_000, 90, 2_000)
    val sameBand = initial.copy(fiveHourPercent = 79, weeklyPercent = 88)
    val crossed75 = sameBand.copy(fiveHourPercent = 74)
    val crossed50 = crossed75.copy(fiveHourPercent = 50)
    val replenishedWithoutReset = crossed50.copy(fiveHourPercent = 90)
    val newCycle = replenishedWithoutReset.copy(fiveHourResetsAtMs = 3_000)

    assertTrue(Band8QuotaAutoPolicy.shouldNotify(previous = null, current = initial))
    assertFalse(Band8QuotaAutoPolicy.shouldNotify(previous = initial, current = sameBand))
    assertTrue(Band8QuotaAutoPolicy.shouldNotify(previous = sameBand, current = crossed75))
    assertTrue(Band8QuotaAutoPolicy.shouldNotify(previous = crossed75, current = crossed50))
    assertFalse(
      Band8QuotaAutoPolicy.shouldNotify(
        previous = crossed50,
        current = replenishedWithoutReset,
      ),
    )
    assertTrue(
      Band8QuotaAutoPolicy.shouldNotify(
        previous = replenishedWithoutReset,
        current = newCycle,
      ),
    )
  }

  @Test
  fun everyConfiguredDownwardThresholdTriggers() {
    listOf(75, 50, 25, 10).forEach { threshold ->
      val previous = Band8QuotaSignal(threshold + 1, 1_000, null, null)
      val current = previous.copy(fiveHourPercent = threshold)

      assertTrue(
        "threshold $threshold should trigger",
        Band8QuotaAutoPolicy.shouldNotify(previous, current),
      )
    }
  }

  @Test
  fun coordinatorSuppressesRepeatedSnapshotsAndStartsWhenCompatibilityIsEnabled() {
    val published = mutableListOf<AppUiState>()
    val coordinator = Band8QuotaAlertCoordinator(published::add)
    val healthy = state(syncState = SyncState.Synced, weekly = WeeklyQuota(80, 2_000))
    val sameThresholdBand = state(syncState = SyncState.Synced, weekly = WeeklyQuota(79, 2_000))
    val crossedThreshold = state(syncState = SyncState.Synced, weekly = WeeklyQuota(75, 2_000))

    coordinator.ingest(healthy)
    coordinator.updateEnabled(true)
    coordinator.ingest(healthy)
    coordinator.ingest(healthy)
    coordinator.ingest(sameThresholdBand)
    coordinator.ingest(crossedThreshold)

    assertEquals(listOf(80, 75), published.map { it.weeklyQuota?.remainingPercent })
  }

  @Test
  fun publishingHandlesPermissionRevocationBetweenCheckAndNotify() {
    var publishCount = 0

    assertEquals(
      Band8NotificationResult.Published,
      publishBand8QuotaNotification { publishCount += 1 },
    )
    assertEquals(1, publishCount)
    assertEquals(
      Band8NotificationResult.PermissionRequired,
      publishBand8QuotaNotification { throw SecurityException("permission revoked") },
    )
  }

  private fun state(
    syncState: SyncState,
    lastSyncAtMs: Long? = null,
    fiveHour: WeeklyQuota? = null,
    weekly: WeeklyQuota? = null,
    fiveHourAvailability: FiveHourQuotaAvailability =
      if (fiveHour == null) FiveHourQuotaAvailability.Missing else FiveHourQuotaAvailability.Available,
  ) =
    AppUiState(
      syncState = syncState,
      lastSyncAtMs = lastSyncAtMs,
      weeklyQuota = weekly,
      resetAvailableCount = null,
      resetCredits = emptyList(),
      connections =
        DeviceConnections(
          computer = DeviceLinkState.Connected,
          phone = DeviceLinkState.Connected,
          band = DeviceLinkState.Disconnected,
        ),
      chatGptState = ChatGptState.NotRunning,
      tasks = emptyList(),
      fiveHourQuota = fiveHour,
      fiveHourQuotaAvailability = fiveHourAvailability,
    )
}
