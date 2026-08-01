package com.codex.quota.android.domain

enum class TaskState {
  Running,
  NeedsAuthorization,
  WaitingForReview,
}

enum class NotificationMode {
  Never,
  WhenChatGptUnfocused,
  Always,
}

enum class PhoneUrgency {
  Silent,
  Vibrate,
}

enum class BandAlertBehavior {
  SystemControlled,
}

data class AlertContext(
  val chatGptFocused: Boolean,
  val androidForeground: Boolean,
  val reconnect: Boolean,
)

data class AlertDelivery(
  val phone: Boolean,
  val band: Boolean,
  val band8: Boolean,
  val phoneUrgency: PhoneUrgency?,
  val bandBehavior: BandAlertBehavior?,
) {
  companion object {
    val None =
      AlertDelivery(
        phone = false,
        band = false,
        band8 = false,
        phoneUrgency = null,
        bandBehavior = null,
      )
  }
}

class NotificationPolicy private constructor(
  private val mode: NotificationMode,
  private val waitingForReview: Boolean,
  private val needsAuthorization: Boolean,
  private val phone: Boolean,
  private val band: Boolean,
  private val band8: Boolean,
) {
  fun plan(state: TaskState, context: AlertContext): AlertDelivery {
    val eventEnabled =
      when (state) {
        TaskState.Running -> false
        TaskState.NeedsAuthorization -> needsAuthorization
        TaskState.WaitingForReview -> waitingForReview
      }
    val timingAllows =
      when (mode) {
        NotificationMode.Never -> false
        NotificationMode.WhenChatGptUnfocused -> !context.chatGptFocused
        NotificationMode.Always -> true
      }
    if (
      !eventEnabled ||
        !timingAllows ||
        context.androidForeground ||
        (context.reconnect && state == TaskState.WaitingForReview) ||
        (!phone && !band && !band8)
    ) {
      return AlertDelivery.None
    }

    return AlertDelivery(
      phone = phone,
      band = band,
      band8 = band8,
      phoneUrgency =
        if (phone) {
          when (state) {
            TaskState.NeedsAuthorization -> PhoneUrgency.Vibrate
            TaskState.WaitingForReview -> PhoneUrgency.Silent
            TaskState.Running -> null
          }
        } else {
          null
        },
      bandBehavior = if (band) BandAlertBehavior.SystemControlled else null,
    )
  }

  companion object {
    fun create(
      mode: NotificationMode,
      waitingForReview: Boolean,
      needsAuthorization: Boolean,
      phone: Boolean,
      band: Boolean,
      band8: Boolean = false,
    ) =
      NotificationPolicy(
        mode = mode,
        waitingForReview = waitingForReview,
        needsAuthorization = needsAuthorization,
        phone = phone,
        band = band,
        band8 = band8,
      )

    fun default() =
      create(
        mode = NotificationMode.WhenChatGptUnfocused,
        waitingForReview = true,
        needsAuthorization = true,
        phone = true,
        band = true,
        band8 = false,
      )
  }
}
