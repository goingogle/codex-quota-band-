# CodexQuota 当前状态

更新时间：2026-08-02。

## 当前版本

`0.6.0 / versionCode 600` 的手环 8 NFC 通知兼容预发布已创建；Windows 便携版和 Band 8-only Android APK 已上传到 GitHub Release。

预发布标签为 [`v0.6.0-band8-nfc-preview`](https://github.com/goingogle/codex-quota-band-/releases/tag/v0.6.0-band8-nfc-preview)。它不包含小米手环 10 RPK、标准 Wearable AAR 构建或正式签名 APK。

当前发布资产使用不依赖私有 Wearable AAR 的 Band 8-only Debug APK；它不修改手环 10 RPK。Windows 便携版包含运行所需
`libunwind.dll`，Android APK 使用调试签名，适合当前用户验收，不应当作为长期稳定更新渠道。

## 当前能力

| 组件 | 已验证能力 |
| --- | --- |
| Windows | 托盘、二维码配对、额度确认、任务 Hook、加密局域网同步，以及区分未配对/配对离线/配对在线的连接诊断 |
| 安卓手机 | 5 小时额度、周额度、任务看板、缓存状态、手环桥接和检查更新 |
| 小米手环 10 | 两页纵向 swiper、5 小时额度、周额度进度条、可用重置和任务页 |
| 小米手环 8 NFC | Android 通知兼容入口、低频配额摘要和任务通知转发；已完成手机后台连接、Mi Fitness 转发和手环真机接收验收 |

## 产品边界

- 仅支持安卓手机；AstroBox 只用于侧载或升级小米手环 10 RPK。手环 8 NFC 路径不使用 AstroBox/RPK。
- 只同步额度摘要、重置摘要、连接状态、同步时间、短任务标题和任务状态。
- 不读取或传输提示词、回复、命令、文件路径、Cookie、密码或令牌。
- `等待查看` 只表示当前一轮停止并等待用户查看，不表示成功或失败。
- 手环 8 NFC 兼容依赖小米运动健康转发 Android 系统通知；手机无需访问 OpenAI 或开启代理，但必须保留 Android App 和小米运动健康。

## 发布边界

1. Band 8 NFC 用户下载 Release 中的 Windows portable ZIP 和 Debug APK。
2. 按 [device-acceptance.md](device-acceptance.md) 完成小米手环 8 NFC 真机验收。
3. 正式 Windows 安装器、正式签名 APK 和手环 10 RPK 需要后续补齐 NSIS、发布签名和私有 Wearable SDK 后另行发布。
