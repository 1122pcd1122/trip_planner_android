# Trip Planner - Android Client

一个智能旅行计划 Android 客户端应用，基于 MVVM 架构和 Jetpack Compose 构建。

## ✨ 核心功能

### 行程规划
- 🤖 **AI 智能行程生成** - 输入目的地和天数，自动生成完整旅行计划
- 📅 **日程安排** - 按天展示景点、酒店、餐饮安排
- 🗺️ **POI 详情** - 景点、酒店、餐厅详细信息查看

### 实用工具
- 🌤️ **天气查询** - 目的地天气预报与出行建议
- 💰 **预算管理** - 旅行费用记录与统计
- 📝 **行程笔记** - Markdown 格式笔记记录
- 🎒 **行李清单** - 智能打包清单管理
- 🔄 **行程编辑** - 手动调整行程顺序与内容

### 用户功能
- 👤 **用户认证** - 注册/登录/JWT Token 认证
- ☁️ **云端同步** - 本地与云端行程数据同步
- 💾 **三级缓存** - 内存/磁盘/网络分层缓存，离线可用

## 🏗️ 技术架构

### 架构模式
```
UI Layer (Jetpack Compose)
    ↓
ViewModel Layer (StateFlow)
    ↓
Data Layer (Repository)
    ↓
Network + Database
```

### 技术栈
- **UI 框架**: Jetpack Compose + Material3
- **架构**: MVVM + Repository 模式
- **状态管理**: StateFlow + collectAsState
- **网络请求**: Retrofit + OkHttp + Gson
- **本地存储**: Room (SQLite) + DataStore
- **图片加载**: Coil
- **异步编程**: Kotlin Coroutines
- **序列化**: kotlinx-serialization-json

## 📦 核心模块

| 模块 | 说明 |
|------|------|
| `viewModel` | MainViewModel 统一状态管理 |
| `ui/screens` | Compose 页面组件 |
| `ui/components` | 可复用 UI 组件 |
| `data/repository` | 数据仓库层（网络/缓存） |
| `data/database` | Room 数据库定义 |
| `data/model` | 数据模型定义 |
| `utils` | 工具类（网络、主题、偏好） |

## 🛠️ 构建要求

- **Min SDK**: 29 (Android 10)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.x
- **Java**: 11
- **Compose Compiler**: 1.5.4

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/1122pcd1122/trip_planner_android.git
cd trip_planner_android
```

### 2. 配置后端地址
修改 `utils/NetworkUtils.kt` 中的后端 API 地址：
```kotlin
const val BASE_URL = "http://你的后端IP:5000/"
```

### 3. 构建运行
```bash
# 使用 Android Studio 打开项目
# 或使用命令行构建
./gradlew assembleDebug
```

## 📱 界面截图

- 深色/浅色主题自动适配
- 骨架屏加载动画
- 流畅的交互动画

## 🔄 后端依赖

本项目需要配套的后端服务：
- **仓库地址**: https://github.com/1122pcd1122/trip_planer
- **启动方式**: `python main_api.py`
- **默认端口**: 5000

## 📄 License

MIT License
