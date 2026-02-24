package com.plugins.sqlgen.util

/**
 * SELECT 语句生成工具
 */
object SelectGenerator {

    /**
     * 生成 SELECT 语句
     * @param tableName 表名
     * @param columns 选中的字段列表
     * @param alias 表别名（可选）
     * @return 生成的 SELECT 语句
     */
    fun generateSelect(tableName: String, columns: List<String>, alias: String? = null): String {
        if (columns.isEmpty()) {
            return "-- 错误：没有选择任何字段"
        }

        return buildString {
            append("SELECT ")

            val indent = "       "
            var currentLineLength = 7 // "SELECT " 的长度
            val maxLineLength = 100 // 触发换行的行宽阈值

            for ((index, column) in columns.withIndex()) {
                val columnStr = if (alias != null && alias.isNotBlank()) "$alias.$column" else column
                val isLast = index == columns.size - 1
                val textToAppend = if (isLast) columnStr else "$columnStr, "
                
                // 如果当前行加上这个字段超过了最大长度（并且这不是这行的第一个字段），则换行
                if (currentLineLength + textToAppend.length > maxLineLength && currentLineLength > indent.length) {
                    append("\n")
                    append(indent)
                    currentLineLength = indent.length
                }
                
                append(textToAppend)
                currentLineLength += textToAppend.length
            }

            append("\nFROM ")
            append(tableName)

            // 添加别名
            if (alias != null && alias.isNotBlank()) {
                append(" $alias")
            }

            append(";")
        }
    }

    /**
     * 生成带注释的 SELECT 语句
     * @param tableName 表名
     * @param columns 选中的字段列表
     * @param alias 表别名（可选）
     * @return 生成的 SELECT 语句（带注释）
     */
    fun generateSelectWithComment(tableName: String, columns: List<String>, alias: String? = null): String {
        return buildString {
            append("-- 表：$tableName\n")
            append("-- 生成时间：${java.time.LocalDateTime.now()}\n\n")
            append(generateSelect(tableName, columns, alias))
        }
    }
}
