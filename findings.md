# 手环 8 NFC 通知模式发现记录

## 2026-08-01 第二轮真机反馈

- Band 8-only APK 的设置页仍显示手环 10 专属入口或字样；这会让用户误以为手环 8 可以调用 Xiaomi Wearable SDK。该构建应直接隐藏专属项，标准手环 10 构建保持原样。
- 首页“手环未连接”来自手环 10 直连语义；手环 8 通知兼容模式无法从本应用直接读取手环蓝牙连接状态，因此必须改成“通知转发已启用/未启用”等可验证事实，不能伪造连接状态。
- 电脑—手机配对和配额同步已经成功（设置页曾显示“已同步 刚刚”，周额度为 66%），所以任务页无状态不等于整条局域网同步失败；应单独检查 Windows 任务 Hook 是否安装及是否实际生成任务事件。
- Android 已成功向系统通知管理器发布配额测试通知，但用户确认小米运动健康没有把它送到手环。当前配额渠道为低重要度且静默，这可能被厂商转发规则过滤；还需核对 Mi Fitness 通知监听器、屏幕状态限制和手环免打扰设置。
- ADB 实测 `com.mi.health/com.xiaomi.fitness.notify.NotifySyncService` 已列入系统通知监听器，说明小米运动健康确实拥有读取通知的系统权限，不是用户漏开总权限。
- CodexQuota 的 `POST_NOTIFICATIONS` 权限为 `granted=true`；通知管理器记录显示配额通知已经发布 1 次并更新 5 次，当前内容含周额度 64%，所以 Android 端发布链路正常。
- 该通知使用 `band8-quota-status-v1`，系统最终重要度为 `LOW`，`isNoisy=false`，并被 HyperOS 归入 `Aggregate_SilentSection`。通知渠道重要度创建后不能可靠原地提升，因此修复应使用新的渠道 ID，并以默认重要度、无声音、无手机振动的组合再次验证转发。
- `CodexQuotaApp.kt` 首页直接读取 `state.connections.band` 并映射成“已连接/未连接/不可用”；Band 8-only 空桥接无法产生直连状态，导致“未连接”恒定出现。设置页的“检查手环 10 应用连接”和“手环 10 应用提醒”也没有按构建模式隐藏。
- Gradle 已有 `band8Only` 属性但 BuildConfig 只导出了配额演示开关；UI 目前无法知道自己是哪一种构建。应增加只读 `BAND8_ONLY` 构建常量，由 `MainActivity` 显式传给 Compose，便于单测和保留标准构建行为。
- 设置页可以在 Band 8-only 构建中直接省略“检查手环 10 应用连接”和“手环 10 应用提醒”，无须新增会让小白用户继续困惑的二级菜单；手环 8 分组和系统通知设置保留。
- 配额通知渠道 ID 仍是 `band8-quota-status-v1`。因为 Android 通知渠道创建后重要度不可由应用覆盖，新的默认重要度方案必须升级为新 ID，并把 v1 加入旧渠道清理列表。
- README 已明确任务状态不可用时要从 Windows 托盘执行“安装/修复任务 Hook”，重启 ChatGPT，并在 ChatGPT 设置的钩子页信任、开启四项 Hook。当前配额正常但任务为空，与 Hook 未安装/未信任的表现一致。
- 实机检查已确认任务根因：`C:\Users\Strol\.codex\hooks.json` 不存在，Windows 数据目录也没有 `hook-status-v1.json` 或 `hook-events-v1`，表示 Hook 从未安装、也从未被 ChatGPT 调用；手机端没有任务事件可同步。
- Windows 程序源码的安装入口是同一已安装 EXE 的 `--install-hooks` 参数，只会合并自己标有 `--owner codex-quota` 的四项事件处理器并保留其他用户配置；目标路径正是缺失的 `.codex\hooks.json`。
- 自带安装入口执行成功后，`hooks.json` 已包含 `UserPromptSubmit`、`PermissionRequest`、`PreToolUse`、`Stop` 四项各一个产品自有处理器，均指向当前已安装的 CodexQuota.exe，超时为 3 秒。
- 手机端任务通知设置实测全部开启：需要授权、等待查看、手机通知和 Band 8 通知兼容均为 true，通知时机为“ChatGPT 失焦时”。因此新 Hook 事件到达后应进入通知策略。
- Windows 的失焦判断只认可官方 `OpenAI.Codex` 或 `OpenAI.ChatGPT` 打包应用；对当前诊断而言，只要官方应用不在前台，任务通知应允许发布。
- 兼容方式发送的诊断 Hook 已由 Windows 记录为 `accepted`，事件队列随后被主程序清空，说明“Hook 命令 → Windows 任务归并”已经恢复。
- 但诊断任务没有出现在 Android 通知中；重新前台打开手机 App 后页面显示“缓存 9分”，而不是实时已同步。当前阻断点已后移到“Windows → Android 的现有 WSS 连接暂时中断”，不能把它归咎于 Hook 或通知策略。
- 手机进程仍存活、配对数据未被清除；需先核对 Windows 48733 监听、当前局域网地址和手机保存的配对端点，恢复实时连接后 Windows 当前任务快照会再次下发。
- 初查固定 48733 端口没有结果是诊断假设错误：本版本的手机实际保存端点是 `wss://192.168.1.4:17322/sync`。电脑当前 WLAN 地址仍是 `192.168.1.4`，端点 IP 没有漂移；应按保存的 17322 端口继续检查。
- Windows CodexQuota 主进程仍正常响应，手机进程也仍存活，配对凭据和端点均存在；无需清数据或重新安装，只需确定 17322 监听/防火墙/重连状态。
- 端口 17322 实测由 CodexQuota PID 27688 在 `0.0.0.0` 监听，且已与手机 `192.168.1.6` 建立 TCP 连接；手机到端口的 TCP 探测也成功。重连后 Android 页面恢复为“已同步 刚刚”，说明 WSS 不是持续性故障，只是诊断时的短暂重连窗口。
- 重连后仍未出现“需要授权”系统通知，需要进入任务页确认诊断快照是否已下发；若任务存在但无通知，再查通知策略的前序状态/失焦判断，若任务不存在则查 Windows publisher 的当前快照行为。
- Android 任务页真机确认已出现 1 条诊断任务，状态“需要授权”、更新时间 4 分钟前，页面同时显示“已同步 刚刚”。因此 Windows Hook → 任务归并 → WSS → Android 看板链路已完整恢复。
- 该诊断事件恰好在 WSS 断开窗口中产生，重连时作为已有快照显示，但没有形成系统通知；不能用它判断当前实时任务通知是否失效。安装新 APK 后应在连接稳定时做一次状态变化测试。
- 新 APK 覆盖安装成功并保留数据。真机设置页已确认手环 10 的“检查应用连接”和“应用提醒”完全隐藏，只保留小米手环 8 NFC 通知兼容、立即发送配额和 Android 通知相关功能。
- 应用启动后已创建并发布 `band8-quota-status-v2`：系统重要度为 `DEFAULT(3)`，声音为 null、手机振动为 false；旧 v1 渠道已标记删除。这正是预期的“可被转发但手机静音”组合。
- 新版首页真机文本已核对：电脑“已连接”，手环区域显示“手环 8 通知 / 转发已启用”，并显示“1 个任务需要你在电脑端处理”；不再把 Band 8 报成未连接。
- `TaskAlertCoordinator` 对同一任务的状态变化会重新规划通知；从“需要授权”切到“等待查看”可在 WSS 已连接时形成一次真实实时通知。任务通知设置 `localOnly=false`（Band 8 兼容路径）且渠道为高重要度，适合用来区分“配额旧静默渠道问题”与“Mi Fitness 整体未转发”。
- 第一轮实时状态变化测试发生时 CodexQuota 手机页面仍在前台；任务状态虽已由 Windows 接受，但系统没有新增任务通知。产品策略本来就会在 Android 页面前台时抑制重复提醒，因此这次结果不能用于判断 Mi Fitness 转发失败。
- 电源键测试后 HyperOS 在 USB 供电下仍报告 `mWakefulness=Awake`；更可靠的条件是先按 Home 让 Activity 明确进入后台，再发送相反状态变化，并从 Android 通知记录确认是否真实发布。
- 手机退回桌面后再次切换为“需要授权”仍未发布任务通知；此时 Android 前后台条件已满足，但当前 Windows 前台正是官方 OpenAI.Codex，本应用通知时机又设置为“失焦”。因此策略会按设计继续抑制，不能判为任务通知故障。
- 为不依赖 Windows 前台焦点做确定性验收，可临时把手机端通知时机切为“始终”，在手机 App 后台发送一次相反状态变化；确认系统和手环后立即恢复“失焦”。
- 手机端通知时机已成功临时改为 `Always`，随后退出到桌面并实时把诊断任务从“需要授权”切为“等待查看”；Android 仍没有创建任务通知，只有配额 v2 通知。此时焦点策略不再是解释，需要检查 Activity 前后台标志接线和任务 dispatcher 的实际调用。
- 真机状态进一步确认：手机 App 退到桌面后 MainActivity 正常进入 `STOPPED`，但 Windows 端 17322 的手机 TCP 连接消失；诊断事件产生时 WSS 不在线。再次打开 App 后任务页通过重连更新为“等待查看 / 缓存 1分”，而 reconnect 保护按设计不回放已存在的等待通知。
- 所以“App 退后台收不到任务通知”的当前根因是 HyperOS 没有让局域网 WSS 保持运行，而不是 dispatcher、Hook 或 Mi Fitness。项目不使用常驻前台服务的既定边界下，需要核对并放开 Codex额度 的后台电量/自启动限制；之后再做实时通知测试。
- 临时通知时机已恢复为用户原来的 `Unfocused`，其他通知开关未改变。
- 退到桌面后连续 46 秒监测：Android 进程 PID 17819 始终存活，但 WSS 从第一次采样起持续断开，没有按客户端 30 秒退避重新建立。这说明不是进程被杀，而更像 HyperOS 对后台进程执行/网络进行了冻结。
- 系统 standby bucket 为 10（活跃），普通 Android appops 没显示明确拒绝项；还需进入 HyperOS 应用详情核对厂商专有的“省电策略/后台运行/自启动”，标准 Android bucket 不能代表小米后台许可。
- HyperOS 应用信息页截图已确认：Codex额度“自启动”开关明确为关闭；“权限使用记录”显示正在运行，通知管理为允许。后台连接冻结与自启动关闭直接吻合。
- 该页同时提供“省电策略”入口，但摘要只显示耗电 0.0%，不能据此判断当前策略。需要进入后选择允许后台运行的“不限制”，并开启自启动；这两项是可逆的系统设置，不涉及代理或 OpenAI 网络访问。
- 自启动已成功切为开启（蓝色）。HyperOS 弹窗提示这可能增加内存占用和耗电，系统仍会按使用情况优化不常用应用；需要确认“知道了”后继续设置省电策略。
- 省电策略当前实际选中“智能限制后台运行（推荐）”，说明文字明确包含“对后台联网、定位、传感器使用或 CPU 占用的限制”；这与 WSS 一进后台即断完全吻合。
- 同页提供“无限制（不采取任何限制措施）”。为满足任务通知的实时性，需要改选无限制；不选常驻前台服务，代价是应用进程可能带来少量额外耗电。
- 省电策略已成功切换到“无限制”，蓝色勾选已真机截图确认；自启动也已开启。两项系统前置条件现已满足，可以重新验证 App 退后台后的 WSS 是否保持。
- 修改后的对照结果明确：先前 46 秒的 10 次后台采样全部 `WssEstablished=false`；开启自启动并选无限制后，50 秒的 10 次后台采样全部为 `true`，Android PID 也保持不变。HyperOS 后台冻结根因已修复。
- 现在可在 App 后台且 WSS 在线时进行真实任务状态变化测试；为了排除当前 Windows Codex 前台导致的失焦抑制，仍需临时使用 `Always`，测试后恢复。
- 最终实时测试满足全部前置条件：手机 App 在后台、WSS 为 `ESTABLISHED`、临时通知时机为 `Always`。Android 随即发布高重要度 `needs-authorization-v2` 通知，标题“需要授权”、正文“任务”。
- 实际任务通知 flags 为 `ONLY_ALERT_ONCE|AUTO_CANCEL`，没有 `LOCAL_ONLY`；只有 HyperOS 自动聚合摘要带 `LOCAL_ONLY`，不影响原通知被 Mi Fitness 读取。配额 v2 通知也同时保持默认重要度。
- Mi Fitness 通知监听器仍处于启用状态，因此 Android → Mi Fitness 的系统前置条件均满足；剩余一步只能由用户观察手环是否收到/振动。
- 测试后手机通知时机已恢复为原来的 `Unfocused`。自启动和无限制省电策略保留，因为它们是后台任务同步必要条件。
- 本轮 APK 为 `CodexQuota-0.6.0-band8-nfc-statusfix-debug.apk`，46,935,400 bytes，SHA-256 `81F561E224BA2BD2173F95C58055315CBBCA29FF81C4B482467181B25E8C5119`，签名证书仍为既有 `c16b...77ae`，已成功覆盖安装且未清数据。
- Band 8-only UI 的新模型测试和根接线测试已转绿：首页语义改为通知转发状态，构建常量已从 Gradle 传入 Compose，手环 10 控件只在非 Band 8-only 构建显示。

