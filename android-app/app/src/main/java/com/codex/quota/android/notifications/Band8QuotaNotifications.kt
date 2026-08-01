package com.codex.quota.android.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.codex.quota.android.MainActivity
import com.codex.quota.android.R
import com.codex.quota.android.ui.AppUiState
import com.codex.quota.android.ui.FiveHourQuotaAvailability
import com.codex.quota.android.ui.SyncState
import com.codex.quota.android.ui.fiveHourResetLabel
import com.codex.quota.android.ui.syncStatusLabel
import com.codex.quota.android.ui.weeklyResetDateLabel
import java.time.ZoneId

data class Band8QuotaNotificationContent(
  val title: String,
  val summary: String,
  val details: String,
) {
  companion object {
    fun from(
      state: AppUiState,
      nowMs: Long,
      zoneId: ZoneId = ZoneId.systemDefault(),
    ): Band8QuotaNotificationContent {
      val fiveHourSummary =
        when {
          state.fiveHourQuota != null &&
            state.fiveHourQuotaAvailability == FiveHourQuotaAvailability.Available ->
            "5小时 ${state.fiveHourQuota.remainingPercent}%"
          state.fiveHourQuotaAvailability == FiveHourQuotaAvailability.Pending -> "5小时 待同步"
          else -> "5小时 暂无数据"
        }
      val weeklySummary =
        state.weeklyQuota?.let { "周额度 ${it.remainingPercent}%" } ?: "周额度 暂无数据"
      val resetDetails =
        buildList {
            state.fiveHourQuota
              ?.takeIf { state.fiveHourQuotaAvailability == FiveHourQuotaAvailability.Available }
              ?.let { add("5小时 ${fiveHourResetLabel(it, state.fiveHourQuotaAvailability, zoneId)}") }
            state.weeklyQuota?.let { add("周额度 ${weeklyResetDateLabel(it.resetsAtMs, zoneId)}") }
          }
          .joinToString(" · ")
      val status =
        syncStatusLabel(
          state = state.syncState,
          lastSyncAtMs = state.lastSyncAtMs,
          nowMs = nowMs,
          usageFreshness = state.usageFreshness,
        )

      return Band8QuotaNotificationContent(
        title = "Codex 配额",
        summary = "$fiveHourSummary · $weeklySummary",
        details = listOf(resetDetails, status).filter(String::isNotBlank).joinToString("\n"),
      )
    }
  }
}

data class Band8QuotaSignal(
  val fiveHourPercent: Int?,
  val fiveHourResetsAtMs: Long?,
  val weeklyPercent: Int?,
  val weeklyResetsAtMs: Long?,
) {
  companion object {
    fun from(state: AppUiState): Band8QuotaSignal? {
      if (state.syncState != SyncState.Synced) return null
      val fiveHourQuota =
        state.fiveHourQuota
          ?.takeIf { state.fiveHourQuotaAvailability == FiveHourQuotaAvailability.Available }
      if (fiveHourQuota == null && state.weeklyQuota == null) return null
      return Band8QuotaSignal(
        fiveHourPercent = fiveHourQuota?.remainingPercent,
        fiveHourResetsAtMs = fiveHourQuota?.resetsAtMs,
        weeklyPercent = state.weeklyQuota?.remainingPercent,
        weeklyResetsAtMs = state.weeklyQuota?.resetsAtMs,
      )
    }
  }
}

object Band8QuotaAutoPolicy {
  private val thresholds = listOf(75, 50, 25, 10)

  fun shouldNotify(previous: Band8QuotaSignal?, current: Band8QuotaSignal?): Boolean {
    current ?: return false
    previous ?: return true
    if (
      resetCycleChanged(previous.fiveHourResetsAtMs, current.fiveHourResetsAtMs) ||
        resetCycleChanged(previous.weeklyResetsAtMs, current.weeklyResetsAtMs)
    ) {
      return true
    }
    if (
      becameAvailable(previous.fiveHourPercent, current.fiveHourPercent) ||
        becameAvailable(previous.weeklyPercent, current.weeklyPercent)
    ) {
      return true
    }
    return crossedThreshold(previous.fiveHourPercent, current.fiveHourPercent) ||
      crossedThreshold(previous.weeklyPercent, current.weeklyPercent)
  }

  private fun resetCycleChanged(previous: Long?, current: Long?): Boolean =
    previous != null && current != null && previous != current

  private fun becameAvailable(previous: Int?, current: Int?): Boolean =
    previous == null && current != null

  private fun crossedThreshold(previous: Int?, current: Int?): Boolean {
    if (previous == null || current == null || current >= previous) return false
    return thresholds.any { threshold -> previous > threshold && current <= threshold }
  }
}

class Band8QuotaAlertCoordinator(
  private val dispatcher: (AppUiState) -> Unit,
) {
  private val lock = Any()
  private var enabled = false
  private var previousSignal: Band8QuotaSignal? = null

  fun updateEnabled(newEnabled: Boolean) {
    synchronized(lock) {
      if (newEnabled && !enabled) previousSignal = null
      enabled = newEnabled
    }
  }

  fun ingest(state: AppUiState) {
    val shouldNotify =
      synchronized(lock) {
        val currentSignal = Band8QuotaSignal.from(state) ?: return
        val result = enabled && Band8QuotaAutoPolicy.shouldNotify(previousSignal, currentSignal)
        previousSignal = currentSignal
        result
      }
    if (shouldNotify) dispatcher(state)
  }
}

enum class Band8NotificationResult {
  Published,
  PermissionRequired,
  NotificationsDisabled,
}

internal fun publishBand8QuotaNotification(publish: () -> Unit): Band8NotificationResult =
  try {
    publish()
    Band8NotificationResult.Published
  } catch (_: SecurityException) {
    Band8NotificationResult.PermissionRequired
  }

class Band8QuotaNotificationDispatcher(
  private val context: Context,
  private val clock: () -> Long = System::currentTimeMillis,
) {
  // Permission is checked before construction; the wrapper also handles a post-check revocation race.
  @SuppressLint("MissingPermission")
  fun notify(state: AppUiState): Band8NotificationResult {
    val permissionResult = notificationPermissionResult()
    if (permissionResult != null) return permissionResult
    val content = Band8QuotaNotificationContent.from(state, clock())
    val openApp =
      PendingIntent.getActivity(
        context,
        BAND8_QUOTA_NOTIFICATION_ID,
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    val notification =
      NotificationCompat.Builder(context, NotificationChannels.BAND8_QUOTA_STATUS_CHANNEL_ID)
        .setSmallIcon(R.drawable.codex_quota_notification)
        .setContentTitle(content.title)
        .setContentText(content.summary)
        .setStyle(
          NotificationCompat.BigTextStyle().bigText("${content.summary}\n${content.details}"),
        )
        .setContentIntent(openApp)
        .setAutoCancel(false)
        .setOnlyAlertOnce(true)
        .setLocalOnly(false)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .build()

    return publishBand8QuotaNotification {
      NotificationManagerCompat.from(context).notify(BAND8_QUOTA_NOTIFICATION_ID, notification)
    }
  }

  private fun notificationPermissionResult(): Band8NotificationResult? {
    if (
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
      return Band8NotificationResult.PermissionRequired
    }
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
      return Band8NotificationResult.NotificationsDisabled
    }
    return null
  }

  private companion object {
    const val BAND8_QUOTA_NOTIFICATION_ID = 8_008
  }
}
