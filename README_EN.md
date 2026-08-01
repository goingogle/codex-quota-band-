<p align="right"><a href="README.md">简体中文</a></p>

# Codex Quota for Xiaomi Smart Band 10 / 8 NFC

View your **Codex five-hour quota, weekly quota, reset times, and task status** on Windows and Android.
Smart Band 10 uses the dedicated band app; Smart Band 8 NFC uses the validated notification compatibility mode.

<p align="center">
  <img src="assets/icon.svg" alt="Codex Quota icon" width="96">
</p>

Current version: **0.6.0 fork build**

The Smart Band 8 NFC notification compatibility mode has been built and validated on a real band for quota summaries and task
notifications. The Windows client distinguishes “Not paired,” “Paired · offline,” and “Paired · online” in its connection
diagnostics. This fork has synchronized source changes, but has not published a GitHub Release binary yet.

[View changelog](CHANGELOG.md)

## 0.6.0 layout previews

<table>
  <tr>
    <th align="center">Android home</th>
    <th align="center">Band home (212×520)</th>
  </tr>
  <tr>
    <td align="center"><img src="docs/band-ui-preview/five-hour-primary-android.png" alt="Android five-hour primary quota layout" width="240"></td>
    <td align="center"><img src="docs/band-ui-preview/five-hour-primary-band.png" alt="Band five-hour primary quota layout" width="180"></td>
  </tr>
</table>

## Before you start

Install Xiaomi Fitness on the Android phone and connect the band there first. AstroBox is only used temporarily for the Smart
Band 10 RPK. Smart Band 8 NFC does not install an RPK and uses Xiaomi Fitness notification mirroring.

## Requirements

- A Windows 10/11 x64 computer with Codex installed and in use
- Xiaomi Smart Band 10, or Smart Band 8 NFC for the validated notification compatibility mode
- An Android phone with Xiaomi Fitness installed and connected to the band
- The phone and computer connected to the same trusted local network

The current architecture targets Android. AstroBox is used only to sideload or upgrade the band RPK; Xiaomi Fitness keeps the daily band connection.

Building the standard Smart Band 10 Android app from source requires `xms-wearable-lib_1.4_release.aar` from Xiaomi's official developer channel in `android-app/app/libs/`; this third-party SDK is not redistributed here. The Smart Band 8 NFC notification build can instead use the explicit `-PcodexQuotaBand8Only=true` mode, which does not require that private SDK or Android Studio.

### Phone compatibility

- **Windows → Android dashboard**: Any Android 8.0+ phone that can install the APK should work in principle; a Xiaomi phone is not required.
- **Band sync**: Xiaomi Fitness must be installed and kept running, with the band paired there first. Android phones from other manufacturers may work, but OEM background, autostart, battery, and permission policies can affect continuous sync or notification mirroring.
- **AstroBox**: It is only for sideloading or upgrading the Smart Band 10 RPK. Smart Band 8 NFC does not use it. AstroBox does not replace Xiaomi Fitness for the daily connection.
- Xiaomi officially lists Android 8.0+ support for Smart Band 10; Xiaomi-only phone features are outside this project's dependency.

## Download

