## [0.0.6] - 2026-11-06

### 🛠️ Stability
- 修复首次兼容性验证暴露的问题：可选依赖配置文件声明补全为 `database-support.xml`，消除插件结构缺陷
- 编译基线收敛到最低支持版本 2025.3，修复 `FileEditorManager` 在 2026.3 中 Kotlin 化导致的 `NoSuchFieldError` 隐患
- 通过 `verifyPlugin` 双版本校验：2025.3（253）与 2026.3（263）均为 Compatible

---

## [0.0.5] - 2026-11-06

### ✨ Features
- 升级兼容性，支持 IntelliJ IDEA 2026.3 版本（until-build 提升至 263.*）

### 🛠️ Stability
- 字段类型解析改用 `DasType`（`specification` / `description`），替换 2026.3 中已标记待移除的 `DasTypedObject.getDataType()`，并移除原有反射兜底逻辑
- 工具链升级：Kotlin 2.4.21、Gradle 8.14.4

---

## [0.0.4] - 2026-04-21

### ✨ Features
- 升级兼容性，支持 IntelliJ IDEA 2026.1 版本

### 🎨 UI/UX Improvements
- 全新设计的 SQL 生成对话框，提供更现代化的布局与交互体验
- 优化字段选择列表的视觉层次，增加详细的元数据工具提示
- 改进实时预览逻辑，表别名变更现在能即时反映在预览框中

### 🛠️ Stability
- 针对新版 IntelliJ 平台 API 进行了适配优化

---

## [0.0.3] - 2025-02-23

### ✨ Features
- 添加 WebStorm 支持，将 database 插件依赖设为可选

---

## [0.0.2] - 2025-02-23

### ✨ Features
- 新增右键菜单：一键复制带行号的文件路径
- 优化生成结果：改进系统默认字段的换行格式

---

## [0.0.1] - 2025-02-23

### 🚀 Initial Release
- 提供从数据库表一键生成 SELECT 语句的基础功能
- 支持可视化勾选列与自定义别名
