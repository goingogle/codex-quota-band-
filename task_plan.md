# 小米手环 8 NFC 通知模式适配计划

## 目标

保留现有 Windows → Android 局域网同步和小米手环 10 Wearable/RPK 路径，为小米手环 8 NFC 增加基于 Android 原生通知、由 Mi Fitness 镜像到手环的兼容模式，使用户能查看配额摘要并收到待授权/等待查看通知，且手机无需访问 OpenAI 或开启代理。

## 边界

- 不修改 `band-app` RPK，不把手环 8 假设为 Vela 应用设备。
- 不新增云端中转、OpenAI 手机端访问、常驻前台服务或高频状态栏通知。
- `Stop` 继续表示“等待查看”，不能称为“已完成”。
- 保留手环 10 的 Xiaomi Wearable SDK 路径。
- 先补测试，再实现；不发布 GitHub Release 二进制，源码同步到用户 fork 的 `main` 分支。

## 阶段

- [complete] 阶段 1：建立干净基线，阅读产品、通知、设置与 UI 实现
- [complete] 阶段 2：定义手环 8 通知策略并补失败测试
- [complete] 阶段 3：实现配额摘要通知、阈值节流与手动推送入口
- [complete] 阶段 4：补充设置/说明文案，保持手环 10 路径兼容
- [complete] 阶段 5：运行相关测试和静态检查，记录真机验收步骤
- [complete] 阶段 6：增加显式的手环 8 NFC 专用构建模式，在不读取私有 Wearable AAR 时使用安全空实现
- [complete] 阶段 7：在工作区配置便携 Java 17 与 Android SDK 命令行工具
- [complete] 阶段 8：运行 Android 单测、Lint 与 Debug 构建，生成可安装 APK
- [complete] 阶段 9：检测用户手机的 ADB 授权状态并在获得确认后安装 APK
- [complete] 阶段 10：修复 Android 扫码入口，重新构建、覆盖安装并完成电脑—手机配对
- [complete] 阶段 11：完成首轮手环 8 NFC 真机验收，记录设置混杂、状态语义、任务同步与通知转发问题
- [complete] 阶段 12：测试优先清理 Band 8-only 设置页，并把首页状态改为通知转发模式的真实语义
- [complete] 阶段 13：定位并修复 Windows → Android 任务状态同步链路
- [complete] 阶段 14：定位 Mi Fitness → 手环通知转发失败原因，调整通知渠道并再次真机验收
- [complete] 阶段 15：审阅 Windows 客户端的配对凭据、在线连接状态与托盘 UI，定义不混淆的显示模型
- [complete] 阶段 16：测试优先实现电脑端“手机配对 / 在线”状态显示
- [complete] 阶段 17：运行 Windows 与根测试，构建并真机复核更新后的电脑客户端
- [complete] 阶段 18：核对 Git 分支、远程仓库与本次同步范围
- [complete] 阶段 19：更新中英文 README，运行提交前完整检查
- [complete] 阶段 20：提交并推送到用户 fork 的 GitHub 仓库，核对远端结果
- [complete] 阶段 21：核对 Release 版本、发布范围、签名材料与现有构建产物
- [complete] 阶段 22：构建或整理可公开发布的资产并生成 SHA-256
- [complete] 阶段 23：创建 Git tag 与 GitHub Release，上传经过核验的资产
- [complete] 阶段 24：核对远端 Release、更新发布文档并记录最终结果
- [complete] 阶段 25：核对手环 10 保留支持并修复 GitHub Release 正文编码
- [in_progress] 阶段 26：提交文档修复、更新远端 Release 并完成最终核验

## 完成标准

- Android 可在用户主动操作时把当前配额作为一条原生通知发布。
- 可选择启用低频自动配额提醒，避免每次 45 秒同步都打扰。
- 原有任务提醒语义、焦点规则和 Wearable Bridge 行为不回归。
- 无可用配额时不猜测数值，并给出准确状态。
- 测试覆盖阈值、重复抑制、无数据和手动推送。
- 清楚列出 Mi Fitness 配置与手环 8 NFC 真机验收边界。
- `-PcodexQuotaBand8Only=true` 构建不需要小米私有 AAR，且不会把手环 10 空实现误装进标准构建。
- 便携工具链只写入工作区，不修改系统级 Java/Android Studio 安装。
- Band 8-only APK 不展示不可用的手环 10 SDK 功能，首页也不把“无法直接读取蓝牙连接”误报成“手环未连接”。
- Android 能显示 Windows 端实际产生的任务状态；若没有任务事件，界面明确区分“暂无任务”与“同步失败”。
- 小米运动健康能将本应用的配额或任务测试通知转发到手环 8 NFC，并完成锁屏/亮屏条件下的真机核对。

