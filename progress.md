# 手环 8 NFC 通知模式工作进度

- 2026-08-02：README 更新后的根测试 46/46 通过；Rust 离线测试只因隔离 Cargo 缓存缺少 `base64` 索引条目失败，准备联网补齐后再提交。

- 2026-08-02：中英文 README 已完成同步更新，下一步运行 Node/Rust 检查并审阅最终提交范围。

- 2026-08-02：Git 远程已核对为用户 fork `goingogle/codex-quota-band-`，当前 `main` 与远端基线一致但工作树有完整未提交改动。README 中的旧验收/原作者链接信息需要先更新，再进行全量测试、提交和推送。

- 2026-08-02：用户明确授权把当前项目同步到 GitHub 并更新 README。新增阶段 18—20；先审计远程与工作树，保留此前 Android/Band 8/Windows 全部改动，不做重置。

- 2026-08-02：已把通过测试的 Windows EXE 持久更新到原安装目录，旧版已备份。正式安装版真实诊断窗口和 TCP 均确认手机“已配对 · 在线”；阶段 17 完成，未提交、推送或发布。

- 2026-08-02：安装目录与开机启动路径核对完成；持久更新可安全限定为“备份旧 EXE → 替换为已验证 EXE → 保留现有 DLL/卸载器/注册表/配对数据”。

- 2026-08-02：真实 Windows 诊断窗口验收通过，现场显示“Android 手机：已配对 · 在线”“Windows 服务：运行中”“Codex 额度源：已同步”；三态文档已补齐。下一步仅剩最终全量检查与决定是否覆盖安装目录。

- 2026-08-02：候选版已获准启动且 17322 正常监听。Windows 界面工具不能定位系统托盘，停止猜坐标操作；准备审阅启动流程，若边界安全则增加 `--show-diagnostics` 参数以直接打开既有诊断窗口完成真实截图。

- 2026-08-02：已精确定位运行中的旧 Windows 客户端；当前手机实时 WSS 离线，候选版首个真实验收目标为“已配对 · 离线”。开始使用 Windows 界面控制能力复核托盘与诊断窗口。

- 2026-08-02：rustfmt 检查通过，格式修正后 Windows 71 项测试再次通过；release EXE 已成功构建（7,630,336 bytes，SHA-256 `22B0B35B...AD102F`）。下一步制作带 `libunwind.dll` 的候选运行目录并真机检查托盘/诊断窗口。

- 2026-08-02：根 `npm test` 46/46 与 `git diff --check` 均通过；便携最小 Rust 未带 rustfmt，已定位官方对应组件，等待补齐格式检查后构建 Windows EXE。

- 2026-08-02：工作区便携 Rust 1.97.1、llvm-mingw 20260616、CMake 4.3.3 与 Ninja 1.13.1 已组装并按官方元数据校验；Windows 原生 `cargo test --workspace --locked --offline` 全量通过，共 71 项、0 失败。阶段 16 完成，阶段 17 进入根测试、构建与真实托盘界面复核。

