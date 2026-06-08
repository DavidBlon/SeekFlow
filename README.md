# SeekFlow

> DeepSeek API 余额与用量监控 Android 应用

SeekFlow 是一款 Android 应用，用于实时监控 DeepSeek 平台的 API 余额和 Token 消耗情况。支持桌面小组件、余额预警通知、多模型用量统计等功能。

## 功能特性

- **余额实时监控** — 调用 DeepSeek API 查询账户余额（赠金/充值余额分项展示）
- **用量统计** — 按日/月统计 Token 消耗，区分 V4 Flash 与 V4 Pro 模型
- **可视化图表** — 近 30 天每日 Token 消耗柱状图
- **桌面小组件** — 提供小/中/大三种尺寸的 Widget，支持自动刷新
- **余额预警** — 设置余额阈值，低于阈值时推送通知提醒
- **后台自动刷新** — 通过 WorkManager 每 6 小时自动拉取最新数据
- **Material 3 设计** — 支持动态取色（Android 12+）及深色/浅色主题

## 技术栈

| 层级 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Hilt 依赖注入 |
| 网络 | Retrofit + OkHttp |
| 本地存储 | Room (数据库) + DataStore (偏好) |
| 后台任务 | WorkManager |
| 桌面组件 | AppWidget (Glance) |
| 语言 | Kotlin 2.0 / JDK 17 |

## 项目结构

```
app/src/main/java/com/deepseek/balance/
├── MainActivity.kt                 # 入口 Activity
├── DeepSeekApp.kt                  # Application 类
├── data/
│   ├── api/                        # Retrofit API 接口及数据模型
│   ├── db/                         # Room 数据库、DAO、Entity
│   ├── repository/                 # 数据仓库层
│   └── worker/                     # WorkManager 后台任务
├── di/                             # Hilt 依赖注入模块
├── ui/
│   ├── components/                 # 可复用 Compose 组件
│   ├── screens/                    # 页面（Dashboard、Settings、Splash）
│   ├── theme/                      # Material 3 主题
│   └── widget/                     # 桌面小组件
└── util/                           # 工具类（通知等）
```

## 快速开始

### 前置要求

- Android Studio Ladybug 或更高版本
- JDK 17
- Android SDK 35

### 构建运行

1. 克隆仓库
   ```bash
   git clone https://github.com/DavidBlon/SeekFlow.git
   ```

2. 使用 Android Studio 打开项目

3. 同步 Gradle 并运行到设备/模拟器

4. 首次启动后，进入设置页面输入你的 DeepSeek API Key（从 [platform.deepseek.com](https://platform.deepseek.com) 获取）

## 数据来源

| 数据项 | 来源 |
|--------|------|
| 账户余额 | DeepSeek API `/user/balance` |
| 今日消耗 | 本地 Room 数据库（当日记录之和） |
| 本月消耗 | 本地 Room 数据库（当月记录之和） |
| 模型 Token | 本地 Room 数据库（按模型过滤） |
| 柱状图 | 本地 Room 数据库（近 30 天按日聚合） |

## License

MIT