## 真机扫码问题

- 2026-08-01 真机确认：“扫码连接电脑”会打开手机普通相机并拍照，但不会解析二维码。
- 根因位于 `MainActivity.openPairingCamera()`：当前只发送 `MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA` / `MediaStore.ACTION_IMAGE_CAPTURE`，这两个动作都不提供二维码识别结果，也不会把识别出的 `codexquota://pair` 深链回传给应用。
- 修复必须使用真正的二维码扫描能力，并让扫描结果显式交给现有 `CodexQuotaApplication.handlePairingLink()`，不能继续依赖相机厂商是否在普通拍照界面附带“扫一扫”。
- Android/Google 官方文档给出两条真正的扫码路径：Google Code Scanner 不需要应用相机权限但依赖 Google Play 服务动态模块；CameraX + ML Kit 可把识别模型随 APK 打包并离线运行。
- 当前手机是中国版小米设备，不能把 Google Play 服务是否完整可用当作前提；因此选择 CameraX + 内置版 ML Kit，只新增一次性的相机权限。扫码图像只在设备端处理，不拍照、不保存、不上传。
- 采用 2026-07-01 官方稳定版 CameraX `1.6.1`；ML Kit 官方文档当前列出的内置条码模型为 `com.google.mlkit:barcode-scanning:17.3.0`，约增加 2.4 MB，但安装后立即可用。
- Android 官方 `camera-samples` 已提供 `LifecycleCameraController + MlKitAnalyzer + BarcodeScanning` 的 QR 实时识别范式；本项目只需返回首个合法的 `codexquota://pair` 文本，不需要绘制复杂识别框。
- `camera-view` 和 `camera-mlkit-vision` 不会自动把 Camera2 后端打入 APK；真机需要显式声明同版本 `androidx.camera:camera-camera2:1.6.1`，否则 `LifecycleCameraController` 会报 CameraX 未配置默认实现。
- 最终真机已显示自有扫码预览页和“将电脑上的配对二维码放入画面”，不再调用小米普通拍照相机；扫描新生成的 Windows QR 后设置页显示“已同步 刚刚”，电脑—手机配对成功。