- 2026-08-02：用户确认手环 8 NFC 已正常收到新任务通知；Mi Fitness → 手环链路真机验收通过，阶段 14 完成。用户新增需求：电脑客户端显示手机配对情况；已新增阶段 15–17，先区分“保存过配对凭据”和“手机当前在线”。
- 2026-08-02：已重新读取 `planning-with-files` 说明并恢复项目规划上下文；当前进入阶段 15，只读审阅 Windows 配对状态和托盘 UI，尚未修改产品代码。
- 2026-08-02：规划技能要求已落实：新增阶段、记录真机结论，并将在每两次代码/UI 检索后写入发现记录；已知损坏的 `session-catchup.py` 不再重复调用。
- 2026-08-02：继续完整读取规划技能说明；本任务沿用项目根目录的 legacy 规划文件，不启用自动循环、目标或 gated 模式。
- 2026-08-02：`planning-with-files` 455 行已完整读到末尾；同时开始重新读取仓库 `AGENTS.md`，确认 Windows 托盘入口和“先测试后实现、未验收不提交发布”的约束。
- 2026-08-02：已先新增 Windows 手机三态的根契约测试，并把 Rust 单测期望改为“未配对 / 已配对·离线 / 已配对·在线”。定向 Node 测试按预期失败于 `WindowsHost.phone_paired()` 尚不存在，红灯成立，开始正式实现。
- 2026-08-02：已实现 `WindowsHost.phone_paired()` 安全布尔查询和 AppController 接线；托盘与诊断窗现在共享三态文案。定向 Node 契约测试转绿，`git diff --check` 通过（仅现有换行提示）。阶段 15 完成，阶段 16 等待 Rust 编译/单测确认。
- 2026-08-01：用户实机确认最终的高优先级“需要授权 / 任务”测试通知仍未到达手环。Android 已真实发布通知、Mi Fitness 全局通知监听已启用，因此阶段 14 继续进行；下一步只核对 Mi Fitness 的 Codex额度 单应用转发、锁屏/息屏条件和手环免打扰，再发送全新的对照通知。
- 2026-08-01：定位到 Mi Fitness 开启了“仅在手机锁屏时通知”，而此前测试均在亮屏解锁状态；Codex额度 单应用开关本身已开启。已关闭该限制，并在 App 后台创建全新诊断会话，手机端出现不同 ID 的 HIGH“需要授权 / 任务”通知。临时“始终”已恢复为原“失焦”，当前等待用户确认手环是否收到新通知。

