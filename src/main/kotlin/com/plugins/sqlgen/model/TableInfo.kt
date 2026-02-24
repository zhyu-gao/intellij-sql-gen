package com.plugins.sqlgen.model

/**
 * 数据库表字段信息
 */
data class ColumnInfo(
    val name: String,
    val type: String = "",
    val nullable: Boolean = true,
    val defaultValue: String? = null,
    val comment: String? = null,
    var selected: Boolean = true
)

/**
 * 数据库表信息
 */
data class TableInfo(
    val name: String,
    val schema: String = "",
    val columns: List<ColumnInfo>
)