## 已确定产品方向

- 手机保留为局域网与蓝牙桥接，但不需要代理或 OpenAI 凭据。
- 手环 8 使用 Mi Fitness 的 Android 通知镜像能力，不移植手环 10 RPK。
- 用户的核心需求是查看配额摘要，并收到任务待授权/等待查看通知。

## 必须保持的项目约束

- `Stop` 的产品语义是“等待查看”，不是“已完成”。
- 不新增常驻前台服务。
- Android 和手环通知开关应保持独立。
- 不把缓存值冒充实时值，不补猜缺失的配额数据。

## 文档基线

- 当前任务通知由 `TaskAlertCoordinator` 统一处理；处理中静默，待授权/等待查看按焦点和渠道设置决策。
- Android 每 45 秒请求刷新、每 5 秒复核数据年龄，但进程被系统杀死后不承诺继续同步。
- Android 系统通知是已经存在的正式能力；手环 8 模式应复用它，而不是修改 WSS 或额度协议。
- `CONTEXT.md` 不允许用常驻状态栏通知掩盖后台限制，因此配额状态不能实现为永久 ongoing foreground notification。
- 当前 README 把非手环 10 设备描述为“可能页面错位”，但手环 8 NFC 实际采用通知兼容模式，后续需修正文档事实。
- 本次不修改 RPK，因此不触发 212×520 手环 UI 预览确认流程。

## 现有 Android 触点

- 依赖装配在 `CodexQuotaApplication.kt`，Activity 通过 Application 更新通知设置。
- 设置模型在 `ui/UiModels.kt`，持久化在 `ui/NotificationSettingsStore.kt`。
- 设置页集中在 `ui/CodexQuotaApp.kt::SettingsScreen`，已有手机通知、手环提醒和系统通知设置入口。
- 任务通知已有独立的 `NotificationChannels`、`TaskAlertCoordinator` 和 `TaskNotificationDispatcher`，并具备单元测试。
- 运行时配额快照由 `RuntimeStateRepository` 对外暴露；新配额提醒应订阅该状态，而不是侵入 WSS 协议。

## 代码行为与设计约束

- `TaskNotificationDispatcher` 发布的手机任务通知显式设置了 `localOnly=true`；手环 10 任务提醒另走 `XiaomiWearableBridge.sendTaskAlert`。
- 手环 8 兼容通知必须使用独立 dispatcher，并避免 `localOnly=true`，否则可能被系统视为不应桥接到穿戴设备。
- 当前只创建“需要授权”和“等待查看”两个高重要度渠道；配额兼容模式需要单独的低重要度、无振动渠道。
- `RuntimeStateRepository.state` 是 `StateFlow<AppUiState>`，已包含五小时额度、周额度、重置时间、新鲜度和连接状态，适合生成通知模型。
- `CodexQuotaApplication` 拥有长生命周期 scope，可在进程存活期间订阅配额状态并执行低频自动策略。
- `MainActivity` 已集中负责设置保存和 Toast 反馈；适合增加“立即发送配额到手环”动作。
- 设置页现有“手环提醒”专指 Wearable Bridge，直接复用会混淆设备模式；应新增明确的“手环 8 通知兼容”开关。

## 测试与策略结构

- Android 单测只依赖 JUnit，没有 Robolectric；通知内容和自动节流应提炼成纯 Kotlin 模型测试，Android dispatcher 只负责权限检查与发布。
- `NotificationPolicy` 当前只有 phone/band 两路，且 `band` 明确表示 Wearable SDK。为了不改既有策略契约，手环 8 兼容任务通知应在 coordinator 中作为第三个独立 dispatcher，并沿用同一事件/时机决策结果。
- 手环 8 兼容开关默认必须关闭，避免升级后同一任务同时收到 RPK 提醒和 Mi Fitness 镜像通知。
- 配额状态不使用常驻通知；自动推送应只在首次获得有效数据、跨过关键阈值或重置周期变化时发生，手动推送不受节流限制。
- 纯策略建议以 75/50/25/10 四个剩余百分比阈值判断向下跨越；额度回升但重置时间未变化时不触发。

## 首轮实现复核

- `git diff --check` 未发现空白错误；所有已有 `NotificationSettings` 构造点因新字段带默认值而保持源码兼容。
- 手环 8 任务兼容和手机通知使用相同 notification ID：两路同时启用时，后发布的可桥接版本覆盖前一条，不产生两条长期记录。
- 兼容任务通知必须出现在 Android 通知栏，这是 Mi Fitness 读取并转发通知的必要条件；设置文案已明确这一副作用。
- 自动配额只接受 `SyncState.Synced` 且至少存在一个有效额度窗口；缓存、离线、待同步数据只允许用户手动发送，并保留真实状态标签。

## UI 预览门槛

- `CONTEXT.md` 明确要求 Android App 正式 UI 改动前先提供单方案预览并由用户确认。
- 正式 Compose/Activity 的可见接线已撤回，避免把预览当成既成实现；底层通知代码和测试不影响当前 UI。
- 预览采用现有深色设置页视觉语言，只新增一个独立“小米手环 8 NFC”分组：兼容开关、立即发送动作和 Mi Fitness 首次配置提示。

## 最终实现结论

- 用户已确认 Android 设置页预览，正式 UI 接线已恢复。
- 手环 8 NFC 兼容模式默认关闭；开启后任务提醒作为可由小米运动健康读取的 Android 系统通知发布，手机和手环 10 渠道仍保持独立。
- 配额使用独立低重要度、无振动通知渠道和固定通知 ID；通知不是 ongoing/前台服务通知，可以从手机通知栏划掉。
- 自动配额通知只接受当前 `SyncState.Synced` 且真实存在的额度；触发条件为首次有效数据、向下跨过 75/50/25/10 阈值或重置周期变化。
- 手动发送不受自动节流限制；缓存、待同步或缺失状态会如实显示，不猜测数值。
- Android 8–12 不再被 Android 13 的 `POST_NOTIFICATIONS` 运行时权限检查误拦截；Android 13+ 仍必须取得该权限。
- `band-app/` 保持零改动，手环 10 Wearable SDK/RPK 路径未被替换。
- 新增 ADR-005，明确拒绝电脑蓝牙直连、直接移植 RPK和手机/云端访问 OpenAI 三条扩大风险的路线。

