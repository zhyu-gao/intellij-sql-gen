# Changelog

所有项目的显著变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
- 计划支持 INSERT 语句生成
- 计划支持 UPDATE 语句生成
- 计划支持 WHERE 条件生成

## [1.0.0] - 2026-02-24

### Added
- ✨ 初始版本发布
- 🖱️ 支持右键菜单生成 SELECT 语句
- ☑️ 字段选择功能（复选框界面）
- 🏷️ 表别名支持
- 📋 一键复制 SQL 到剪贴板
- 👁️ 实时 SQL 预览
- 🎨 横向排列的字段选择界面（WrapLayout）
- 🌐 支持 IntelliJ IDEA 2025.3+

### Fixed
- 修复了在新版 IntelliJ API 中的兼容性问题
- 修复了 Database 工具窗口中获取选中表的问题

## 版本说明

### 版本号格式
- `MAJOR.MINOR.PATCH`
- MAJOR: 重大功能变更或破坏性更新
- MINOR: 新功能添加（向后兼容）
- PATCH: Bug 修复或小改进

### 标签说明
- `Added` - 新添加的功能
- `Changed` - 对现有功能的变更
- `Deprecated` - 即将移除的功能
- `Removed` - 已移除的功能
- `Fixed` - Bug 修复
- `Security` - 安全相关修复
