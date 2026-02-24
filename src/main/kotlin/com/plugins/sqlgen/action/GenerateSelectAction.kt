package com.plugins.sqlgen.action

import com.intellij.database.model.DasColumn
import com.intellij.database.psi.DbElement
import com.intellij.database.psi.DbTable
import com.intellij.database.util.DasUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.plugins.sqlgen.model.ColumnInfo
import com.plugins.sqlgen.ui.SelectGeneratorDialog

/**
 * 生成 SELECT 语句的右键菜单动作
 */
class GenerateSelectAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // 获取选中的数据库表
        val dbTable = getSelectedTable(e) ?: return

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                // 从数据库获取表结构信息
                val columns = fetchTableColumns(dbTable)

                ApplicationManager.getApplication().invokeLater {
                    // 显示对话框
                    val dialog = SelectGeneratorDialog(dbTable.name, columns)
                    if (dialog.showAndGet()) {
                        // 生成 SELECT 语句
                        val selectSql = dialog.getGeneratedSql()

                        // 将 SQL 插入到编辑器
                        insertSqlToEditor(project, selectSql)
                    }
                }
            } catch (ex: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "获取表结构失败：${ex.message}",
                        "错误"
                    )
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val table = getSelectedTable(e)
        e.presentation.isEnabledAndVisible = table != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    /**
     * 获取选中的数据库表
     */
    private fun getSelectedTable(e: AnActionEvent): DbTable? {
        // 尝试从 PSI_ELEMENT 获取
        e.getData(com.intellij.openapi.actionSystem.LangDataKeys.PSI_ELEMENT)?.let { psiElement ->
            if (psiElement is DbTable) {
                return psiElement
            }
        }
        
        // 尝试从 PSI_ELEMENT_ARRAY 获取
        e.getData(com.intellij.openapi.actionSystem.LangDataKeys.PSI_ELEMENT_ARRAY)?.let { psiElements ->
            for (element in psiElements) {
                if (element is DbTable) {
                    return element
                }
            }
        }

        // 备用：从 PSI_FILE 获取（当在编辑器中右键时）
        e.getData(CommonDataKeys.PSI_FILE)?.let { psiFile ->
            if (psiFile is DbTable) {
                return psiFile
            }
        }

        return null
    }

    /**
     * 从数据库表获取字段列表
     */
    private fun fetchTableColumns(table: DbTable): List<ColumnInfo> {
        return try {
            // 直接从 DbTable 获取列信息，现代 API 推荐做法
            // 这样能获取到更准确的类型信息，因为它会尝试从 PSI 层面解析
            val columns = DasUtil.getColumns(table)
            if (columns.isEmpty()) {
                return fetchColumnsFromPsi(table)
            }
            columns.map { column -> createColumnInfo(column) }.toList()
        } catch (e: Exception) {
            fetchColumnsFromPsi(table)
        }
    }

    /**
     * 从 Psi 元素获取列信息
     */
    private fun fetchColumnsFromPsi(table: DbTable): List<ColumnInfo> {
        return try {
            val children = table.children
            children.filterIsInstance<DbElement>()
                .mapNotNull { element ->
                    element.name?.let { name ->
                        var typeName = "unknown"
                        try {
                            val presentableText = element.presentation?.presentableText
                            val locationString = element.presentation?.locationString
                            
                            typeName = "p=${presentableText}, l=${locationString}"
                            
                            if (element is DasColumn) {
                                @Suppress("DEPRECATION")
                                val dt = element.dataType
                                typeName += " | DasCol: dt=${dt?.typeName}, spec=${dt?.specification}, das=${try { element.javaClass.getMethod("getDasType").invoke(element)?.toString() } catch(e: Exception) { "no" }}"
                            }
                        } catch (e: Exception) {
                            typeName = "err"
                        }
                        
                        ColumnInfo(
                            name = name,
                            type = typeName,
                            selected = true
                        )
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 从 DasColumn 创建 ColumnInfo
     */
    private fun createColumnInfo(column: DasColumn): ColumnInfo {
        var typeName = ""
        
        try {
            // 尝试从 DbElement 的 presentation (UI展示节点) 获取
            if (column is DbElement) {
                // UI 上展示的 locationString 通常是像 "varchar(255)" 这样的灰字
                val locStr = column.presentation?.locationString
                if (!locStr.isNullOrBlank()) {
                    typeName = locStr.trim()
                }
                
                // 如果 locationString 是空的，我们再看看 presentableText 是否带了冒号，比如 "id: bigint"
                if (typeName.isEmpty()) {
                    val pText = column.presentation?.presentableText
                    if (pText != null && pText.contains(":")) {
                        typeName = pText.substringAfter(":").trim()
                    }
                }
            }

            // 如果从 UI 上还是没获取到，尝试 fallback 获取内部的真实数据类型
            if (typeName.isEmpty() || typeName == "unknown") {
                @Suppress("DEPRECATION")
                val dt = column.dataType
                val typeStr = dt?.specification ?: dt?.typeName ?: ""
                if (typeStr.isNotBlank() && typeStr != "unknown") {
                    typeName = typeStr
                }
            }
            
            // 如果上述都没有，尝试使用 DasUtil.getDataType 甚至反射
            if (typeName.isEmpty() || typeName == "unknown") {
                try {
                    val getDataTypeMethod = DasUtil::class.java.methods.find { it.name == "getDataType" }
                    if (getDataTypeMethod != null) {
                        val dt = getDataTypeMethod.invoke(null, column)
                        if (dt != null) {
                            val typeStr = dt.javaClass.getMethod("getTypeName").invoke(dt)?.toString() ?: ""
                            if (typeStr.isNotBlank() && typeStr != "unknown") {
                                typeName = typeStr
                            }
                        }
                    }
                } catch(e: Exception) { }
            }
            
        } catch (e: Exception) {
            // ignore
        }

        if (typeName.isEmpty()) {
            typeName = "unknown"
        }

        return ColumnInfo(
            name = column.name,
            type = typeName,
            nullable = !column.isNotNull,
            defaultValue = column.default,
            comment = column.comment,
            selected = true
        )
    }

    /**
     * 将 SQL 插入到编辑器
     */
    private fun insertSqlToEditor(project: Project, sql: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                // 插入到当前编辑器光标位置
                val offset = editor.caretModel.offset
                editor.document.insertString(offset, sql)
            } else {
                // 显示弹窗
                Messages.showInfoMessage(
                    project,
                    "生成的 SQL:\n\n$sql",
                    "SELECT 语句"
                )
            }
        }
    }
}