## 验证结论

- 根目录契约测试最终 42/42 通过。
- `git diff --check`、手环 8 静态接线、相对 Markdown 链接、无 ongoing/前台服务关键词和 `band-app` 零改动检查通过。
- Android `testDebugUnitTest`、`lintDebug`、`assembleDebug` 已尝试，但运行环境没有 Java 17/`JAVA_HOME`；同时没有 Android SDK 和私有 Wearable AAR，因此没有生成 APK，也没有把旧的 0.6.0 构建证据冒充本次结果。
- 手环 8 NFC 真机验收尚未执行，公开清单已写入 `docs/device-acceptance.md`。
## 2026-07-31 轻量 Android 工具链（官方来源）

- Android Developers 当前提供无需 Android Studio 的 Windows 命令行工具包 `commandlinetools-win-15859902_latest.zip`（155.7 MB），页面给出的 SHA-256 为 `90ae805d20434428bffcb699c290860f19bb5f66a67e6b330067e3de801fb04a`；包内 `sdkmanager` 可继续安装平台、构建工具与 platform-tools。来源：https://developer.android.com/studio
- Microsoft Learn 提供始终指向最新 Java 17 LTS Windows x64 ZIP 的官方链接 `https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip`，ZIP 可解压到任意目录后仅为当前命令设置 `JAVA_HOME`，不需要系统安装。来源：https://learn.microsoft.com/en-us/java/openjdk/download-major-urls 与 https://learn.microsoft.com/en-us/java/openjdk/install
- Microsoft Learn 页面在 2026-07-31 已更新为 OpenJDK 17.0.20；官方 major-version 页面说明在包 URL 后附加 `.sha256sum.txt` 可取得校验值。实下载 ZIP 为 17.0.20+8，SHA-256 `e46fd292317c6bb0a8fe9dc63115021329f3a63caeba791c185f89f3666a68e5`，与官方校验文件一致。来源：https://learn.microsoft.com/en-us/java/openjdk/download 与 https://learn.microsoft.com/en-us/java/openjdk/download-major-urls
- 项目 Wrapper 指定 Gradle 9.1.0。Gradle 官方校验页给出的 9.1.0 binary-only ZIP SHA-256 为 `a17ddd85a26b6a7f5ddb71ff8b05fc5104c0202c6e64782429790c933686c806`；官方安全指南建议校验分发包。来源：https://gradle.org/release-checksums/ 与 https://docs.gradle.org/current/userguide/best_practices_security.html
- Gradle 分发包的官方 GitHub 仓库为 `gradle/gradle-distributions`；在 Gradle CDN 路径被重置时，可尝试对应版本的官方 GitHub Release 资产，仍以 Gradle 官方 checksum 为最终信任依据。来源：https://github.com/gradle/gradle-distributions/releases
- Gradle 官方博客列出三个 Gradle 分发域名：`services.gradle.org`、`downloads.gradle.org` 和 `downloads.gradle-dn.com`；前两个在本机失败后，`downloads.gradle-dn.com` 是仍属于 Gradle 官方列出的最后一个独立端点。来源：https://blog.gradle.org/decommissioning-http

## 2026-08-01 AGP 9 内置 Kotlin 源目录

- Android Gradle Plugin 9.0 默认启用内置 Kotlin；自定义 Kotlin 源目录不能再加到 `AndroidSourceSet.java`，必须通过 `android.sourceSets` 中的 `AndroidSourceSet.kotlin.directories` 添加。官方迁移示例为 `android.sourceSets.named("main") { kotlin.directories += "additionalSourceDirectory/kotlin" }`。来源：https://developer.android.com/build/migrate-to-built-in-kotlin
- 本项目最初把手环 8/手环 10 的条件源码目录加到 `java.srcDir`，Gradle 给出弃用警告且 `compileDebugKotlin` 看不到 `XiaomiWearableBridge`；这与官方说明完全吻合，需改为 Kotlin 目录而不是退回旧 Kotlin 插件。

## 2026-08-01 Gradle Java 编译隔离

- Gradle 官方文档说明，`JavaCompile` 默认在构建进程内运行，并可通过 `tasks.withType<JavaCompile>().configureEach { options.isFork = true }` 改为独立进程。`forkOptions.executable` 可指定实际 `javac`，同时会关闭该任务的输出缓存。来源：https://docs.gradle.org/current/userguide/performance.html 与 https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/compile/ForkOptions.html
- 当前 `AccessDeniedException` 的 stacktrace 落在 JDK `ZipFileSystem.close()`，即 Gradle 清理编译类路径时关闭 JAR，而不是打开或读取 JAR 时失败；文件 ACL、备用数据流和只读打开均正常，且没有残留 Java 进程。
- Android 应用源码中没有 `.java` 文件；因此仅在本机验证用 init script 强制调用便携 JDK 的独立 `javac`，可隔离环境问题，且无需把沙箱规避写入项目的正式 `build.gradle.kts`。

## 2026-08-01 Band 8-only 构建结论

- `-PcodexQuotaBand8Only=true` 的 Android Debug 构建已真实通过：107/107 单元测试、Lint 0 error、`assembleDebug` 成功；根目录契约测试为 43/43。
- Lint 发现通知权限可能在检查后被系统撤销；发布动作现会把 `SecurityException` 安全转换为 `PermissionRequired`，并有回归测试覆盖。
- APK 已核对包名 `com.codex.quota.android`、版本 `0.6.0 / versionCode 600`、最低 Android 8.0（API 26）、目标 API 36 与 v2 调试签名。
- 当前 ADB 设备列表为空；代码与可安装包已就绪，尚不能把“构建成功”冒充为小米手环 8 NFC 真机验收成功。

## 2026-08-01 Mi Fitness 转发段复测

