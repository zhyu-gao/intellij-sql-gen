# SQL Select Generator

一个简单高效的 IntelliJ IDEA 插件，用于从数据库表生成 SELECT SQL 语句。

## ✨ 功能特性

- 🖱️ **右键生成** - 在 Database 工具窗口中右键点击任意表即可生成 SELECT 语句
- ☑️ **字段选择** - 通过友好的复选框界面选择/取消特定字段
- 🏷️ **表别名** - 支持可选的表别名设置
- 📋 **一键复制** - 一键将生成的 SQL 复制到剪贴板
- 👁️ **实时预览** - 实时显示生成的 SQL 语句

## 📖 使用方法

1. 在 IntelliJ IDEA 中打开 Database 工具窗口
2. 连接到你的数据库
3. 右键点击任意数据表
4. 选择 "Generate SELECT Statement"
5. 在弹出的对话框中：
   - 勾选需要的字段（默认全选）
   - 可选：输入表别名
   - 查看实时 SQL 预览
6. 点击 "复制" 按钮或 "确定" 插入到编辑器

## 🔧 系统要求

- IntelliJ IDEA 2025.3 或更高版本
- 支持 Ultimate 和 Community 版本
- 需要安装 Database 插件（通常已内置）

## 📦 安装方式

### 从 JetBrains Marketplace 安装

1. 打开 IntelliJ IDEA
2. 进入 `Settings` → `Plugins` → `Marketplace`
3. 搜索 "SQL Select Generator"
4. 点击 Install 安装

### 从磁盘安装

1. 下载最新的 [releases](https://github.com/zhyu-gao/intellij-sql-gen/releases)
2. 进入 `Settings` → `Plugins` → ⚙️ → `Install plugin from disk`
3. 选择下载的 ZIP 文件

## 🛠️ 开发构建

```bash
# 克隆项目
git clone https://github.com/zhyu-gao/intellij-sql-gen.git
cd intellij-sql-gen

# 构建插件
./gradlew buildPlugin

# 运行测试 IDE
./gradlew runIde
```

## 📝 更新日志

参见 [CHANGELOG.md](./CHANGELOG.md)

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](./LICENSE) 文件

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 👤 作者

- GitHub: [@zhyu-gao](https://github.com/zhyu-gao)
- 邮箱: your-email@example.com
