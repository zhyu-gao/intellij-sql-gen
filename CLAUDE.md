# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an IntelliJ IDEA plugin written in Kotlin that generates SELECT SQL statements from database tables. It integrates with the IDE's Database tool window and provides a UI dialog for selecting columns.

The plugin also includes a secondary feature: "Copy File Path with Line Number" which copies the current file path with cursor line number to clipboard.

## Common Commands

```bash
# Build the plugin (produces zip in build/distributions/)
./gradlew buildPlugin

# Run plugin in sandbox IDE for testing
./gradlew runIde

# Debug the plugin
./gradlew runIde --debug

# Clean build
./gradlew clean
```

## Architecture

The plugin follows standard IntelliJ Platform architecture:

- **Actions** (`src/main/kotlin/com/plugins/sqlgen/action/`)
  - `GenerateSelectAction.kt`: Main action triggered from Database tool window context menu. Reads table schema via IntelliJ's Database PSI API (`DasUtil.getColumns()`) and shows the generation dialog.
  - `CopyFilePathWithLineAction.kt`: Secondary action for copying file paths with line numbers.

- **UI** (`src/main/kotlin/com/plugins/sqlgen/ui/`)
  - `SelectGeneratorDialog.kt`: Dialog with checkboxes for column selection, alias input, and real-time SQL preview. Includes a custom `WrapLayout` for flow-style checkbox arrangement.

- **Models** (`src/main/kotlin/com/plugins/sqlgen/model/`)
  - `ColumnInfo.kt`: Data class holding column metadata (name, type, nullable, default, comment).
  - `TableInfo.kt`: Data class holding table metadata.

- **Utilities** (`src/main/kotlin/com/plugins/sqlgen/util/`)
  - `SelectGenerator.kt`: Object generating formatted SELECT SQL with smart line wrapping.

## Plugin Configuration

- **plugin.xml**: Declares actions and their group placements (DatabaseViewPopupMenu, EditorPopupMenu).
- **database-support.xml**: Optional dependency configuration for the Database plugin.
- **build.gradle.kts**:
  - 编译基线: IntelliJ IDEA 2025.3（build 253，即 since-build 的最低版本）
  - 兼容性验证目标: 2026.3（IU-263.7307.200）与 2025.3，`./gradlew verifyPlugin`
  - Bundled plugin: `com.intellij.database`
  - Since build: 253 (2025.3), until build: 263.*
  - Kotlin 2.4.21 / Gradle 8.14.4（2026.3 平台 Kotlin 库 metadata 为 2.4.0，KGP 必须 ≥ 2.4.x）
  - 编译基线不要改成 2026.3：263 的 `FileEditorManager` 已 Kotlin 化，用 263 编译会被绑定到
    `FileEditorManager.Companion`，在 253/261 上抛 `NoSuchFieldError`；且 263 平台字节码为 Java 25（class 69.0），
    JDK 21 的 javac 读不了，Java 源码也无法参与编译
  - Supports both Ultimate and Community editions; WebStorm support via optional database dependency

## Key Patterns

- Use `ApplicationManager.getApplication().executeOnPooledThread` for background database operations
- Use `ApplicationManager.getApplication().invokeLater` for UI updates from background threads
- Actions implement `DumbAware` for compatibility with indexing
- Action update thread: `ActionUpdateThread.BGT` for database operations

## Development Notes

- Requires JDK 21
- The Database plugin dependency is optional to support WebStorm (which doesn't bundle the Database plugin by default)
- The `GenerateSelectAction` has fallback logic to fetch column info from PSI when `DasUtil.getColumns()` returns empty