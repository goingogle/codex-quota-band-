package com.codex.quota.android.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PairingQrPayloadPolicyTest {
  @Test
  fun acceptsOnlyTheCodexPairingDeepLink() {
    val pairingLink = "codexquota://pair?offer=abc_DEF-123"

    assertEquals(pairingLink, PairingQrPayloadPolicy.accept(pairingLink))
    assertEquals(pairingLink, PairingQrPayloadPolicy.accept("  $pairingLink\n"))
    assertNull(PairingQrPayloadPolicy.accept("https://example.com/pair?offer=abc"))
    assertNull(PairingQrPayloadPolicy.accept("codexquota://other?offer=abc"))
  }

  @Test
  fun rejectsBlankMalformedAndOversizedScannerResults() {
    assertNull(PairingQrPayloadPolicy.accept(null))
    assertNull(PairingQrPayloadPolicy.accept(""))
    assertNull(PairingQrPayloadPolicy.accept("not a uri"))
    assertNull(PairingQrPayloadPolicy.accept("codexquota://pair"))
    assertNull(PairingQrPayloadPolicy.accept("codexquota://pair?offer=${"a".repeat(8_193)}"))
  }
}