- 2026-08-02 用户确认手环已经正常收到消息；关闭 Mi Fitness 的“仅在手机锁屏时通知”后，全新 HIGH 任务通知成功转发，阶段 14 获得最终真机验收。
- 用户确认 Android 已发布的最终高优先级任务通知仍未到达手环，阶段 14 不能完成。
- ADB 当前稳定识别手机 `75328112 / 2410DPN6CC`；Mi Fitness 启动后直接进入“穿戴设备”页，说明应用账号和主页面可用。
- Mi Fitness 当前“设备”页显示“通知设置”入口，下一步从该入口核对 Codex额度 是否在单应用通知列表中启用，并检查转发条件与手环免打扰。
- “通知设置”总页显示“消息通知：已开启”和“来电提醒：已开启”；这只能证明总开关开启，尚不能证明 Codex额度 已被单独允许。
- Mi Fitness 页面再次对 UIAutomator 返回 `null root node`，没有生成 XML；已停止依赖无障碍树，改用原始 1440×3200 截图和只读系统状态，不把空树误判为页面异常。
- 进入“消息通知”后确认“自定义消息通知”已选中，且 Codex额度 的单应用开关已经开启；单应用白名单不是失败原因。
- 同页发现“仅在手机锁屏时通知”处于开启状态。此前高优先级实测时手机处于亮屏解锁状态，这一条件足以让 Mi Fitness 不向手环转发，属于当前最直接的根因证据。
- 已关闭“仅在手机锁屏时通知”，截图复核开关变为灰色关闭；现在允许手机亮屏和锁屏两种状态都向手环转发，其他应用的单独开关没有改变。
- 关闭限制后，手机到电脑的 WSS 仍为 `192.168.1.6 → 192.168.1.4:17322 ESTAB`；Codex额度设置页也显示“已同步 刚刚”，通知兼容模式和需要授权提醒均保持开启。
- Android 当前通知时机仍为“失焦”；为排除 ChatGPT 窗口焦点对本轮测试的影响，下一步临时切到“始终”，发布一个全新任务状态，再恢复“失焦”。
- 一次 `run-as ... sh -c` 的只读循环因 Android shell 引号被外层解析而报 `unexpected do`；没有改动应用数据。已从界面直接确认设置，不再重复该错误命令。
- 非 root 的 `ss -p` 输出提示 netlink 权限不足，但仍返回了连接表并明确显示目标 WSS 为 ESTAB；只采用连接状态，不依赖进程归属字段。
- 已通过 App 界面把通知时机从“失焦”临时切换到“始终”，截图确认选中项变化；这是为排除桌面 ChatGPT 焦点抑制，只用于本轮新通知测试，测试后恢复原设置。
- 搜索字面量 `--hook-event` 时漏加 ripgrep 的 `--` 参数终止符，被误解析为选项并返回 `unrecognized flag`；后续使用 `rg ... -- '--hook-event'`，不重复同一错误。
- Hook 测试入口已从源码复核：`CodexQuota.exe --hook-event --owner codex-quota` 从标准输入读取 UTF-8 JSON，最小任务字段为 `session_id`、`turn_id`、`hook_event_name`；`Stop` 会生成 `waiting_for_review`，适合把现有诊断任务从“需要授权”切成一条全新的“等待查看”通知。
- 第一条 `Stop` 诊断事件被 Windows 程序以退出码 0 接受，但发送时 Codex额度 Android App 正在前台；系统中没有新增 `waiting-for-review-v2` 活跃通知，符合“App 前台抑制提醒”的既有策略，不能拿它做手环转发测试。
- 已按 Home 键把 Android App 退到后台并确认桌面 `com.miui.home/.launcher.Launcher` 在前台；下一条会改回 `PermissionRequest` 形成新的状态跃迁和新通知。
- 本次后台后的非 root `ss -tn` 只返回 netlink 权限提示而没有目标连接行；需以随后 Android 新通知是否真实发布为最终同步证据，不把这次空筛选解释为确定断线。
- 同一诊断任务从 `Stop` 再切回 `PermissionRequest` 时只更新旧通知 ID；为排除 Mi Fitness 忽略通知更新，新增独立会话 `codex-quota-band8-diagnostic-2` 生成真正的新通知。
- 新诊断通知已在 App 后台、通知时机“始终”、Mi Fitness 锁屏限制关闭的条件下真实发布：新 ID `-479300275`，渠道 `needs-authorization-v2`，重要性 HIGH，标题“需要授权”、正文“任务”，`seen=false`，通知本体没有 `LOCAL_ONLY`。
- 手机通知栏截图已看到顶部新通知“需要授权 任务”，同时配额通知仍存在；当前已具备让用户只检查手环是否收到的完整对照条件。
- 展开通知栏后启动 App 时，HyperOS 仍保持通知面板覆盖；显式折叠状态栏后回到 Codex额度设置页，确认临时“始终”仍选中，准备恢复原“失焦”。
- 已通过 App 界面把通知时机恢复为原来的“失焦”，截图确认中间选项重新高亮；Mi Fitness 的“仅在手机锁屏时通知”继续保持关闭，这是本轮唯一保留的设置修正。

## 2026-08-02 Windows 显示手机配对状态

- 提交前检查：根 `npm test` 46/46 通过，`git diff --check` 通过；Windows `cargo fmt` 可启动，但同一命令链的离线 `cargo test` 因当前隔离 Cargo 缓存缺少 `base64` crates.io 索引条目而停止（`no matching package named base64 found`），不是源码编译诊断。将按既有受控联网方式补齐索引后重跑。

- README/README_EN 已更新：改为 goingogle fork 的 Releases 地址，删除“Band 8 尚未构建/验收”和“未提交未推送”等过期叙述，补充真实手环验收、Mi Fitness 锁屏通知限制、Windows 三态诊断与应用内二维码扫描说明；同时明确当前 fork 尚未发布 Release。

- Git 审计确认当前仓库根为 `C:/Users/Strol/Documents/Codex/2026-07-30/ni/work/codex-quota-band-`，分支 `main` 与 `origin/main` 同在提交 `27b3cfc`；远程 fetch/push 均指向用户 fork `https://github.com/goingogle/codex-quota-band-.git`。工作树包含此前全部未提交适配改动，不能只提交本轮 Windows 文件。
- README 当前存在过期叙述：仍称 Band 8 NFC“尚未构建/真机验收”，英文版也称“Nothing has been committed, pushed”；下载链接仍指向原作者 `Vincent-hechuan`。本次应同步改为 fork 地址、真实 Band 8 验收结论、Windows 三态诊断说明，并保留“尚未发布 Release”这一准确边界。

- 持久更新已获准并完成：旧安装 EXE 已备份到 `outputs/CodexQuota-0.6.0-installed-before-phone-status.exe`（原哈希 `010FF1E9...B2B3C39`），安装目录 EXE 已替换为已验证构建（哈希 `D36394E9...CBF3A3C`）；官方 DLL、卸载器、HKCU 开机启动与配对数据均未改动。
- 更新后的正式安装进程 PID 32972 已再次通过真实窗口截图：Android 手机“已配对 · 在线”。TCP 旁证同一 PID 同时监听 `0.0.0.0:17322`，并与手机 `192.168.1.6:34080` 建立连接；阶段 17 完成。

- 只读安装核对（获准提升权限）确认：现有安装目录包含旧 `CodexQuota.exe`（SHA-256 `010FF1E9...B2B3C39`）、正确的官方 `libunwind.dll`（哈希与候选一致）和卸载器；HKCU 开机启动明确指向该旧 EXE。若要重启后保留新功能，只需备份并替换这一份 EXE，不必改 DLL、注册表或重新配对。
- 最终工作树仍包含此前 Android/Band 8 适配的既有修改与本轮 Windows 修改；已再次确认 `band-app` 零改动。不会提交、推送或发布。

- 带 `--show-diagnostics` 的候选 EXE 已通过 71 项 Windows 测试并重建，SHA-256 `D36394E96569305B650E8064B638F655BB6FA58BAA6935BC2F31F77D5CBF3A3C`。获准更新候选后，Computer Use 精确定位唯一诊断窗口并截图确认：Windows 服务“运行中”、Android 手机“已配对 · 在线”、Codex 额度源“已同步”。
- 真实验收同时证明手机会在候选服务恢复后自动重连：启动最初仅监听 17322，打开诊断窗口时已转为在线。文档已同步三态含义、Band 8 NFC 真机验收结论与 `--show-diagnostics` 支持入口。

