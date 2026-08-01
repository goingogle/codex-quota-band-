package com.codex.quota.android.runtime

import android.content.Context
import com.codex.quota.android.domain.SyncedTask

/**
 * Safe replacement for the direct Xiaomi Wearable SDK bridge in Band 8 notification-only builds.
 * Band 8 NFC receives Android notifications through Mi Fitness, so no direct wearable connection
 * is reported and task alerts continue through the Android notification dispatcher.
 */
class XiaomiWearableBridge(
  @Suppress("UNUSED_PARAMETER") context: Context,
  private val repository: RuntimeStateRepository,
) {
  fun start() {
    repository.setBandConnected(false)
  }

  fun requestPermission(onResult: (Boolean) -> Unit = {}) {
    repository.setBandConnected(false)
    onResult(false)
  }

  fun refresh() {
    repository.setBandConnected(false)
  }

  fun stop() {
    repository.setBandConnected(false)
  }

  @Suppress("UNUSED_PARAMETER")
  fun sendTaskAlert(task: SyncedTask): Boolean = false
}
