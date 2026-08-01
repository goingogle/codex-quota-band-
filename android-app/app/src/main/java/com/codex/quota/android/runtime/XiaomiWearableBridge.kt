package com.codex.quota.android.runtime

import com.codex.quota.android.domain.SafeActivity
import com.codex.quota.android.domain.TaskBoard
import com.codex.quota.android.domain.TaskState
import com.codex.quota.android.protocol.ChatGptState
import com.codex.quota.android.protocol.CodexLinkStatus
import com.codex.quota.android.protocol.ComputerLinkStatus
import com.codex.quota.android.protocol.QuotaSnapshot
import com.codex.quota.android.protocol.QuotaSourceStatus
import com.codex.quota.android.protocol.QuotaWindow
import com.codex.quota.android.protocol.QuotaWindowStatus
import com.codex.quota.android.protocol.ResetInventoryStatus
import com.codex.quota.android.protocol.TaskSnapshot
import com.codex.quota.android.protocol.UpstreamFreshnessStatus
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Wearable-only quota summary. This is deliberately separate from the Windows v1/v2 wire
 * contracts: reset-card identities and titles never cross the phone-to-band boundary.
 */
internal fun buildBandQuotaSnapshot(
  snapshot: QuotaSnapshot,
  nowMs: Long = System.currentTimeMillis(),
): JsonElement =
  buildJsonObject {
    put("protocolVersion", JsonPrimitive(BAND_QUOTA_VERSION))
    put("generatedAt", JsonPrimitive(formatBandTimestamp(snapshot.generatedAtMs)))
    put("sourceStatus", JsonPrimitive(snapshot.effectiveBandSourceStatus(nowMs).bandValue()))
    if (snapshot.limitsCollectedAtMs == null) put("limitsCollectedAt", JsonNull)
    else put("limitsCollectedAt", JsonPrimitive(formatBandTimestamp(snapshot.limitsCollectedAtMs)))
    put(
      "windows",
      buildJsonArray {
        snapshot.windows.forEach { window ->
          add(
            buildJsonObject {
              put("id", JsonPrimitive(window.id))
              put("name", JsonPrimitive(window.name))
              put("windowMinutes", JsonPrimitive(window.windowMinutes))
              if (window.remainingPercent == null) put("remainingPercent", JsonNull)
              else put("remainingPercent", JsonPrimitive(window.remainingPercent))
              put("resetsAt", JsonPrimitive(formatBandTimestamp(window.resetsAtMs)))
              put("status", JsonPrimitive(window.status.bandValue()))
            },
          )
        }
      },
    )
    put(
      "resetInventory",
      buildJsonObject {
        put("status", JsonPrimitive(snapshot.resetInventory.status.bandValue()))
        if (snapshot.resetInventory.availableCount == null) put("availableCount", JsonNull)
        else put("availableCount", JsonPrimitive(snapshot.resetInventory.availableCount))
        if (snapshot.resetInventory.cachedAtMs == null) put("cachedAt", JsonNull)
        else put("cachedAt", JsonPrimitive(formatBandTimestamp(snapshot.resetInventory.cachedAtMs)))
        put(
          "items",
          buildJsonArray {
            snapshot.resetInventory.items.forEach { item ->
              add(
                buildJsonObject {
                  put("status", JsonPrimitive("available"))
                  if (item.grantedAtMs == null) put("grantedAt", JsonNull)
                  else put("grantedAt", JsonPrimitive(formatBandTimestamp(item.grantedAtMs)))
                  put("expiresAt", JsonPrimitive(formatBandTimestamp(item.expiresAtMs)))
                },
              )
            }
          },
        )
      },
    )
    put(
      "link",
      buildJsonObject {
        put("computer", JsonPrimitive(snapshot.computerLink.bandValue()))
        put("codex", JsonPrimitive(snapshot.codexLink.bandValue()))
      },
    )
  }

internal fun QuotaSnapshot.withDemoFiveHourQuota(
  nowMs: Long = System.currentTimeMillis(),
): QuotaSnapshot =
  copy(
    windows =
      windows.filterNot { it.name == "five_hour" || it.windowMinutes == FIVE_HOUR_WINDOW_MINUTES } +
        QuotaWindow(
          id = "demo-five-hour",
          name = "five_hour",
          windowMinutes = FIVE_HOUR_WINDOW_MINUTES,
          remainingPercent = DEMO_FIVE_HOUR_PERCENT,
          resetsAtMs = nowMs + DEMO_FIVE_HOUR_RESET_DELAY_MS,
          status = QuotaWindowStatus.Current,
        ),
  )

