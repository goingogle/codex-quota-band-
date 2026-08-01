import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const windowsSourceUrl = new URL(
  "../windows-native/src/bin/codex_quota_windows.rs",
  import.meta.url,
);
const hostSourceUrl = new URL("../windows-native/src/host.rs", import.meta.url);

test("the Windows client distinguishes pairing from live phone connectivity", async () => {
  const [windowsSource, hostSource] = await Promise.all([
    readFile(windowsSourceUrl, "utf8"),
    readFile(hostSourceUrl, "utf8"),
  ]);

  assert.match(
    hostSource,
    /pub async fn phone_paired\(&self\) -> bool/,
    "WindowsHost must expose only whether a phone credential exists",
  );
  assert.match(
    windowsSource,
    /fn phone_connection_label\(phone_paired: bool, active_connections: usize\)/,
  );
  assert.match(windowsSource, /"未配对"/);
  assert.match(windowsSource, /"已配对 · 离线"/);
  assert.match(windowsSource, /"已配对 · 在线"/);
  assert.match(
    windowsSource,
    /fn tray_phone_status_label\(phone_paired: bool, active_connections: usize\)/,
  );
  assert.match(windowsSource, /"手机 未配对"/);
  assert.match(windowsSource, /"手机 已配对 · 离线"/);
  assert.match(windowsSource, /"手机 已配对 · 在线"/);
  assert.match(
    windowsSource,
    /argument == "--show-diagnostics"/,
    "the existing diagnostics window must be directly openable for safe verification and support",
  );
  assert.match(windowsSource, /run\(show_onboarding, show_diagnostics\)/);
  assert.match(
    windowsSource,
    /if show_diagnostics \{\s*show_diagnostics_window\(\);\s*\}/,
  );
});