- 2026-08-01：第二轮真机反馈确认三类问题：Band 8-only 设置仍混入手环 10 项、首页错误显示“手环未连接”、任务状态未出现且系统通知未被 Mi Fitness 转发。已将后续拆为 UI 语义、任务 Hook、通知转发三阶段，当前先以测试约束 Band 8-only UI。
- ADB 已确认 Mi Fitness 通知监听服务和 CodexQuota 通知权限均已启用；配额通知确已进入 Android 通知管理器，但被归入低重要度静默分区。UI 根因也已定位到未区分构建模式的手环 10 直连状态和设置项。
- 已先补 Band 8-only 首页状态、手环 10 控件可见性、BuildConfig 接线和配额渠道 v2/default 重要度测试；根契约测试与 Android 编译均按预期失败，红灯原因就是新实现尚不存在。
- 已实现 Band 8-only UI 分流和配额渠道 v2；相关根测试 3/3、Android 定向单测通过。阶段 12 完成，进入任务同步诊断。
- 任务同步根因已确认：用户 `.codex\hooks.json` 不存在，Windows 本地目录也没有任何 Hook 调用记录。下一步运行产品自带的“安装/修复任务 Hook”，再验证四项安全事件处理器和真实任务事件。
- 产品自带 Hook 安装已成功，四项处理器均已核对。手机端任务与 Band 8 兼容开关也均已开启；准备发送一次可清理的本地诊断事件验证端到端任务同步。
- 首次诊断进程因本机 PowerShell 不支持 `ProcessStartInfo.ArgumentList`，实际只启动了无参数 EXE，事件未发送；已记录并切换到兼容的固定 `Arguments` 方式。
- 兼容重试已被 Hook 状态文件确认为 `accepted` 且队列已由 Windows 消费。手机仍没有任务通知，进一步发现 App 当前仅显示“缓存 9分”，说明诊断时电脑—手机实时连接已中断；下一步先修复现有配对的 WSS 重连。
- WSS 已自行重连：电脑 17322 正在监听并与手机建立连接，手机恢复“已同步 刚刚”。诊断任务仍未形成系统通知，正在区分“任务快照未下发”与“通知策略未触发”。
- 手机任务页已显示诊断任务“需要授权”，任务同步端到端确认成功，阶段 13 完成。阶段 14 开始：全量验证、构建并安装带 Band 8 UI 分流和通知渠道 v2 的 APK，再做实时通知验收。
- 完整验证通过：Android 单测、Lint、assemble、根测试 45/45 和 diff 检查均成功。新 APK 已覆盖安装；设置页手环 10 项已隐藏，v2 配额通知已按默认重要度、无声、无手机振动发布。
- 新版首页也已真机确认：显示“手环 8 通知 / 转发已启用”和当前诊断任务，阶段 12/13 的用户可见结果均正确。准备在 WSS 稳定、手机熄屏条件下把诊断任务切为“等待查看”，验证实时任务通知和 Mi Fitness 转发。
- “等待查看”变化已由 Windows 接受，但手机 App 当时仍处于前台，按产品策略没有新增系统通知；这不是转发失败证据。下一次先让 App 退到后台，再切回“需要授权”。
- 手机退到桌面后的“需要授权”变化也按设计未提醒，因为 Windows 端官方 Codex 正在前台，而通知时机是“失焦”。下一次临时选择“始终”完成可控验收，随后恢复用户原设置。
- 临时设置首次点击因坐标转换错误未生效，偏好仍为 `Unfocused`；已取得“始终”控件的准确真机边界，未误改其他设置。
- 临时 `Always` 已确认生效，手机桌面前台时实时发送“等待查看”，但系统仍未出现任务通知。继续定位 Android 前后台状态接线/dispatcher，不把问题误归给 Mi Fitness。
- 已确认真实阻断点：App 进后台后电脑侧 WSS 连接消失，任务只能在重新打开 App 时以重连快照出现，因此不会触发实时通知。下一步恢复原“失焦”设置，并检查 HyperOS 后台电量/自启动限制。
- 原“失焦”已恢复。46 秒后台监测中进程一直存活但 WSS 始终断开，确认是后台执行/网络冻结型问题；正在检查 HyperOS 厂商专有省电与自启动设置。
- 已把“Codex额度”应用详情页打开到手机，但系统当前锁屏/显示层无法读取，标准唤醒重试仍为空；需用户解锁屏幕后继续检查并调整 HyperOS 后台策略。
- 截图确认系统页面实际可见：自启动当前关闭，通知已允许。准备开启自启动并进入省电策略选择“不限制”，随后重复 46 秒后台 WSS 测试。
- 第一次按缩略图坐标点击误进“应用联网”，没有改动数据卡/WLAN 开关；已换算手机原始 1440×3200 坐标，返回应用详情后继续。
- 自启动已开启，当前停在 HyperOS 说明弹窗；继续确认并进入“省电策略”。
- 已进入省电策略并确认当前是会限制后台联网的“智能限制后台运行”。准备改为“无限制”并重新测后台 WSS。
- “无限制”已选中且自启动已开启；开始重复同样的 46 秒后台连接监测。
- 后台连接复测通过：50 秒、10/10 次均保持 WSS 在线，修改前为 0/10。后台任务同步前置条件已修复，进入最后一次实时通知与手环观察。
- 最终通知测试前 USB ADB 设备临时消失，临时 `Always` 没有被写入；后台 WSS 修复证据不受影响。正在重新检测 ADB，恢复后继续，不会把未执行的测试报成成功。
- ADB 随后恢复；临时 Always、App 后台和 WSS 在线均确认。最终“需要授权 / 任务”已作为高重要度、可穿戴转发的系统通知真实发布，原“失焦”设置随后恢复。
- 当前只等待用户肉眼确认手环是否收到这条通知；在此之前阶段 14 保持进行中，不把 Android 发布成功冒充为手环接收成功。

