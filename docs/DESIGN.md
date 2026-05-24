# PRD 完成后的下一步工作清单

| Version | Date | Author | Description |
| :--- | :--- | :--- | :--- |
| 1.0 | 2026‑05‑23 | — | Initial design outline based on PRD v1.0 |

> 针对《Android 手机健康监测应用》PRD v1.0（仅计步 + Health Connect，无穿戴设备）

---

## 总体开发流程概览

完成 PRD 之后，标准流程分为 **7 个阶段**：

1. 技术方案详细设计  
2. 创建 Android 工程与基础框架  
3. 分迭代实现核心模块（4 个迭代）  
4. 测试与质量保障  
5. 准备发布材料  
6. 灰度发布与监控（可选）  
7. 正式发布  

建议总周期约 **4 周**（取决于迭代速度）。

---

## 第 1 步：技术方案详细设计（1–2 天）

将 PRD 的技术选型落地为可执行的设计文档。

### 1.1 架构设计
- 分层结构：UI (`Compose`) → ViewModel → Repository → DataSource
- 数据源：
  - `SensorDataSource` 封装 `SensorManager`
  - `LocalDataSource` 封装 Room
  - `HealthConnectDataSource` 封装 Health Connect SDK
- `StepRepository` 聚合多源，处理优先级与同步逻辑

### 1.2 数据库设计
- 实体 `DailyStepEntity`：
  - `date` (String, yyyy-MM-dd)
  - `steps` (Int)
  - `distance` (Float, 米)
  - `calories` (Float, 千卡)
  - `isManuallyEdited` (Boolean)
- 用户指标表 `UserProfile`：身高(cm)、体重(kg)、每日步数目标

### 1.3 前台服务设计
- `StepCounterService` 继承 `Service`
- 绑定 `SensorEventListener`
- 通知渠道 ID: `step_channel`，标题“步数记录中”
- `onStartCommand` 返回 `START_STICKY`

### 1.4 Health Connect 集成方案
- 初始化时机：应用启动时检查可用性
- 权限请求：按需请求 `READ_STEPS` / `WRITE_STEPS`
- 冲突策略：本地传感器数据优先，或取时间戳较新的记录（可配置）

### 输出物
- 技术设计文档（含架构图、流程图）

---

## 第 2 步：创建 Android 工程与基础框架（1 天）

- 使用 Android Studio 新建项目：Kotlin + Jetpack Compose
- 最低 SDK 26，目标 SDK 34/35
- 添加核心依赖：

````groovy
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.health.connect:connect-client:1.2.0-alpha04")
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")