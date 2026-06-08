<p align="center">
  <img src="docs/hero.svg" width="420" alt="SeekFlow"/>
</p>

<p align="center">
  <strong>DeepSeek API 余额与用量监控</strong>
</p>

<p align="center">
  实时追踪余额 · 多模型用量分析 · 桌面小组件 · 智能预警
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0+-3DDC84?style=flat-square&logo=android" alt="Platform"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose" alt="Compose"/>
  <img src="https://img.shields.io/badge/License-MIT-00B8D9?style=flat-square" alt="License"/>
  <img src="https://img.shields.io/badge/Version-2.1.0-blue?style=flat-square" alt="Version"/>
</p>

---

## 功能

| 功能 | 说明 |
|:---|:---|
| **余额监控** | 实时查询 DeepSeek 账户余额，赠金与充值余额分项展示 |
| **消耗统计** | 当日 / 本月消耗一目了然，支持 V4 Flash 和 V4 Pro 模型独立统计 |
| **趋势图表** | 近 7 天每日 Token 消耗柱状图，渐变色可视化 |
| **桌面小组件** | 小 / 中 / 大三种尺寸，15 分钟自动刷新，无需打开应用 |
| **余额预警** | 设置阈值，余额不足时系统通知推送 |
| **后台刷新** | WorkManager 驱动，主数据 6 小时 / 小组件 15 分钟定时更新 |
| **双凭证模式** | API Key 查余额，User Token 查用量明细，灵活搭配 |
| **Material 3** | 支持 Android 12+ 动态取色，深色 / 浅色主题自适应 |

## 架构

```
MVVM + Clean Architecture

┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  Compose Screens ← ViewModel ← StateFlow    │
├─────────────────────────────────────────────┤
│               Domain Layer                   │
│  UsageRepository (数据聚合 + 业务逻辑)        │
├─────────────────────────────────────────────┤
│                Data Layer                    │
│  DeepSeekApi · PlatformApi · Room · DataStore│
└─────────────────────────────────────────────┘
```

## 项目结构

```
app/src/main/java/com/deepseek/balance/
├── MainActivity.kt                  # 入口
├── DeepSeekApp.kt                   # Application (Hilt + WorkManager)
├── data/
│   ├── api/                         # Retrofit 接口 & 数据模型
│   │   ├── DeepSeekApi.kt           #   官方 API (余额)
│   │   └── PlatformApi.kt           #   平台 API (用量明细)
│   ├── db/                          # Room 数据库 & DAO
│   ├── repository/                  # 数据仓库
│   └── worker/                      # 后台刷新 Worker
├── di/                              # Hilt 依赖注入
├── ui/
│   ├── screens/                     # 页面 (Dashboard / Settings / Splash)
│   ├── components/                  # 组件 (BalanceCard / BarChart / UsageCard)
│   ├── theme/                       # Material 3 主题
│   └── widget/                      # 桌面小组件 (3 种尺寸)
└── util/                            # 通知工具
```

## 技术栈

| 层级 | 技术 |
|:---|:---|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Hilt |
| 网络 | Retrofit + OkHttp |
| 存储 | Room (数据库) + DataStore (偏好) |
| 后台 | WorkManager |
| 小组件 | AppWidget (RemoteViews) |
| 语言 | Kotlin 2.0 / JDK 17 |

## 快速开始

### 环境要求

- Android Studio Ladybug+
- JDK 17
- Android SDK 35 (minSdk 26)

### 构建

```bash
git clone https://github.com/DavidBlon/SeekFlow.git
cd SeekFlow
./gradlew assembleDebug
```

### 配置

首次启动后进入设置页面，填入：

| 凭证 | 获取方式 | 用途 |
|:---|:---|:---|
| **API Key** | [platform.deepseek.com/api_keys](https://platform.deepseek.com/api_keys) | 查询余额 |
| **User Token** | 浏览器 Console 执行 `localStorage.getItem('userToken')` | 查询用量明细 |

> 仅填 API Key 也可使用余额查询；填入 User Token 后可解锁按模型、按天的精确用量统计。

## 数据来源

| 数据 | 接口 |
|:---|:---|
| 账户余额 | `GET /user/balance` (DeepSeek API) |
| 用户概览 | `GET /api/v0/users/get_user_summary` (Platform API) |
| 用量统计 | `GET /api/v0/usage/amount` + `GET /api/v0/usage/cost` |
| 本地缓存 | Room 数据库 (按日 / 按模型聚合) |

## License

```
MIT License

Copyright (c) 2025 DavidBlon
```
