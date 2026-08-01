# Codex额度 Android

0.6.0 的日常链路分为两种：

- 小米手环 10：`Windows → Android Codex额度 → 小米运动健康 → 手环 RPK`；
- 小米手环 8 NFC：`Windows → Android Codex额度 → Android 系统通知 → 小米运动健康 → 手环通知列表`。

AstroBox 只在首次安装或升级手环 10 RPK 时临时使用，不参与日常额度同步、任务同步或提醒；
手环 8 NFC 路径完全不使用 AstroBox 或 RPK。

标准的手环 10 Android 构建使用小米官方 `xms-wearable-lib_1.4_release.aar`。该 SDK 二进制受其提供方许可约束，
不随本仓库分发；开发者需要从小米官方开发者渠道取得并放入 `android-app/app/libs/`。
APK 与 RPK 必须使用相同的
应用包名和配套签名，Wearable SDK 才会把设备权限与 `MessageApi` 数据通道交给该应用。

手环 8 NFC 的通知专用构建不需要该私有 AAR。它必须显式带上
`-PcodexQuotaBand8Only=true`；该模式只关闭直接 Wearable SDK 桥接，不会关闭 Android 通知、
Windows 局域网同步或任务提醒。默认构建仍严格要求私有 AAR，避免误把手环 8 空实现装进手环 10 包。

这是 0.6.0 Android 本地候选应用目录。当前包含任务看板、通知决策、Task Sync v1
严格解析核心、额度 Snapshot v1 严格解析与可信缓存归并，以及用户确认后的三页
Compose 界面骨架。

当前规则：

- 手机保留全部处理中/需要授权任务和最近 10 条等待查看任务。
- 手环快照按需要授权、处理中、等待查看排序，最多 3 条。
- 手机端等待查看默认静默，需要授权默认请求震动。
- 手环端只表达是否发送，收到后的震动由小米运动健康和手环系统控制。
- 任务协议拒绝未知字段、超长标题、重复或非法会话标识，不接收原始提示词、
  回复、命令、路径和工具输出。
- 额度协议同样拒绝未知字段和非法时间，旧快照不能覆盖新数据；断线时保留最近一次
  可信额度，明确显示为离线而不是已同步。
- 加密同步流在连接建立时协商额度与任务协议版本，并拒绝未知字段和嵌套快照时间倒挂；
  每条消息携带连接标识和单调序列，供传输会话拒绝跨连接消息。局域网传输不会回退到
  旧版明文 HTTP。
- Android 将二维码中的 Windows 公钥 SHA-256 指纹作为唯一信任锚，使用常量时间比较
  校验证书；IP、局域网发现结果和普通自签名证书都不能替代该身份。
- Android 只持久化电脑公钥指纹和随机手机令牌，两者以 Keystore 不可导出 AES 密钥
  加密保存，并明确排除云备份和设备迁移；损坏密文会被清除而不是绕过验证。
- Android 设置页提供“扫码连接电脑”按钮，使用 CameraX 和随 APK 安装的 ML Kit 模型在设备端实时识别二维码；首次使用会申请相机权限，但不会拍照、保存或上传画面。识别出的合法配对链接会直接交给 WSS `/pair` 客户端，随后由 `/sync` 自动重连会话；配对深链使用
  `codexquota://pair`，长期令牌只通过已固定公钥的 TLS 通道返回，不进入二维码。
- Android Manifest 禁止明文网络；WSS 客户端的信任管理器和主机校验器都会重新核对
  已配对电脑的公钥指纹，不接受系统 CA 或普通自签名证书作为替代。
- 首页以 5 小时额度圆环为主层级，在同一卡片内用横向进度条显示周额度；电脑与手环使用一个双栏连接卡片。
- 数据源没有 5 小时窗口时显示 `-- / 暂无数据`，窗口存在但尚未同步时显示 `-- / 待同步`，不猜测额度。
- 正式运行的初始状态只显示离线和等待同步，不使用开发预览中的示例额度或任务。
- 通知设置保持只读任务语义，不包含回复、授权、停止或其他反向控制入口。
- Android 13 及以上首次启动时只请求一次系统通知权限；拒绝不会阻止使用，也不会在
  后续启动时反复弹窗，用户可从设置页重新开启。
- “需要授权”使用默认振动、无声音的独立系统渠道；“等待查看”使用默认静默渠道，
  两者可在 Android 系统设置中分别调整。
- 设置页可手动检查本项目公开 GitHub Release；应用前台每天静默检查至多一次，不自动下载或安装，
  检查失败不影响局域网同步。

前一轮 Android ↔ Windows WSS 往返、正式托盘二维码窗口、局域网服务、Hook 和真实
额度采集已经在本地候选组合中接通并通过自动化验证。`0.6.0` 正在完成三端重建和按改动范围复验，仍不能
把本地 debug APK 当作正式发布包；最近结果见根目录 `docs/build-verification.md`。

本地测试：

```powershell
$env:JAVA_HOME = Join-Path $env:LOCALAPPDATA "codex-quota-dev\jdk-17"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
..\spikes\android-background-probe\gradlew.bat -p . :app:testDebugUnitTest
```

仅构建小米手环 8 NFC 通知版：

```powershell
..\spikes\android-background-probe\gradlew.bat -p . -PcodexQuotaBand8Only=true :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain
```

以上命令只要求 Java 17 和 Android SDK 命令行工具；使用 VS Code 时不必安装 Android Studio。