private fun formatBandTimestamp(value: Long): String =
  java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(value), java.time.ZoneOffset.UTC)
    .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

private fun QuotaSourceStatus.bandValue() =
  when (this) {
    QuotaSourceStatus.Ok -> "ok"
    QuotaSourceStatus.Partial -> "partial"
    QuotaSourceStatus.Unavailable -> "unavailable"
    QuotaSourceStatus.Paused -> "paused"
  }

private fun QuotaSnapshot.effectiveBandSourceStatus(nowMs: Long): QuotaSourceStatus {
  val usageFreshness = upstreamFreshness?.usage
  return when (usageFreshness?.status) {
    UpstreamFreshnessStatus.Current ->
      if (nowMs - (usageFreshness.lastSuccessAtMs ?: return QuotaSourceStatus.Partial) <= BAND_CURRENT_QUOTA_MAX_AGE_MS) sourceStatus
      else QuotaSourceStatus.Partial
    null -> sourceStatus
    UpstreamFreshnessStatus.Cached -> QuotaSourceStatus.Partial
    UpstreamFreshnessStatus.Unavailable -> QuotaSourceStatus.Unavailable
  }
}

private fun QuotaWindowStatus.bandValue() =
  when (this) {
    QuotaWindowStatus.Current -> "current"
    QuotaWindowStatus.PendingSync -> "pending_sync"
    QuotaWindowStatus.Unknown -> "unknown"
  }

private fun ResetInventoryStatus.bandValue() =
  when (this) {
    ResetInventoryStatus.Cached -> "cached"
    ResetInventoryStatus.CachedDerived -> "cached_derived"
    ResetInventoryStatus.Missing -> "missing"
    ResetInventoryStatus.Unavailable -> "unavailable"
  }

private fun ComputerLinkStatus.bandValue() =
  when (this) {
    ComputerLinkStatus.Online -> "online"
    ComputerLinkStatus.Offline -> "offline"
    ComputerLinkStatus.Paused -> "paused"
  }

private fun CodexLinkStatus.bandValue() =
  when (this) {
    CodexLinkStatus.Ok -> "ok"
    CodexLinkStatus.Unavailable -> "unavailable"
    CodexLinkStatus.Stale -> "stale"
    CodexLinkStatus.FormatChanged -> "format_changed"
  }

private const val BAND_QUOTA_VERSION = 2
private const val BAND_CURRENT_QUOTA_MAX_AGE_MS = 60_000L
private const val FIVE_HOUR_WINDOW_MINUTES = 300
private const val DEMO_FIVE_HOUR_PERCENT = 68
private const val DEMO_FIVE_HOUR_RESET_DELAY_MS = 3 * 60 * 60_000L

internal fun buildBandTaskSnapshot(snapshot: TaskSnapshot?): JsonElement {
  if (snapshot == null) return JsonNull
  val tasks = TaskBoard.from(snapshot.tasks).bandTasks
  return buildJsonObject {
    put("generatedAtMs", JsonPrimitive(snapshot.generatedAtMs))
    put("chatGptState", JsonPrimitive(snapshot.chatGptState.wireValue()))
    put(
      "tasks",
      buildJsonArray {
        tasks.forEach { task ->
          add(
            buildJsonObject {
              put("title", JsonPrimitive(task.title))
              put("state", JsonPrimitive(task.state.wireValue()))
              task.activity?.let { put("activity", JsonPrimitive(it.wireValue())) }
              put("updatedAtMs", JsonPrimitive(task.updatedAtMs))
            },
          )
        }
      },
    )
  }
}

private fun ChatGptState.wireValue() =
  when (this) {
    ChatGptState.Running -> "running"
    ChatGptState.NotRunning -> "not_running"
    ChatGptState.HookUnavailable -> "hook_unavailable"
  }

private fun TaskState.wireValue() =
  when (this) {
    TaskState.Running -> "running"
    TaskState.NeedsAuthorization -> "needs_authorization"
    TaskState.WaitingForReview -> "waiting_for_review"
  }

private fun SafeActivity.wireValue() =
  when (this) {
    SafeActivity.ExecutingCommand -> "executing_command"
    SafeActivity.ModifyingFiles -> "modifying_files"
    SafeActivity.UsingBrowser -> "using_browser"
  }