- 启动流程已核对：参数在 `main` 收集，`run(show_onboarding)` 在 APP、窗口类和托盘图标全部初始化后才进入消息循环；现有 `show_diagnostics_window()` 是幂等的（已有窗口则恢复并前置，否则创建）。安全接入点是在 `run` 增加 `show_diagnostics` 布尔值，并在托盘初始化后调用同一函数。
- 新增参数不需要新窗口类、协议、存储或权限，也不会绕过单实例锁；候选进程必须以该参数首次启动才能显示窗口。将先扩展根源码契约测试并观察红灯，再实现。

- 获准后已精确停止旧 PID 并启动候选版：PID 16804，候选路径正确，17322 已恢复监听；未删除配对或任务数据。
- Computer Use 的 `list_apps`/`list_windows` 只能返回可定位的顶层窗口，候选托盘进程没有顶层窗口，Windows 系统托盘本身也不在可定位窗口列表中；因此无法安全地靠猜坐标打开托盘菜单。API 仅允许针对已返回窗口操作，不能构造桌面/托盘假句柄。
- 当前 release 仅支持 `--show-onboarding`，调试构建才会响应 `--show-pairing`，没有 `--show-diagnostics`。要完成真实诊断窗口截图，可考虑增加一个显式、无副作用的 `--show-diagnostics` 启动参数，然后让候选版启动时直接显示现有诊断窗口。

- 兼容查询确认旧客户端正在运行：PID 27688，路径 `C:\\Users\\Strol\\AppData\\Local\\Programs\\CodexQuota\\CodexQuota.exe`。查询当刻 17322 无 TCP 连接，因此新候选版的首个真实期望状态是“已配对 · 离线”；这可验证持久配对凭据与实时在线连接没有再被混淆。
- 为真实 Windows 托盘/诊断窗口复核，已按要求读取 `computer-use` 技能完整说明；接下来会先读取其 guidance 与 confirmations，再通过官方包装器控制 UI。

- 已建立不覆盖已安装版本的候选运行目录 `outputs/CodexQuota-0.6.0-phone-status-windows-x64/`，包含 `CodexQuota.exe` 与已校验官方 `libunwind.dll`；两者哈希分别为 `22B0B35B...AD102F`、`13BF4E99...F4C2C`。
- release 客户端使用固定单实例互斥量 `Local\\CodexQuotaWindowsNative040`，没有 `--show-diagnostics` 参数；真实诊断窗口只能从托盘菜单打开。首个基于 CIM/TCP 的运行实例查询返回退出码 1 且无输出，尚不能据此判断客户端未运行，后续改用更兼容的 `Get-Process`/注册表/`netstat` 精确核对。

- rustfmt 1.97.1 官方组件已校验并加入便携工具链。首次 `cargo fmt --check` 仅要求把一个测试断言合并为单行；用 `apply_patch` 修正后格式检查通过，随后 Windows 71 项测试再次全绿。
- 第一次 release 命令误写 `--bin codex-quota-windows`，Cargo 明确提示实际自动发现的目标名是 `codex_quota_windows`；改用正确名称后 release 构建成功。EXE 为 7,630,336 bytes，SHA-256 `22B0B35B7523E881C93A7C1AD06309BF852858D4A480668EF2366FD707AD102F`。
- 仓库已有 NSIS 安装器脚本，会把构建产物重命名为 `CodexQuota.exe`，但现有脚本仍只打包 EXE、不打包 `libunwind.dll`；真实候选运行时需要继续把官方 DLL 与 EXE 放在同一目录，避免重复上游缺陷。

- 根 Node 契约测试 46/46 通过，新增用例 `the Windows client distinguishes pairing from live phone connectivity` 已包含在全量结果中；`git diff --check` 通过，仅有仓库既存的 LF→CRLF 提示。
- 当前便携 Rust 按 `profile=minimal` 组装，未包含 `rustfmt-preview`，因此 `cargo fmt` 命令不存在；Windows 源码差异已人工复核且排版符合现有风格。官方清单已定位 rustfmt 1.97.1 xz SHA-256 `59f0ed296d8cf2c7de6d9ae1c55399a55c9ebd858f63a9a69274583e5329b415`，如补装可做正式格式检查。

- 便携 CMake/Ninja 已解压且版本命令正常。PATH 同时包含 Rust、llvm-mingw、CMake、Ninja 后，Windows 原生完整测试全绿：71 passed、0 failed；其中 Windows UI 契约测试明确覆盖手机三态。阶段 16 的实现与 Rust 编译验证完成。

- 官方发布元数据确认便携依赖：CMake `4.3.3` Windows x86_64 ZIP（52,967,828 bytes，SHA-256 `935ade9e5e8723583c07f44c5592cea2a1c8f65c56ca7e07b34c025c880e0bd6`）与 Ninja `1.13.1` Windows ZIP（289,808 bytes，SHA-256 `26a40fa8595694dec2fad4911e62d29e10525d2133c9a4230b66397774ae25bf`）。

- llvm-mingw 已成功解压到工作区，`x86_64-w64-mingw32-clang --version` 为 22.1.8，且缺失的 `advapi32`、`ole32`、`oleaut32` 导入库均存在。解压验证命令因同时探测了包内并不附带的可选 `cmake.exe`/`ninja.exe` 而退出 1；Clang 本身正常。
- PATH、工作区、Codex 本地运行时及常见 Android SDK 位置均未发现 CMake/Ninja。由于 `aws-lc-sys` 使用 CMake 构建，还需要补充项目内便携 CMake（及 Ninja）后才能继续完整测试。

- 已从官方 GitHub release 下载 `llvm-mingw-20260616-ucrt-x86_64.zip`（187,504,083 bytes）；本地 SHA-256 `b9b68a4d276e16fa25802aaba458e4638f64b3884c290aaccdc2d87083b6ca35` 与 GitHub release API 的资产 digest 完全一致。

- `llvm-mingw` 项目说明其 Windows 原生 UCRT 包只需解压即可使用；截至 2026-08-02，最新稳定版是 `20260616`（LLVM 22.1.8），对应 64 位 Windows 包名为 `llvm-mingw-20260616-ucrt-x86_64.zip`。20260721 为 LLVM 23.1.0 RC1 预发布，未选用。

- Rust 官方 `windows-gnullvm` 文档确认：该目标需要 LLVM 系 C 工具链；纯 Rust 项目也可使用 `rust-mingw` + `rust-lld`。本地确实存在 `rust-lld.exe`，改用它后 Rust 代码开始正常编译，说明最初的默认链接器缺失已绕过。
- 当前项目并非纯 Rust：依赖树包含 `ring` 与 `aws-lc-sys`。`rust-lld` 继续链接其构建脚本时发现官方 `rust-mingw` 的 13 个自包含库里没有 `advapi32`、`ole32`、`oleaut32` 导入库，因此仍需要最小 LLVM/MinGW 工具链；这是构建环境缺口，不是手机配对状态实现的代码错误。