Download matching files from the [goingogle fork Releases](https://github.com/goingogle/codex-quota-band-/releases) page after a release is published:

No Release has been published on this fork yet. To use the current changes, build from source using the [development guide](docs/development-guide.md).

| Install on | File |
| --- | --- |
| Windows computer | `Codex-Quota-Setup-0.6.0.exe` |
| Android phone | `CodexQuota-0.6.0.apk` |
| Xiaomi Smart Band 10 (AstroBox only for sideloading) | `com.codex.quota.android.release.0.6.0.rpk` |
| Xiaomi Smart Band 8 NFC | No extra file; use the Android APK notification mode |

Smart Band 10 uses all three matching components. Smart Band 8 NFC uses the matching Windows and Android packages only.

## Installation

### 1. Install the Windows app

1. Run `Codex-Quota-Setup-0.6.0.exe`.
2. After installation, the app stays in the Windows notification area. If it is hidden, click the `^` icon in the taskbar.
3. The current test build is not commercially code-signed, so Windows may display an “Unknown publisher” warning. Download only from this repository and verify the SHA-256 value shown on the Release page.

### 2. Install the Android app

Install `CodexQuota-0.6.0.apk`, keep Xiaomi Fitness connected, and allow Android notifications for CodexQuota.

#### Xiaomi Smart Band 10

1. Open AstroBox and enter the page for the connected Xiaomi Smart Band 10.
2. Import `com.codex.quota.android.release.0.6.0.rpk` and wait for the upgrade animation.
3. Exit AstroBox after the upgrade and keep Xiaomi Fitness connected to the band.

#### Xiaomi Smart Band 8 NFC

1. Do not install the RPK and do not use AstroBox.
2. In Xiaomi Fitness, allow app notifications from CodexQuota.
3. In CodexQuota Settings, enable “Smart Band 8 NFC notification compatibility.”
4. Tap “Send quota now,” then check the band’s native notification list.

If notifications should mirror while the phone is unlocked, disable Xiaomi Fitness’s “Notify only when the phone is locked”
restriction. With that restriction enabled, notifications may not reach the band while the phone screen is on.

The source notification remains visible and dismissible in Android because Xiaomi Fitness needs to read it for mirroring. Automatic
quota updates are limited to the first valid snapshot, downward crossings of 75/50/25/10 percent, and reset-cycle changes.

## First pairing

1. Make sure the phone and computer are on the same Wi-Fi or trusted local network.
2. Right-click the Codex Quota icon in the Windows notification area and select 「显示配对信息…」 (Show pairing information).
3. In CodexQuota, open Settings → “Scan to connect computer” and scan the QR code in the in-app scanner.
4. Complete pairing in CodexQuota.
5. On Smart Band 10, open 「Codex 额度」 and wait for the dashboard. On Smart Band 8 NFC, enable compatibility in Android Settings and use “Send quota now” to test the native notification list.

The QR code and six-digit pairing code expire quickly. You normally do not need to type the computer address. Use the advanced manual information in the Windows pairing window only if QR pairing fails.

## Troubleshooting

### Scanning does not open CodexQuota

Open CodexQuota Settings → “Scan to connect computer” and use the in-app QR scanner on the Windows code. AstroBox is not used for pairing.

### The plugin cannot find Windows

- Confirm that the phone and computer are on the same local network.
- If a VPN or proxy is enabled, allow AstroBox and local-network addresses to bypass it.
- When the Windows app starts for the first time, allow it through Windows Firewall on private networks.
- Avoid guest Wi-Fi, public Wi-Fi, and networks with client isolation enabled.

### The band shows offline or stops updating

For Smart Band 10:

- Confirm that Xiaomi Fitness is still running in the background and connected to the band.
- Disable phone-level battery restrictions for Xiaomi Fitness and CodexQuota, and grant Bluetooth/nearby-device permissions.
- If the phone or Xiaomi Fitness has restarted, open CodexQuota Settings and use “Check band connection” again.

For Smart Band 8 NFC, first verify that the CodexQuota notification appears on the phone, then check Android notification permission,
Xiaomi Fitness’s app-notification allowlist, the “notify only when phone is locked” restriction, the band’s Do Not Disturb setting, and the Bluetooth connection. A `Stop` event is labeled
“Waiting for review”; it does not claim that the task completed successfully.

### The Windows client says the phone is offline

Open “连接与诊断…” from the Codex Quota tray icon. “Paired · offline” means the saved pairing is still present but no live
authenticated phone connection is active; it is different from “Not paired.” The phone normally returns to “Paired · online” after
CodexQuota resumes in the background and reconnects.

### Only the weekly quota appears after reinstalling Codex, and reset credits show `--`

The weekly quota and available reset credits come from different local Codex data. Reinstalling Codex may preserve the weekly quota source while clearing the network cache that contains reset-credit information.

1. Open the Usage page in the Codex client.
2. Expand the reset-credit section and wait until its cards are fully displayed.
3. Right-click the Codex Quota icon in the Windows notification area and select 「立即刷新」 (Refresh now).
4. Wait about 5–10 seconds, then reopen the band app.

`0.6.0` keeps the last unexpired reset-credit data while the new cache is unavailable and shows 「缓存」 (Cached). It returns to 「已同步」 (Synced) automatically after Codex recreates the cache.

### Xiaomi Fitness competes for the connection

AstroBox should be closed after the RPK upgrade. If Xiaomi Fitness disconnects repeatedly, adjust the phone's background, autostart, and battery settings so Xiaomi Fitness and CodexQuota can coexist.

## Privacy

- Phone and band data moves only between your Windows computer, phone, and band. This project has no cloud relay; Windows contacts the official ChatGPT/Codex quota endpoint directly when confirming quota.
- The phone does not call OpenAI and does not need a permanent VPN or proxy. It receives minimized data from the paired Windows app over the trusted LAN.
- It reads and displays only quota summaries. It does not read or transmit conversations, prompts, project files, or terminal content.
- It does not read ChatGPT/Codex cookies or passwords. Windows reads the existing local Codex access token only to confirm quota with the official endpoint; the token remains in Windows process memory and never enters logs, caches, diagnostics, the phone, or the band.
- You can revoke all paired devices from the Windows tray menu at any time.
- Use it only on a trusted local network. Do not expose the Windows service port to the public internet.

## Uninstall

- Windows: uninstall Codex Quota from Settings → Apps → Installed apps.
- Phone: uninstall the CodexQuota APK from Android Settings.
- Smart Band 10: uninstall 「Codex 额度」 through AstroBox.
- Smart Band 8 NFC: no band app is installed; disable compatibility in CodexQuota or notification forwarding in Xiaomi Fitness.

<details>
<summary>Developer build and test instructions</summary>

Requires Node.js 24+, PowerShell, a Rust/WASI environment, and the Xiaomi Vela quick-app toolchain.

```powershell
npm install
npm test
npm run test:plugin
npm run build:win
npm run build:plugin

Set-Location band-app
npm install
npm run build
```

See [docs/security.md](docs/security.md) for the security model.

</details>

## Notice

This is a community open-source project, not an official product of OpenAI, Xiaomi, or AstroBox.

Licensed under the [MIT License](LICENSE).