- 2026-08-01：用户在真机发现扫码入口只会拍照、不会识别二维码。已定位为普通相机 Intent 误用，阶段 10 转为测试优先修复扫码、重建并覆盖安装；Mi Fitness 与手环验收顺延到阶段 11。
- 已新增扫码结果白名单单测和 Android 构建契约测试；契约测试按预期因扫码 Activity 尚不存在而失败。Android 单测的首次“红灯”编译长时间无输出后已终止，不重复该空等路径。
- CameraX/ML Kit 依赖已从官方仓库下载成功；首次正式编译定位到两个 API 级别错误，已按 CameraX 1.6.1 和新版 Android 类型要求修正。
- 修正后 Android 单测 109/109 通过。源码复核发现 CameraX 1.6.1 已把坐标常量迁移到 `ImageAnalysis`，已切换导入以消除弃用警告。
- 新 APK 覆盖安装成功，通知权限保留，相机权限由系统授权；真机 Activity 栈确认扫码页启动后立即结束，已进入设备级 CameraX 初始化异常诊断，尚未把阶段 10 标记完成。
- 真机日志定位缺少 Camera2 后端；按红绿流程补依赖契约并加入 `camera-camera2:1.6.1`。使用原调试证书重新构建后覆盖安装成功，扫码预览稳定显示并读取新 QR；Android 设置页已显示“已同步 刚刚”，阶段 10 完成。
- 通知兼容模式已确认持久化为开启；真机发布测试通知成功，内容为“5小时 暂无数据 · 周额度 66%”，等待用户确认手环 8 NFC 是否收到。
- 最终全量验证：Android 单测 109/109、根测试 44/44、Lint 0 问题、Band 8-only APK 构建均通过。最终 APK 为 46,881,543 bytes，SHA-256 `9990BEA6998E932901892ABE61047B01146E664E87ADC6CD94790F36941D2972`，位于 `outputs/CodexQuota-0.6.0-band8-nfc-qrfix-debug.apk`。

## 2026-07-31