- 四个官方组件已在工作区组装成便携 Rust 1.97.1 工具链，`rustc --version` 与 `cargo --version` 均正常；未做系统级安装或 PATH 修改。
- Cargo 在受限网络中首次运行超时并出现 Schannel `SEC_E_NO_CREDENTIALS`，已改用获准联网方式；项目锁定依赖随后全部成功下载。
- 首轮 Rust 编译在 `x86_64-w64-mingw32-clang` 缺失处停止，并非本次代码编译错误。PATH、便携工具链、常见 Android NDK/LLVM/Visual Studio 路径均未找到 Clang/LLD；需要为 gnullvm 工具链补充一个项目内链接器。
- 列压缩包结构时再次因 `tar | Select-Object -First` 提前关闭管道而返回退出码 1；两份新组件 SHA-256 已单独确认与官方清单一致，此退出码不代表压缩包损坏，后续不再使用提前截断管道。

- 已从本地官方 stable 清单确认 2026-07-16 / Rust 1.97.1 gnullvm 组件：Cargo xz SHA-256 为 `765afd9396db3da57d3b28af85b1b955a142b3b4952b1001e5cc8cbaadc10cef`，rust-std xz SHA-256 为 `444f02927714ea5379c05c3d4b070398b9d79e35fb98a58d1466596ca7c4b9d4`。
- 本机已有 rustc 与 rust-mingw 压缩包的 SHA-256 分别匹配官方清单 `01f4f8d4...`、`d606e4fd...`；Cargo 与 rust-std 压缩包仍缺失，需要从 `static.rust-lang.org` 下载后再组装工作区内的便携工具链。

- 工作树包含此前手环 8 NFC 适配和扫码修复的未提交改动；本轮必须保留这些改动，不重置、不覆盖，并把 Windows 修改限定在 `windows-native/`、相关测试和文档。
- 项目文档说明 Windows 已有托盘菜单“连接与诊断…”：状态窗目前显示 Windows 服务、Android 手机和 Codex 额度源；另有“撤销手机配对”。因此不是从零增加状态，而是要核对当前窗口是否只显示在线状态、是否缺少“已保存配对凭据”的明确语义。
- 产品约束要求一次只配对一台手机，Windows 允许撤销当前手机；配对凭据状态与 WSS 当前在线状态是两个不同事实，新增显示不能把暂时离线误写成“未配对”。
- 仓库 `AGENTS.md` 已完整重读：Windows UI 可用简洁原生控件，但必须先定位现有实现和测试、先补测试再实现；用户明确验收前不提交、不推送、不发布。
- 当前托盘顶部只按 `active_connections > 0` 显示“手机 已连接 / 手机 未连接”；诊断窗的 `phone_connection_label` 也只返回“已连接 / 未连接”。因此手机暂时离线时，电脑端无法判断仍已配对，正是用户所问的缺口。
- 已有可信配对事实来源：`WssService.phone_token_hash()` 和 `PairingManager.phone_token_hash()`；`WindowsHost` 启动时从 DPAPI 保护的 `phone-token-hash-v1.bin` 恢复，撤销配对时清空。无需增加新存储或暴露令牌，只需读取“是否存在”布尔值。
- 现有单测 `diagnostics_reports_observable_phone_and_upstream_states` 只断言连接数 0/1；可先扩展为三态：未配对、已配对但离线、已配对且在线，再接入托盘与诊断窗。
- 一次宽泛 `rg ... | Select-Object -First 500` 因提前截断管道让 `rg` 返回退出码 1，且输出被工具截断；已改成针对 `PhoneTokenHashStore`、`active_connections` 和标签函数的聚焦搜索，不重复该宽泛管道。
- `AppController` 当前只暴露 `active_connections()`；`WindowsHost` 已可访问 `WssService`，但没有公开布尔型配对查询。最小实现是新增只返回 `phone_token_hash().is_some()` 的方法，不读取、不显示哈希内容。
- 实际使用的诊断绘制函数在窗口里画三行固定项目，Android 手机一行可直接改为三态而不增加窗口高度：`未配对`、`已配对 · 离线`、`已配对 · 在线`。托盘顶部同步显示 `手机 未配对 / 手机 已配对 · 离线 / 手机 已配对 · 在线`。
- 旧的双态标签函数有同文件内单测，适合先把测试改成三态并确认红灯，再实现 AppController/Host 接线；不需要改同步协议、Android 或手环代码。
- 阶段 15 的最小设计已确定：不增加新窗口和新持久化，只把现有托盘首行与“连接与诊断”手机行改为同一三态模型，并从当前内存中的配对哈希存在性读取布尔值。
- 当前 PATH 中没有 `cargo` 或 `rustc`，工作区只发现 Android 工具链目录；代码仍可按先红后绿修改，但完整 Windows Rust 测试/构建需要先查找此前官方 Rust 包是否保留可用工具链，或补一套工作区便携 Rust。
- 前一轮为补 `libunwind.dll` 解压的官方 `rustc 1.97.1 x86_64-pc-windows-gnullvm` 组件仍在 `work/windows-runtime-extract/`；该归档看起来包含 rustc 组件目录，但尚未确认是否含 Cargo。项目固定 `stable-x86_64-pc-windows-gnullvm` minimal 工具链，因此可优先复用同目标组件，避免改项目目标或使用 MSVC。
- 对该提取目录按 `rg --files` 过滤可执行文件时返回退出码 1，随后递归枚举也没有发现任何 `.exe` 或安装脚本；说明前一轮很可能只提取了运行库子目录/文件，不是可直接运行的完整 Rust 工具链。后续不把它误当 Cargo 环境。
## 2026-08-02：GitHub 同步结果

- 已核对目标仓库为用户 fork：`origin` 的 fetch/push 均指向 `goingogle/codex-quota-band-.git`，当前分支为 `main`。
- 中文 `README.md` 与英文 `README_EN.md` 已更新，说明 Band 8 NFC 真机验证、Windows 手机配对三态、应用内扫码、Mi Fitness 锁屏转发限制和当前 fork 尚未发布 GitHub Release。
- 提交前验证通过：根 Node 契约测试 46/46；Windows Rust workspace 测试 71/71；`cargo fmt --all -- --check`；`git diff --check`。离线 Rust 尝试因隔离缓存缺少 `base64` 索引失败，联网重试通过。
- 首次提交因本机没有 Git 作者配置而失败；未改动全局配置，仅为本仓库设置 `goingogle <goingogle@users.noreply.github.com>`。
- 首次推送因 Windows Schannel `SEC_E_NO_CREDENTIALS` 失败；改用 OpenSSL 后端推送成功。初次远端 commit 为 `10a84c9`，随后通过普通合并保留历史；最终本地与远端 `origin/main` 均为 `2c6927cc70f3115c3be13643c354a78145e4b9c4`。
- `C:\tmp` 仍保留官方 2026-07-16 stable 清单、完整 82.5 MB rustc 归档和 639 KB rust-mingw 归档；tar 清单确认完整 rustc 归档内实际含 `rustc.exe`/`rustdoc.exe`，先前只是只提取了运行库。
- 官方清单给出同目标 Cargo 1.97.1 xz 包及 SHA-256；完整离线/便携工具链还需 Cargo 和目标 `rust-std` 组件。应按清单精确提取对应 URL/哈希，只下载缺失组件，并安装到工作区目录而非系统。

## 2026-08-02：创建 GitHub Release 的初步核对

