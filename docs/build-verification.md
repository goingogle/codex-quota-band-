# 构建与验证

本文件只保留当前公开预发布的验证结论和校验值。逐次开发、调试和设备环境记录不随公开仓库保留。

## v0.6.0-band8-nfc-preview 发布资产

本预发布面向小米手环 8 NFC 通知兼容路径。Android APK 的 `versionCode` 为 `600`，Windows 文件为便携 ZIP，不是 NSIS 安装器。

| 组件 | 自动验证 | 当前本地产物 | SHA-256 |
| --- | --- | --- | --- |
| Windows | Rust workspace 71/71，`cargo fmt --check` | `CodexQuota-0.6.0-Windows-x64-portable.zip`（2,904,800 bytes） | `1339CE5126DB7E358C737226D65135B0268E541862BE18159544F50274D4376A` |
| 安卓手机（Band 8-only） | 先前 Band 8-only 单测 107/107、Lint、Debug assemble | `CodexQuota-0.6.0-band8-nfc-debug.apk`（46,935,400 bytes） | `81F561E224BA2BD2173F95C58055315CBBCA29FF81C4B482467181B25E8C5119` |
| 根目录契约 | Node 测试 46/46 | — | — |

标准 Android release APK 使用固定发布签名；签名材料和小米 Wearable SDK 二进制均不随仓库分发。本次预发布 APK 是
Debug 签名，仅用于 Band 8 NFC 当前验收。ZIP 内的
`CodexQuota.exe` SHA-256 为 `63C0FB203E9D1BF2FC0105DE42253904056293DD8990443CCC03663E88E4BD90`，
`libunwind.dll` SHA-256 为 `13BF4E99B0193634EBDEB0BBDEFF9753B39F1D04799183F664111014AABF4C2C`。

## 真机结论

Windows 安装、手机与电脑配对、额度和任务同步、手机后台缓存语义、手环数据同步、两页纵向切换、5 小时额度和周额度进度条均已通过人工验收。

正式 Windows 安装器、签名 APK 和手环 10 RPK 尚未纳入本次预发布；未来发布时必须重新计算对应资产的 SHA-256。