- 已读取 `planning-with-files` 技能和仓库 `AGENTS.md`。
- 已确认仓库工作树干净，未发现已有用户修改。
- 已建立本次适配的任务计划、发现记录和进度日志。
- 当前阶段：阅读现有产品语义、通知策略、设置存储与 Compose UI。
- 已阅读 README、CHANGELOG、CONTEXT、当前状态、架构、开发与构建验证文档。
- 已确认实现必须复用 Android 原生通知，且不能新增常驻前台通知。
- 已定位通知设置、Compose 设置页、Application 装配、任务通知和运行时状态仓库的实现与测试位置。
- 已读完通知设置/存储、通知渠道、任务 dispatcher/coordinator、Application、Activity 和运行时仓库关键实现。
- 初步决定使用独立低重要度配额渠道与独立手环 8 dispatcher，避免改变手环 10 和手机通知语义。
- 已阅读现有通知单测和 `NotificationPolicy`；确定使用纯 Kotlin 内容/阈值策略测试，避免引入 Robolectric。
- 阶段 1 完成；进入阶段 2，先新增手环 8 配额通知内容、自动触发和任务兼容通知测试。
- 已新增失败测试：配额通知格式、缺失数据语义、有效快照筛选、75/50/25/10 阈值、重置周期、低重要度渠道和独立任务兼容通道。
- 测试环境检查确认缺少 Java 17、Android SDK 和私有 Wearable AAR；新增 Android 测试无法在本机实际启动，已记录为验证边界。
- 已实现纯 Kotlin 配额通知内容、有效快照信号、自动阈值策略和自动提醒协调器。
- 已增加低重要度无振动的“手环 8 配额状态”通知渠道，以及可被 Mi Fitness 转发的非 `localOnly` 配额通知。
- 已扩展任务通知策略为独立 band8 通道；兼容模式启用时，任务通知会以可桥接的系统通知发布，同时保持手环 10 Wearable dispatcher。
- 已在设置页增加“小米手环 8 NFC”分组、兼容开关和“立即发送配额”动作，并把原有入口明确标为手环 10 应用连接/提醒。
- 首轮静态复核完成：构造点搜索完整，`git diff --check` 通过；待补协调器去重测试并修整协程/锁实现。
- 已补自动提醒协调器的启用、重复抑制和阈值跨越测试；通知发布移到同步锁外。
- 阶段 2 完成；进入阶段 3 的集成复核与边界修正。
- 复核 `CONTEXT.md` 后发现 Android UI 也需先预览确认；已撤回正式设置页和 Activity 的可见接线，底层通知实现与测试保留。
- 当前暂停点：制作并展示“小米手环 8 NFC”设置分组的单方案预览，等待用户确认后再接入 Compose。
- 已生成设置页 SVG 预览源；本地图片查看器不能直接处理 SVG，正在转换为 PNG 以便视觉复核和展示。
- 浏览器不允许打开本地 `file://` 预览，已按安全策略停止且未尝试规避；将直接在对话中以内联本地 SVG 展示。
- 用户已确认“小米手环 8 NFC”设置分组预览；正式 Compose/Activity 接线已恢复，开始补齐说明文档和验证。
- 已完成中文/英文 README、产品约束、架构、当前状态和真机验收清单更新；手环 8 NFC 明确标记为通知兼容且待真机验收。
- 首次根契约测试因工作树没有安装 `ajv`、`qrcode` 而只运行到 26 项通过、5 个测试文件加载失败；准备安装最小生产依赖后重跑。
- 已安装仓库声明的最小生产依赖并重跑根契约测试：42/42 通过。
- 已执行 Android `testDebugUnitTest + lintDebug + assembleDebug` 验证命令；环境在启动 Gradle 前因没有 `JAVA_HOME`/Java 17 失败，且 Android SDK 与私有 Wearable AAR 也不存在。
- 已新增 ADR-005，并同步更新安全边界、Agent 入口和开发验证矩阵。
- 最终静态检查通过：手环 8 接线、默认关闭、可桥接通知、无 ongoing/前台服务、文档相对链接和 `band-app` 零改动均符合预期。
- 最终根契约测试再次通过 42/42；阶段 1–5 完成，下一步仅剩具备依赖后的 Android 构建和小米手环 8 NFC 真机验收。
- 用户明确同意使用 VS Code + 便携 Java/Android SDK 的轻量方案；新增阶段 6–10，先实现显式 Band 8-only 构建，再配置工具链、生成 APK 和真机验收。
- 已完成显式 `-PcodexQuotaBand8Only=true` 构建模式：手环 8 路径不加载私有 Wearable AAR，默认手环 10 构建仍会在 AAR 缺失时明确失败。
- 已将直接 Xiaomi Wearable SDK 桥接与手环 8 安全空实现分离，并保留共用的手环 10 负载生成代码与测试入口。
- 新增根契约测试并按红绿流程验证；完整根测试现为 43/43 通过，`git diff --check` 通过。
- 文档已说明轻量构建命令、私有 AAR 边界与“不需要 Android Studio”。
- 已从微软官方稳定链接下载便携 OpenJDK 17，官方 SHA-256 校验通过；实际版本为 17.0.20+8，解压在工作区 `work/android-toolchain/jdk-17/`，未安装到系统。
- 便携 Java 已能启动 Gradle Wrapper，但 Gradle 9.1.0 分发包在 Gradle CDN 被连接重置、在官方 GitHub Release 连接超时；本机也没有可复用缓存，已停止重复下载。
- 阶段 7 当前暂停在 Android SDK 许可确认；确认后先验证 Google 官方命令行工具下载链路，再继续完整构建。
- 用户已明确同意 Android SDK License，并授权下载 Android 命令行工具及构建所需 SDK 包。
- Google 官方 `commandlinetools-win-15859902_latest.zip` 已下载成功，155,655,386 字节，SHA-256 与官网 `90ae805d...fb04a` 完全一致；已解压到工作区 Android SDK 的 `cmdline-tools/latest/`。
- 已根据用户授权接受 7/7 SDK 包许可，并在工作区安装 `platforms;android-36`、`build-tools;36.0.0` 和 `platform-tools 37.0.1`。
- 便携工具实测可运行：OpenJDK 17.0.20、ADB 1.0.41/37.0.1、AAPT2 2.20；阶段 7 完成，系统级安装和环境变量均未改变。
- 阶段 8 已开始，但 Gradle 9.1.0 尚未取得：命令行连接 Gradle CDN/GitHub 失败，应用内浏览器访问 GitHub被安全策略拒绝，最后的官方备用端点检查又因 Codex 工具调用额度上限被自动审批拒绝。
- 当前唯一需用户手动完成的动作：从 Gradle 官方链接下载 `gradle-9.1.0-bin.zip` 到 `work/android-toolchain/`；之后可继续本地官方哈希校验、解压和 Android 构建。
- 用户手动放入的 Gradle 9.1.0 ZIP 已通过官方 SHA-256 校验并成功运行；首轮构建下载约 697 MB 工作区依赖后，暴露出 AGP 9 内置 Kotlin 不读取 `java.srcDir` 的真实编译错误。
- 已按 Android 官方 AGP 9 规则把条件源码切换为 `AndroidSourceSet.kotlin.directories`，契约测试转绿，`:app:compileDebugKotlin` 离线构建成功。
- Android 单测随后进入 `compileDebugJavaWithJavac`，两次在 Gradle 关闭 `kotlinx-serialization-core-jvm` 的 ZIP 文件系统时触发 Windows `AccessDeniedException`；依赖文件自身与权限均正常，已确认这是本机构建进程清理问题而非代码编译错误。
- 用工作区 Gradle init script 强制独立 `javac` 后，Java 编译成功；该环境规避没有写入项目正式构建配置。
- 完整 Android 单测 107/107、Lint（0 error）和 Band 8-only `assembleDebug` 均通过；根目录契约测试 43/43 再次通过。
- Lint 暴露通知权限撤销竞态；已按红绿流程补测试并捕获 `SecurityException`，测试和 Lint 均转绿。
- 已生成并校验 debug APK：包名 `com.codex.quota.android`、版本 `0.6.0 / 600`、v2 调试签名、19,858,752 bytes、SHA-256 `d25937d9c5c5656e3ef0dec4fbf66673f9d6dc8af315693b1f1ee471c273c5c2`；副本位于 `outputs/CodexQuota-0.6.0-band8-nfc-debug.apk`。
- ADB 服务已正常启动，但设备列表为空；阶段 8 完成，阶段 9 等待用户连接手机并授权 USB 调试后安装。
- 用户回复已连接并授权后再次检测，ADB 仍为空，Windows 也没有枚举到 Android、ADB 或 MTP 设备；这不是 APK 或授权按钮的问题，需先恢复 USB 数据连接。
- 用户切换“文件传输”后已识别设备 `2410DPN6CC`（型号 `2410DPN6CC`），且未安装同包名应用。首次安装被手机以 `INSTALL_FAILED_USER_RESTRICTED` 取消，等待用户开启小米/HyperOS 的“通过 USB 安装”权限后重试。
- 用户开启“通过 USB 安装”后 APK 安装成功；手机端实际包版本已核对为 `0.6.0 / versionCode 600`，首次安装时间 `2026-08-01 21:46:51`。应用冷启动成功，当前停在系统通知权限确认页；阶段 9 完成，进入小米运动健康与手环 8 NFC 真机验收。
- 通知权限已核对为允许；应用主页正常但显示“电脑离线”。本机没有已运行的 Windows 端或本地构建工具，因此从上游 `v0.6.0` Release 下载 Windows 安装包，2,938,619 bytes 与仓库 SHA-256 `D6D36...AD3` 完全一致。
- Windows 安装完成后首次启动暴露上游打包缺陷：`CodexQuota.exe` 依赖但安装器未携带 `libunwind.dll`；上游 v0.6.1 的安装脚本仍未包含该文件。
- 已从 Rust 官方 2026-07-16 稳定版 `rustc 1.97.1 x86_64-pc-windows-gnullvm` 包提取 90,624-byte `libunwind.dll`；82.5MB 源包官方 SHA-256 与发布清单一致，复制后的 DLL SHA-256 为 `13BF4E99B0193634EBDEB0BBDEFF9753B39F1D04799183F664111014AABF4C2C`。
- 安装向导停在成功完成页，等待用户确认点击“完成”并启动新下载的软件；之后将验证托盘进程、配对二维码和手机同步。
## 2026-08-02：GitHub 同步完成

