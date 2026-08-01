package com.codex.quota.android.ui

import com.codex.quota.android.domain.TaskState
import com.codex.quota.android.protocol.UpstreamFreshnessStatus
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class UiModelsTest {
  @Test
  fun `band 8 notification compatibility is opt in`() {
    assertEquals(false, NotificationSettings.Default.band8NotificationCompatibility)
  }

  @Test
  fun `band 8 build reports notification forwarding instead of a direct connection`() {
    assertEquals(
      BandStatusPresentation(
        label = "手环 8 通知",
        status = "转发已启用",
        linkState = DeviceLinkState.Connected,
      ),
      bandStatusPresentation(
        band8Only = true,
        band8NotificationCompatibility = true,
        directLinkState = DeviceLinkState.Disconnected,
      ),
    )
    assertEquals(
      BandStatusPresentation(
        label = "手环 8 通知",
        status = "转发未启用",
        linkState = DeviceLinkState.Disconnected,
      ),
      bandStatusPresentation(
        band8Only = true,
        band8NotificationCompatibility = false,
        directLinkState = DeviceLinkState.Connected,
      ),
    )
  }

  @Test
  fun `standard build keeps wearable SDK connection wording`() {
    assertEquals(
      BandStatusPresentation(
        label = "手环",
        status = "已连接",
        linkState = DeviceLinkState.Connected,
      ),
      bandStatusPresentation(
        band8Only = false,
        band8NotificationCompatibility = true,
        directLinkState = DeviceLinkState.Connected,
      ),
    )
    assertEquals(true, showBand10Controls(band8Only = false))
    assertEquals(false, showBand10Controls(band8Only = true))
  }

  @Test
  fun `only authorization uses attention text in the current task contract`() {
    assertEquals(TaskStatusEmphasis.Default, taskStatusEmphasis(TaskState.Running))
    assertEquals(TaskStatusEmphasis.Attention, taskStatusEmphasis(TaskState.NeedsAuthorization))
    assertEquals(TaskStatusEmphasis.Default, taskStatusEmphasis(TaskState.WaitingForReview))
  }

  @Test
  fun `compact elapsed labels match the product language`() {
    val now = 10L * 24 * 60 * 60 * 1000

    assertEquals("1分", taskElapsedLabel(now - 60_000, now))
    assertEquals("25分", taskElapsedLabel(now - 25 * 60_000, now))
    assertEquals("11小时", taskElapsedLabel(now - 11 * 60 * 60_000, now))
    assertEquals("3天", taskElapsedLabel(now - 3 * 24 * 60 * 60_000, now))
    assertEquals("1周", taskElapsedLabel(now - 9 * 24 * 60 * 60_000, now))
  }

  @Test
  fun `status ages tick at the next whole minute instead of freezing at composition time`() {
    assertEquals(60_000L, millisecondsUntilNextMinute(0L))
    assertEquals(59_000L, millisecondsUntilNextMinute(1_000L))
    assertEquals(1L, millisecondsUntilNextMinute(59_999L))
  }

  @Test
  fun `band connection check reports whether wearable authorization was granted`() {
    assertEquals("手环已授权，正在同步", bandConnectionCheckResultLabel(true))
    assertEquals("未取得手环授权，请保持小米运动健康已连接后重试", bandConnectionCheckResultLabel(false))
  }

  @Test
  fun `sync state never describes offline data as cached`() {
    val now = 2_000_000L

    assertEquals("已同步 2分", syncStatusLabel(SyncState.Synced, now - 120_000, now))
    assertEquals("缓存 2分", syncStatusLabel(SyncState.Cached, now - 120_000, now))
    assertEquals("离线 2分", syncStatusLabel(SyncState.Offline, now - 120_000, now))
    assertEquals(
      "已同步 2分",
      syncStatusLabel(SyncState.Synced, now - 120_000, now, UpstreamFreshnessStatus.Current),
    )
    assertEquals(
      "待同步",
      syncStatusLabel(
        SyncState.AwaitingConfirmation,
        null,
        now,
        UpstreamFreshnessStatus.Unavailable,
      ),
    )
  }

  @Test
  fun `sync pill labels remain short even for very old data`() {
    val now = 800L * 24 * 60 * 60_000
    val labels =
      listOf(
        syncStatusLabel(SyncState.Synced, 0, now, UpstreamFreshnessStatus.Current),
        syncStatusLabel(SyncState.Cached, 0, now, UpstreamFreshnessStatus.Cached),
        syncStatusLabel(
          SyncState.AwaitingConfirmation,
          null,
          now,
          UpstreamFreshnessStatus.Unavailable,
        ),
        syncStatusLabel(SyncState.Offline, 0, now, UpstreamFreshnessStatus.Current),
      )

    assertEquals(listOf("已同步 99周+", "缓存 99周+", "待同步", "离线 99周+"), labels)
    assert(labels.all { it.codePointCount(0, it.length) <= 8 })
  }

  @Test
  fun `quota levels use global percentage thresholds`() {
    assertEquals(QuotaLevel.Healthy, WeeklyQuota(68, 1).level)
    assertEquals(QuotaLevel.Warning, WeeklyQuota(35, 1).level)
    assertEquals(QuotaLevel.Critical, WeeklyQuota(12, 1).level)
  }

  @Test
  fun `reset information includes exact date and compact remaining time`() {
    val zone = ZoneId.of("UTC")
    val now = Instant.parse("2026-07-29T00:00:00Z").toEpochMilli()
    val expiry = Instant.parse("2026-08-01T00:00:00Z").toEpochMilli()

    assertEquals("8月1日到期", resetDateLabel(expiry, zone))
    assertEquals("8月1日重置", weeklyResetDateLabel(expiry, zone))
    assertEquals("剩余 3天", remainingTimeLabel(expiry, now))
  }

  @Test
  fun `five hour reset label distinguishes available pending and missing data`() {
    val resetAt = Instant.parse("2026-07-29T13:00:00Z").toEpochMilli()
    val quota = WeeklyQuota(68, resetAt)

    assertEquals(
      "13:00重置",
      fiveHourResetLabel(quota, FiveHourQuotaAvailability.Available, ZoneId.of("UTC")),
    )
    assertEquals(
      "待同步",
      fiveHourResetLabel(null, FiveHourQuotaAvailability.Pending, ZoneId.of("UTC")),
    )
    assertEquals(
      "暂无数据",
      fiveHourResetLabel(null, FiveHourQuotaAvailability.Missing, ZoneId.of("UTC")),
    )
  }

  @Test
  fun `reset card timestamps are formatted in Shanghai time`() {
    val granted = Instant.parse("2026-07-25T09:00:00Z").toEpochMilli()
    val expires = Instant.parse("2026-07-31T19:49:39.737Z").toEpochMilli()

    assertEquals("发卡 7月25日 17:00", resetGrantedAtLabel(granted))
    assertEquals("到期 8月1日 03:49", resetExpiresAtLabel(expires))
    assertEquals("发卡时间待同步", resetGrantedAtLabel(null))
  }
}
