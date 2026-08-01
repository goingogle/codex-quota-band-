package com.codex.quota.android.protocol

import java.net.URI

object PairingQrPayloadPolicy {
  private const val MAX_SCANNED_TEXT_LENGTH = 8_192

  fun accept(rawValue: String?): String? {
    val candidate = rawValue?.trim().orEmpty()
    if (candidate.isEmpty() || candidate.length > MAX_SCANNED_TEXT_LENGTH) return null

    val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
    if (uri.scheme != "codexquota" || uri.host != "pair") return null
    if (uri.userInfo != null || uri.port != -1 || uri.fragment != null) return null
    if (!uri.rawPath.isNullOrEmpty()) return null
    if (!hasNonEmptyOffer(uri.rawQuery)) return null
    return candidate
  }

  private fun hasNonEmptyOffer(rawQuery: String?): Boolean =
    rawQuery
      ?.split('&')
      ?.any { field ->
        val separator = field.indexOf('=')
        separator > 0 && field.substring(0, separator) == "offer" && separator < field.lastIndex
      } == true
}
