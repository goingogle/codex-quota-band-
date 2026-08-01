import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const readProjectFile = (relativePath) =>
  readFile(new URL(`../${relativePath}`, import.meta.url), "utf8");

test("the explicit Band 8 build omits the private wearable SDK without weakening the standard build", async () => {
  const [gradle, band8Bridge, wearableSdkBridge] = await Promise.all([
    readProjectFile("android-app/app/build.gradle.kts"),
    readProjectFile(
      "android-app/app/src/band8Only/java/com/codex/quota/android/runtime/XiaomiWearableBridge.kt",
    ),
    readProjectFile(
      "android-app/app/src/wearableSdk/java/com/codex/quota/android/runtime/XiaomiWearableBridge.kt",
    ),
  ]);

  assert.match(gradle, /gradleProperty\("codexQuotaBand8Only"\)/);
  assert.match(
    gradle,
    /kotlin\.directories\s*\+=/,
    "AGP 9 built-in Kotlin requires custom Kotlin sources in AndroidSourceSet.kotlin",
  );
  assert.match(gradle, /if \(band8Only\).*src\/band8Only\/java/s);
  assert.match(gradle, /else.*src\/wearableSdk\/java/s);
  assert.match(
    gradle,
    /if \(!band8Only && !wearableSdkAar\.isFile\).*Wearable SDK AAR is required/s,
  );
  assert.match(gradle, /if \(!band8Only\).*implementation\(files\(wearableSdkAar\)\)/s);

  assert.doesNotMatch(band8Bridge, /com\.xiaomi\./);
  assert.match(band8Bridge, /fun sendTaskAlert\([^)]*task: SyncedTask[^)]*\): Boolean = false/s);
  assert.match(wearableSdkBridge, /com\.xiaomi\.xms\.wearable/);
});

test("Android pairing uses a real in-app QR scanner instead of a photo camera", async () => {
  const [mainActivity, scannerActivity, manifest, gradle] = await Promise.all([
    readProjectFile("android-app/app/src/main/java/com/codex/quota/android/MainActivity.kt"),
    readProjectFile(
      "android-app/app/src/main/java/com/codex/quota/android/PairingQrScannerActivity.kt",
    ),
    readProjectFile("android-app/app/src/main/AndroidManifest.xml"),
    readProjectFile("android-app/app/build.gradle.kts"),
  ]);

  assert.doesNotMatch(mainActivity, /MediaStore|ACTION_IMAGE_CAPTURE|STILL_IMAGE_CAMERA/);
  assert.match(mainActivity, /PairingQrScannerActivity/);
  assert.match(scannerActivity, /MlKitAnalyzer/);
  assert.match(scannerActivity, /Barcode\.FORMAT_QR_CODE/);
  assert.match(scannerActivity, /PairingQrPayloadPolicy\.accept/);
  assert.match(manifest, /android\.permission\.CAMERA/);
  assert.match(manifest, /\.PairingQrScannerActivity/);
  assert.match(gradle, /androidx\.camera:camera-camera2:1\.6\.1/);
  assert.match(gradle, /androidx\.camera:camera-view:1\.6\.1/);
  assert.match(gradle, /androidx\.camera:camera-mlkit-vision:1\.6\.1/);
  assert.match(gradle, /com\.google\.mlkit:barcode-scanning:17\.3\.0/);
});

test("Band 8-only UI hides Band 10 controls and uses notification-forwarding status", async () => {
  const [gradle, mainActivity, appUi] = await Promise.all([
    readProjectFile("android-app/app/build.gradle.kts"),
    readProjectFile("android-app/app/src/main/java/com/codex/quota/android/MainActivity.kt"),
    readProjectFile("android-app/app/src/main/java/com/codex/quota/android/ui/CodexQuotaApp.kt"),
  ]);

  assert.match(gradle, /buildConfigField\("boolean", "BAND8_ONLY", band8Only\.toString\(\)\)/);
  assert.match(mainActivity, /band8Only\s*=\s*BuildConfig\.BAND8_ONLY/);
  assert.match(appUi, /showBand10Controls\(band8Only\)/);
  assert.match(appUi, /bandStatusPresentation\(/);
});