## 错误记录

| 错误 | 尝试 | 处理 |
|---|---:|---|
| 当前环境缺少 Java 17、Android SDK 和私有 Wearable AAR，无法运行新增 Android 测试/构建 | 2 | 已再次执行正式 Gradle 验证命令并确认首先阻塞于 `JAVA_HOME`；使用静态检查/根契约测试验证，最终明确 Android 构建与真机待用户环境复验 |
| 首轮实现时先写入了 Android 设置页可见改动，晚于代码定位才复核到 `CONTEXT.md` 的双端 UI 预览门槛 | 1 | 立即撤回正式 Compose/Activity 可见接线；保留底层实现和测试，先提供单方案预览并等待用户确认 |
| 本地图片查看器不能直接处理 SVG，且系统没有 ImageMagick/Inkscape/rsvg-convert | 1 | 保留 SVG 源预览，改用工作区内置浏览器/图形依赖渲染为 PNG 后再做视觉复核 |
| 内置浏览器安全策略禁止访问本地 `file://` SVG | 1 | 不尝试绕过或切换浏览器；关闭临时标签，直接使用 Codex 支持的本地 SVG 图片展示并让用户确认 |
| `session-catchup.py` 无法启动已注册的 Python 3.12 可执行文件 | 1 | 已直接完整读取三份规划文件并核对 `git status`；不重复调用损坏的解释器 |
| 复核通知权限辅助函数时猜错文件名 `NotificationPermission.kt` | 1 | 使用 `rg` 定位实际文件 `NotificationPermissionPolicy.kt`，不再依赖猜测路径 |
| 根目录 `npm test` 因未安装 `ajv`、`qrcode` 依赖而中止，已运行的 26 项通过、5 个测试文件加载失败 | 1 | 仅安装生产依赖后重跑；不把模块加载失败记作代码测试失败 |
| 沙箱内 `npm install --omit=dev` 无权读取用户级 npm 缓存，报 `EPERM` | 1 | 经批准在沙箱外执行同一最小依赖安装，35 个包安装成功 |
| 更新计划阶段时补丁使用了“说明文档”，而文件原文是“说明文案”，上下文未匹配 | 1 | 重新读取目标行并用精确原文更新；未重复猜测 |
| 用 `git diff --no-index` 展示新增文件时按 Git 约定返回退出码 1 | 1 | 已确认输出只是“文件存在差异”而非检查失败；后续使用普通读取/`git diff --check` 验证 |
| 在 Windows 上把 `runtime/*.kt` 通配符直接传给 `rg`，路径解析报错 | 1 | 改为搜索 `runtime` 目录，由 `rg` 自行递归；后续不向 `rg` 传 Windows 未展开的通配路径 |
| 为寻找根测试样式时猜测了不存在的 `version-contract.test.js` 和 `desktop-service-boundary.test.js` | 1 | 已改用 `rg --files test` 列出真实文件名，再按实际文件读取；不再猜测测试路径 |
| 记录上一条错误时补丁把原文“在 Windows 上”误写为“在 Windows 中”，导致上下文不匹配 | 1 | 读取 UTF-8 文件尾部后使用精确原文补丁；不重复提交错误上下文 |
| 手环 8 构建测试最初只匹配 `if (!band8Only)`，未覆盖实际更严格的 AAR 存在性组合条件 | 1 | 将断言收紧为同时检查 `!band8Only && !wearableSdkAar.isFile`，保持测试意图不变 |
| 更新阶段状态时对 `progress.md` 的轻量方案原句记忆不准确，组合补丁上下文未匹配 | 1 | 读取文件尾部并使用精确原文后分步更新；未覆盖任何现有记录 |
| 沙箱内下载官方 Microsoft OpenJDK 17 ZIP 时无法连接远程服务器 | 1 | 按环境规则以受控网络权限重试同一官方 URL，目标仅限工作区工具目录 |
| 首次用便携 Java 启动 Gradle Wrapper 时，沙箱网络阻止下载官方 Gradle 9.1.0 分发包 | 1 | 保持独立工作区 `GRADLE_USER_HOME`，按环境规则以受控网络权限重试同一 Wrapper 命令 |
| 受控网络下 Wrapper 跳转链和 `downloads.gradle.org` 官方直连均被远端重置，Gradle 9.1.0 下载未开始 | 2 | 停止重复同一路径；先检查本机现有 Gradle 缓存或其他已安装的兼容 Gradle，再决定替代下载方式 |
| 本机无可复用 Gradle 9.1.0 缓存，Gradle 官方 GitHub Release 备用地址连续 4 次连接超时 | 1 | 已停止继续重试；保留已校验的 Java 工具链，等待 Android SDK 许可确认后优先测试 Google 下载链路，再处理 Gradle 网络问题 |
| 尝试通过应用内浏览器打开 Gradle 官方 GitHub Release 时被浏览器安全策略拒绝 | 1 | 立即停止该路线，不切换浏览器或绕过策略；仅检查 Gradle 自有域名是否存在独立官方端点，否则交由用户手动下载 |
| 检查 Gradle 最后一个官方分发端点时，Codex 自动审批因工具调用额度上限拒绝联网请求 | 1 | 不绕过额度或安全限制；请用户在普通浏览器手动下载官方 Gradle 9.1.0 ZIP 到指定工作区路径，随后只做本地 SHA-256 校验与构建 |
| 首次真实 Android 构建在沙箱内无法从 Google/Maven/Gradle Plugin Portal 解析 Android Gradle Plugin 9.0.1 | 1 | 代码和本地工具链已正常启动；按环境规则以受控网络权限重跑同一构建，让 Gradle 下载项目声明的依赖到工作区缓存 |
| 受控网络下的首次 Android 构建运行 6 分钟后被命令时限终止，期间未返回 Gradle 错误 | 1 | 先检查依赖缓存、残留进程和局部产物；复用已下载缓存，下一次拆分为配置/单测/Lint/assemble 独立步骤以缩短单次运行并定位耗时 |
| 超时后的后台构建最终在 8 分 25 秒处失败于 `:app:compileDebugKotlin` | 1 | 读取 daemon 日志中的精确 Kotlin 编译器诊断；修复后先单独重跑 compile，再恢复单测、Lint 和 assemble |
| 汇总失败信息的只读命令因查询已经退出的进程而返回退出码 1 | 1 | 已取得关键失败任务；后续把日志、产物和进程检查拆开，避免缺失进程影响其他只读结果 |
| 使用 Windows CIM 读取遗留 Java 进程命令行时被系统拒绝访问 | 1 | 不申请系统管理权限；改用便携 JDK 自带的 `jps -lv` 仅识别 Java 主类和参数 |
| Kotlin daemon 尝试在用户 `AppData\Local\kotlin\daemon` 写临时标记时被沙箱拒绝 | 1 | Kotlin 自动回退到无 daemon 编译并成功；后续显式使用 `-Pkotlin.compiler.execution.strategy=in-process`，保证所有构建写入工作区 |
| PowerShell 直接传递含多个句点的 `-Pkotlin.compiler.execution.strategy=in-process` 时被拆坏，Gradle 将尾部误认成任务名 | 1 | 改用参数数组展开，逐项原样传给 `gradle.bat`；不重复直接拼接该参数 |
| 单测构建进入 Java 编译时无法访问工作区缓存中的 `kotlinx-serialization-core-jvm-1.11.0.jar` | 3 | stacktrace 已确认异常发生于 Gradle 关闭 JDK ZIP 文件系统；工作区 init script 强制 JavaCompile 调用独立 `javac` 后编译、单测、Lint 与打包均成功，本机构建规避未写入正式项目配置 |
| 组合更新规划文件时，`findings.md` 的补丁上下文漏写一个空格而未匹配 | 1 | 补丁为原子操作且没有文件被改动；改为分别使用已读取的精确尾行更新，不重复错误上下文 |
| 查阅通知权限实现时再次把 `NotificationPermissionPolicy.kt` 猜到 `notifications` 目录 | 1 | 已用 `rg --files` 定位真实的 `permissions` 目录；后续只使用搜索结果中的路径，不再凭文件名猜目录 |
| `lintDebug` 发现配额通知发布存在权限撤销竞态，`notify()` 未处理 `SecurityException` | 2 | 回归测试和异常转换已完成；Lint 不做跨高阶函数的数据流分析，仍标记包装 lambda 内的调用。保留真实检查与 catch，仅在该发送函数上添加带理由的 `MissingPermission` 窄范围标注，不创建 baseline、不关闭全局规则 |
| 搜索文档状态时使用了不存在的 `README.en.md` 文件名 | 1 | 已用 `rg --files` 确认实际文件名为 `README_EN.md`；后续按搜索结果引用，不再猜测大小写和下划线 |
| 沙箱内首次启动 ADB 一直等待且未返回设备列表 | 1 | 终止本次启动的精确 ADB 进程；按环境规则以受控权限只读启动 ADB 并读取设备列表，服务正常但当前没有连接设备 |
| 用户已在手机端连接并授权，但 ADB 与 Windows 即插即用设备列表均未出现 Android/MTP 设备 | 1 | APK 和 ADB 服务均正常；问题位于 USB 数据链路。请用户把 USB 用途切换为“文件传输/Android Auto”，无 USB 菜单时更换支持数据的线缆或接口后再检测 |
| ADB 安装 APK 被手机返回 `INSTALL_FAILED_USER_RESTRICTED: Install canceled by user` | 1 | 用户开启“通过 USB 安装”后重试成功；应用已安装为 `0.6.0 / 600` 并冷启动到 Android 通知权限确认页 |
| 查找 Windows 构建入口时先读取了不存在的根 `Cargo.toml` 和 `scripts/build-installer.ps1` | 1 | `rg --files` 已定位真实入口为 `windows-native/Cargo.toml` 与 `windows-native/scripts/build-installer.ps1`，后续仅使用实际路径 |
| GitHub Release API 因共享出口达到匿名限额，无法返回 v0.6.0 资产元数据 | 1 | 改用 `git ls-remote` 确认标签指向当前基线提交，并从 README 明确的上游 Release 资产 URL 下载；以仓库记录的 SHA-256 校验为信任依据 |
| 读取 GitHub Raw 文件时 PowerShell Web 请求异常，随后 curl 因证书吊销服务器离线失败 | 2 | 改用仍验证 TLS 的 `--ssl-revoke-best-effort` 读取上游源码；未使用跳过证书验证选项 |
| 上游 Windows 0.6.0 安装包首次启动报缺少 `libunwind.dll` | 1 | 0.6.1 安装脚本同样只打包 EXE；从 Rust 官方稳定版清单定位匹配的 `windows-gnullvm` 运行库，按官方 SHA-256 校验后仅补齐所需 DLL |
| Rust 官方 82.5MB 包用 curl 下载速度异常并超过命令时限 | 1 | 终止本次启动的精确 curl 进程，改用 Windows BITS 从同一官方 URL 下载；数秒完成并通过官方 SHA-256 校验 |
| 沙箱内无法在 `C:\tmp` 新建 DLL 提取目录 | 1 | 压缩包仍保存在临时目录，改在可写工作区 `work/windows-runtime-extract` 解压；没有修改或覆盖其他临时文件 |
| “扫码连接电脑”按钮调用普通拍照相机，真机只能拍照而不会解析二维码 | 1 | 已确认 `MainActivity.openPairingCamera()` 使用静态图片相机/拍照 Intent；改为真正的二维码扫描与回传流程，并先补失败测试 |
| 首次同时更新三份计划文件时误认 `progress.md` 标题，补丁原子回滚 | 1 | 已用 UTF-8 重新读取三个文件的真实标题，随后按文件分别更新；没有留下部分写入 |
| 为定位便携 Gradle/JDK 而递归枚举整个工具链时，碰到 Gradle 缓存中已消失的临时转换目录并返回错误 | 1 | 已从成功输出取得准确的 Gradle/JDK 路径；后续直接使用已知路径，不再递归扫描缓存目录 |
| 新增 Android 失败测试后，首次 Gradle 单测超过 3 分钟仍无输出 | 1 | 已终止该次受控进程；根契约测试已明确以“扫码 Activity 不存在”失败，满足先红后绿，待实现和新依赖就绪后再运行完整 Android 验证 |
| 首次编译扫码 Activity 时使用了 CameraX 1.6.1 不公开的 `COORDINATE_SYSTEM_ORIGINAL`，且把颜色整数赋给新版 `GradientDrawable.color` | 1 | 改用官方示例公开的 `COORDINATE_SYSTEM_VIEW_REFERENCED`，并通过 `setColor()` 设置背景色后重跑编译 |
| 沙箱内复跑 Android 单测时仍缺少 5 个 CameraX 传递依赖，网络被 `getsockopt` 权限拦截 | 1 | 编译本身已经通过；按环境规则用受控联网权限补齐官方 Google/Maven 依赖后，109 项 Android 单测全部通过 |
| 用 `javap` 检查 CameraX 1.6.1 常量时，成功输出后因本机 ZipFS 限制返回 `AccessDeniedException` | 1 | 已从输出确认常量已迁移到 `ImageAnalysis`；只修改导入以消除弃用警告，不再重复调用该检查 |
| 真机首次授权相机后，`PairingQrScannerActivity` 立即结束并回到设置页 | 1 | 已通过 Activity 栈确认扫码页确实启动过而非按钮失效；先记录 CameraX 初始化异常及安全关闭 scanner，再按真机日志修复根因 |
| 真机日志确认 CameraX 报“未包含默认实现”，缺少 `camera-camera2` 后端 | 1 | 为相机后端补根构建契约断言，确认测试先失败；随后显式加入与其他 CameraX 组件一致的稳定版 `1.6.1` 依赖 |
| 受控联网环境构建的新 APK 使用了主机调试密钥，与手机上沙箱环境构建包的签名不一致，覆盖安装被拒绝 | 1 | 未卸载、未清数据；依赖下载完成后回到原沙箱环境重新打包，证书 SHA-256 恢复为 `c16b...77ae`，覆盖安装成功 |
| Band 8-only UI 与通知渠道的新测试首次运行失败 | 1 | 根契约测试按预期缺少 `BAND8_ONLY` 构建常量，Android 单测按预期缺少新的状态模型函数；已确认红灯来自尚未实现的新行为，进入正式实现 |
| 手机当前无可供 UIAutomator 读取的根窗口，界面文本导出失败 | 1 | 不把它误判为应用崩溃；通知设置已通过 `run-as` 成功读取，后续唤醒手机后再做 UI 验收 |
| 首次诊断 Hook 启动脚本使用了当前 PowerShell/.NET 不支持的 `ProcessStartInfo.ArgumentList` | 1 | 参数没有传入，诊断事件实际未发送；改用无用户输入、固定字符串的 `Arguments` 属性重试，并以 Hook 状态文件验证，不能把脚本自报成功当成结果 |
| WSS 诊断最初按旧假设检查 48733 端口，未发现监听 | 1 | 从手机非敏感连接配置读取真实端点为 `192.168.1.4:17322`，电脑 IP 未变化；改按实际端口检查，不重置配对 |
| 真机 UI 复核脚本误用了 PowerShell 大小写不敏感的保留变量 `$HOME` 变体 `$home` | 1 | 设置页与渠道证据仍成功取得；首页文本未读取。后续改用任务专用变量 `$homeXmlText`，不再使用任何 HOME/CODEX_HOME 变体 |
| 后台任务通知诊断命令取得完整证据后仍返回退出码 1 | 1 | 证据显示没有任务通知、Mi Fitness 监听器仍启用且手机桌面在前台；后续把可能“无匹配”的筛选和状态读取拆开，不让筛选退出码混淆诊断结果 |
| 临时切换“始终提醒”的首个坐标脚本没有改变设置 | 1 | 正则找到了控件，但错误使用 Match Group 的 `ValueAsInt` 计算坐标；已直接读取真实边界 `[1073,2401][1164,2497]`，下一次用显式整数转换点击并以偏好文件复核 |
| 用 ADB 确认桌面前台的命令显示正确结果但返回退出码 1 | 1 | 已确认 `com.miui.home` 是前台；后续显式以成功退出结束只读筛选，避免无匹配/管道状态造成假失败 |
| 首次打开 HyperOS 应用详情后 UIAutomator 返回空根节点 | 1 | 手机显示层当时不可读取；先用标准唤醒键恢复屏幕，再重新打开同一系统页面，不尝试绕过锁屏凭据 |
| 标准唤醒后再次读取 HyperOS 应用详情仍为空根节点 | 2 | 系统页面已收到 Intent，但锁屏/显示层仍不允许自动读取；停止重复尝试，需要用户亲自解锁手机后继续，不绕过锁屏 |
| 首次按截图估算坐标点击“自启动”时误进了“应用联网” | 1 | 对话中的截图按比例缩小，误用了显示尺寸而非手机原始 1440×3200 坐标；联网开关未改变，返回后按原始比例换算自启动约为 `(1250, 2220)` |
| 最终通知测试前 ADB 突然返回 `no devices/emulators found` | 1 | 后台 WSS 仍已独立验证稳定，手机原通知设置仍为 `Unfocused`；停止盲点 UI，不假装已切换。先重新检测 USB/ADB，设备恢复后再执行临时 Always 测试 |