- 已完成中英文 README 更新和提交前终审。
- 已创建并推送 `feat: add Band 8 NFC compatibility and pairing status`，随后以普通合并保留远端已有提交历史。
- 最终合并提交 `2c6927c` 已推送，远端哈希与本地一致，工作树干净。

## 2026-08-02：创建 GitHub Release

- 用户已要求创建 Release，新增阶段 21–24；先核对版本、tag、构建工具、签名材料和可发布资产。
- 已发现项目没有自动 Release 工作流；Windows 安装器、Android 标准 release 和手环 RPK 的构建条件不同，需先做能力审计再发布。
- 一次全工作区递归工具搜索超时并已终止；改用定点路径和命令发现，未把超时误判为构建失败。
- 工具能力审计完成：便携 Cargo/Java/Android SDK/Gradle 可用，NSIS、GitHub CLI 和私有 Wearable AAR 不在已知路径；当前仅有 Android debug APK 输出。
- 已确认 Windows 无根 toolchain 配置，Android release signing/私有 AAR 配置也不存在；发布构建需显式指定便携工具链并限定为可验证的 Band 8-only 资产，除非发现正式签名材料。
- Windows release 首次构建已启动但因找不到 `x86_64-w64-mingw32-clang` 失败；已定位便携 LLVM-MinGW，准备显式设置 PATH 重试。
- Windows release 已用便携 Cargo + LLVM-MinGW 构建成功；下一步整理带 `libunwind.dll` 的可运行便携 ZIP，并核对 Android Band 8 APK。
- 已核对当前 debug APK 元数据为 `com.codex.quota.android` / `0.6.0`；因 Release 构建签名材料缺失，先按 Band 8-only 预发布路线重新验证，不伪装成正式签名 APK。
- Android wrapper 首次验证因尝试下载 Gradle 9.1.0 被沙箱拒绝；工作区已有完整 Gradle runtime，下一次绕过 wrapper 直接运行。
- 直接 Gradle 运行超过 5 分钟无输出，已终止；现有 APK 时间戳未变化，继续使用先前已验证的 Band 8-only debug 产物并在 Release 中明确其调试签名性质。
- Windows portable ZIP、Band 8-only debug APK 和 SHA256SUMS 已整理到工作区 `release-assets/v0.6.0-band8-nfc-preview/`，ZIP 内含运行所需 `libunwind.dll`。
- 已确定 Release 采用 `v0.6.0-band8-nfc-preview` 预发布标签，上传 Windows portable ZIP、Band 8-only debug APK 和 SHA256SUMS，不上传 Band 10 RPK/标准 release APK。
- GitHub Release 已创建并上传三个资产；API 核对 `prerelease=True`、`draft=False`，三个资产均为 `uploaded`。
- 远端 tag 检查完成：没有现存 `v0.6.0*` 标签；检查过程中遇到 escalated 工作目录 ownership 提示，已改用远端 HTTPS URL 查询。
- 提交前复核脚本因 PowerShell 5 不支持 `||` 而未执行；已记录并改为兼容语法重跑。
