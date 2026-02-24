## 调试指南

### 前置要求

- **JDK 21**: 必须安装 JDK 21 或更高版本
- **IntelliJ IDEA**: 建议使用 2025.3.3 或更高版本

### 1. 在 IDEA 中打开项目

1. 打开 IntelliJ IDEA
2. `File` > `Open` > 选择 `D:\project\tool_proj\idea-tools`
3. 等待 Gradle 同步完成

### 2. 配置 JDK

1. `File` > `Settings` > `Build, Execution, Deployment` > `Gradle`
2. 设置 `Gradle JVM` 为 JDK 21
3. `File` > `Project Structure` > `SDKs`
4. 确保项目 SDK 设置为 JDK 21

### 3. 运行插件（沙箱模式）

**方式 1: 使用 Gradle 工具窗口（推荐）**
1. 打开右侧 Gradle 工具窗口
2. 展开 `idea-tools` > `Tasks` > `intellij`
3. 双击 `runIde`

**方式 2: 创建运行配置**
1. `Run` > `Edit Configurations...`
2. 点击 `+` > `Gradle`
3. Name: `runIde`
4. Tasks: `runIde`
5. 点击 `OK`
6. 运行该配置

### 4. 调试插件

1. **设置断点**:
   - 在 `GenerateSelectAction.kt` 第 42 行（对话框显示前）
   - 或 `SelectGeneratorDialog.kt` 中

2. **调试运行**:
   - 在 Gradle 工具窗口中
   - 右键 `runIde` > `Debug 'runIde'`

3. **触发断点**:
   - 沙箱 IDEA 启动后
   - 打开 Database 工具窗口
   - 连接数据库
   - 右键点击表 > `Generate SELECT Statement`

### 5. 配置数据库（在沙箱中）

1. 沙箱 IDEA 启动后，打开 `Database` 工具窗口
2. 点击 `+` > `Data Source` > 选择数据库类型
3. 输入连接信息并测试连接
4. 展开数据库 > 表

### 6. 构建发布包

```bash
./gradlew buildPlugin
```

构建产物位置：
```
D:\project\tool_proj\idea-tools\build\distributions\sql-generator-1.0.0.zip
```

安装插件：
- `Settings` > `Plugins` > ⚙️ > `Install Plugin from Disk...`
- 选择上面的 zip 文件

### 常见问题

**Q: 找不到 Java 命令**
- 安装 JDK 21: https://adoptium.net/
- 设置环境变量 `JAVA_HOME` 指向 JDK 安装目录

**Q: Gradle 同步失败**
- 检查网络连接（需要下载依赖）
- 尝试使用代理或镜像

**Q: 找不到 Database 插件**
- 沙箱 IDEA 会自动安装配置的插件
- 首次运行可能需要等待下载

**Q: 右键菜单不显示**
- 确保选中了数据库表（不是文件夹）
- 检查是否连接了数据库