- 用户已明确授权创建 GitHub Release；这是对之前“只同步源码、不创建 Release”边界的新增任务。
- 当前项目没有独立的 release checklist 或自动发布工作流；Windows 正式安装器脚本需要 Cargo 和 NSIS，Android 标准 release 需要私有 Wearable AAR 与固定签名，手环 RPK 需要与 Android APK 一致的发布证书。
- 仓库文档仍把 `0.6.0` 描述为本地候选，并记录旧构建数量/哈希；创建 Release 前必须以当前源码重新核对资产，不能直接沿用旧候选哈希。
- 当前 README 已明确手环 8 NFC 使用 Android 通知兼容模式；若没有私有 AAR 或正式签名材料，Release 资产应明确标为 Band 8-only 通知构建或源码发布，不能把 debug APK 冒充标准正式 APK。
- 在整个工作区递归查找多个工具可执行文件超过命令时限，已主动终止；后续只检查已知目录或使用 `Get-Command`，不再对整个工作区做宽泛递归。
- 已确认工作区便携 Rust 工具链可用：`work/rust-toolchain-1.97.1/bin/cargo.exe`；便携 Android 工具链和 Gradle 9.1.0 也存在。系统 PATH 没有 Cargo、NSIS 或 GitHub CLI。
- 当前仓库只保留 Android debug APK 构建输出，Windows release target、安装器 dist、Band RPK 和正式 APK 输出均不存在；需要按发布范围重新构建或明确缺失。
- 仓库根目录没有 `rust-toolchain.toml`；Windows 构建应继续使用此前验证过的便携 Cargo 路径与 `--target-dir`，不能依赖隐含 toolchain 配置。
- Android `build.gradle.kts` 要求 release signing 四项配置；没有 `android-app/local.properties` 或 AAR 时只能构建 Band 8-only debug，标准 Band 10 release 不能声称可发布。
- Windows release 构建首次失败于 `linker x86_64-w64-mingw32-clang not found`；便携 LLVM-MinGW 位于 `work/llvm-mingw-20260616-ucrt-x86_64`，下一次构建显式加入其 `bin` 到 PATH。
- 显式加入 LLVM-MinGW 后 Windows release 构建成功；产物为 `rust-target-phone-status/release/codex_quota_windows.exe`，7,629,824 bytes，SHA-256 `63C0FB203E9D1BF2FC0105DE42253904056293DD8990443CCC03663E88E4BD90`。release 目录未自动携带 `libunwind.dll`，发布包需一并放入已核验的 DLL。
- 当前 Android 输出为 `app-debug.apk`，包名 `com.codex.quota.android`、版本 `0.6.0 / 600`，大小 46,935,400 bytes，SHA-256 `81F561E224BA2BD2173F95C58055315CBBCA29FF81C4B482467181B25E8C5119`；需重新用 Band 8-only 参数跑测试/构建后再作为预发布资产。
- 已整理预发布资产：`CodexQuota-0.6.0-Windows-x64-portable.zip`（包含 `CodexQuota.exe` 与 `libunwind.dll`，SHA-256 `1339CE5126DB7E358C737226D65135B0268E541862BE18159544F50274D4376A`）、`CodexQuota-0.6.0-band8-nfc-debug.apk`（SHA-256 `81F561E...C5119`）和 `SHA256SUMS.txt`；ZIP 内容已核对。
- 发布说明必须明确这是 `v0.6.0-band8-nfc-preview` 预发布：Windows 是便携 ZIP（非 NSIS 安装器），Android 是 Band 8-only debug-signed APK；不提供 Band 10 RPK 或标准签名 APK。
- GitHub Release 已创建：tag `v0.6.0-band8-nfc-preview`，Release id `363593611`，状态为 `prerelease=true`、`draft=false`，目标为 `main`。
- 三个资产均已上传并返回 `state=uploaded`：Windows ZIP 2,904,800 bytes、Band 8-only APK 46,935,400 bytes、`SHA256SUMS.txt` 433 bytes；远端下载 URL 已取得。
- GitHub API 返回的远端资产 digest 与本地完全一致：APK `81f561e...c5119`、Windows ZIP `1339ce...4376a`、校验文件 `85248b...56d8c`；最终本地与远端 `main` 均为 `b19718ad3f9c39dc14127b141fbaf48159a0ed3f`。
- 远端 lightweight tag 查询返回 tag object `913bbedd...3880a`；本地 annotated tag 解引用到发布提交 `3b38bcbfafd649199497f098baae6221528f5ec9`，与 Release 创建时的源码提交一致。
- 最后一次同时查询 tag 解引用时误用了 PowerShell 会解释的 `^{} ` 语法而失败；改为分别读取 `main` 和 tag ref 后核对成功。
- 一次提交前复核命令误用 PowerShell 7 的 `||` 语法，当前 PowerShell 解析失败且未执行任何检查；改用兼容 PowerShell 5 的分步命令。
- Git credential helper 使用 Windows Credential Manager；系统没有 `gh`，可在受控网络权限下通过 GitHub REST API 使用已配置的 Git 凭据创建 Release，令牌不得输出到日志。
- 在 escalated 用户上下文中直接从仓库工作目录读取 tag 触发 dubious ownership；从父目录调用 `origin` 又因没有仓库配置失败；最终改用远端 HTTPS URL 直接查询，确认没有现存 `v0.6.0*` tag。
- Android Band 8-only wrapper 命令因 Gradle Wrapper 未命中工作区已解压的 Gradle 9.1.0，尝试联网下载且被沙箱拒绝；改用 `work/android-toolchain/gradle-runtime/gradle-9.1.0/bin/gradle.bat` 直接执行。
- 直接 Gradle 命令在 5 分钟内没有输出且未更新 APK，已终止本次受控构建；未发现残留 Java 进程。此前已完成的 Band 8-only Android 107/107、Lint、assemble 证据仍可用于预发布说明，但本轮不把长时间无输出误报为新构建通过。
- 代码核对确认手环 10 路径仍保留：标准构建在 `band8Only=false` 时要求 `android-app/app/libs/xms-wearable-lib_1.4_release.aar`，使用 `src/wearableSdk/java`；Band 8-only 才切换到安全空实现。当前预发布不包含手环 10 RPK/标准 APK，只是发布资产范围限制。
- GitHub Release API 读取到正文中的中文已被存成 `????`，不是显示端字体问题；原因是之前通过 PowerShell 命令行内联中文提交正文时发生字符编码损失。应改为从 UTF-8 文件读取正文后 PATCH Release。
- 首次 PATCH Release 正文使用整篇文档时被 GitHub 拒绝 `body is too long (maximum is 125000 characters)`，尽管本地文档应很短；需先检查 PowerShell 读取后的实际字符长度，避免把异常对象或编码内容提交到 API。
- 将正文强制转换为 `[string]` 并使用 `[ordered]@{body=...}` 后，UTF-8 PATCH 成功；API 返回正文长度 933，中文已正确保存。
- GitHub 页面通过浏览器工具读取时返回 Cache miss；以 GitHub API 真实响应为准，不把浏览器缓存失败当作 Release 内容证据。
- 最终 API 核验：Release 正文长度 933，`HAS_QUESTION_MARKS=False`，包含“手环 10 支持”，仍为 `prerelease=true` 且有 3 个资产；本地与远端 `main` 已推送至最终提交 `758fd54b896a9f79f68ca81c52ff891333df0e8f`。
