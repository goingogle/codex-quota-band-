# v0.6.0 Band 8 NFC Preview

这是 `goingogle/codex-quota-band-` fork 的首个 GitHub 预发布，面向小米手环 8 NFC 的通知兼容路径。

## 包含内容

- `CodexQuota-0.6.0-Windows-x64-portable.zip`：Windows x64 便携客户端，ZIP 内含 `CodexQuota.exe` 和运行所需的 `libunwind.dll`。
- `CodexQuota-0.6.0-band8-nfc-debug.apk`：不依赖小米私有 Wearable AAR 的 Band 8-only Android APK，使用调试签名。
- `SHA256SUMS.txt`：上述文件的 SHA-256 校验值。

## 不包含内容

- 小米手环 10 RPK；
- 标准 Wearable SDK Android APK；
- NSIS Windows 安装器；
- 正式发布签名 APK。

## 手环 10 支持

项目源码仍支持标准手环 10 构建：`band8Only=false` 时使用 `src/wearableSdk/java`，并要求小米官方
`xms-wearable-lib_1.4_release.aar`、匹配的发布签名和 RPK。本预发布只针对手环 8 NFC，因此没有上传这些手环 10 资产。

## 使用边界

此预发布用于当前小米手环 8 NFC 用户的功能验收。手环 8 NFC 不安装 RPK，而是通过小米运动健康转发 Android 通知。
Windows portable ZIP 需要手动解压，APK 的调试签名也不应被视为长期稳定更新签名。安装前请先核对 `SHA256SUMS.txt`。

## 已验证范围

- 根 Node 契约测试：46/46；
- Windows Rust workspace 测试：71/71；
- Windows release EXE 已用便携 Rust + LLVM-MinGW 构建；
- Band 8 NFC 通知兼容、任务通知和小米运动健康转发已完成真实手环验收。
