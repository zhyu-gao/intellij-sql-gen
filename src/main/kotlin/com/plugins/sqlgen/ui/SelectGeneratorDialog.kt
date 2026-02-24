package com.plugins.sqlgen.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.plugins.sqlgen.model.ColumnInfo
import com.plugins.sqlgen.util.SelectGenerator
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Insets
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.Toolkit

/**
 * 生成 SELECT 语句的对话框
 */
class SelectGeneratorDialog(private val tableName: String, private val columns: List<ColumnInfo>) : DialogWrapper(true) {

    private val columnCheckboxes: MutableList<JBCheckBox> = mutableListOf()
    private val aliasTextField: JBTextField = JBTextField()
    private val selectAllCheckBox: JBCheckBox = JBCheckBox("全选/全不选")
    private val previewTextArea: JTextArea = JTextArea(5, 40)
    private val generateButton: JButton = JButton("生成 SQL")

    init {
        init()
        title = "生成 SELECT 语句 - $tableName"
    }

    override fun createCenterPanel(): JComponent {
        previewTextArea.lineWrap = true
        previewTextArea.wrapStyleWord = true
        previewTextArea.isEditable = false
        previewTextArea.font = UIUtil.getFontWithFallback("Monospaced", java.awt.Font.PLAIN, 12)

        // 表信息面板
        val infoPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(0, 0, 10, 0)
            add(JBLabel("表名：$tableName"), BorderLayout.WEST)
        }

        // 别名输入面板
        val aliasPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(0, 0, 10, 0)
            val label = JBLabel("表别名 (可选):")
            label.preferredSize = Dimension(100, label.preferredSize.height)
            add(label, BorderLayout.WEST)
            aliasTextField.preferredSize = Dimension(150, aliasTextField.preferredSize.height)
            add(aliasTextField, BorderLayout.CENTER)
            aliasTextField.emptyText.text = "留空表示不使用别名"
        }

        // 字段选择面板 - 使用 WrapLayout 实现横向排列，自动换行
        val columnsWrapperPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = EmptyBorder(10, 0, 10, 0)

            // 全选/全不选
            add(selectAllCheckBox)
            add(Box.createVerticalStrut(5))
            add(JSeparator().apply { maximumSize = Dimension(Int.MAX_VALUE, 2) })
            add(Box.createVerticalStrut(5))

            // 字段复选框容器 - 使用 WrapLayout 实现横向排列，自动换行
            val columnsPanel = JPanel(WrapLayout(FlowLayout.LEFT, 5, 5)).apply {
                // 创建字段复选框列表
                columns.forEach { column ->
                    val checkBox = JBCheckBox(column.name).apply {
                        isSelected = column.selected
                        toolTipText = buildString {
                            append("可空：${if (column.nullable) "是" else "否"}")
                            column.defaultValue?.let { append(" | 默认值：$it") }
                            column.comment?.let { append(" | 注释：$it") }
                        }
                        // 添加选中变化监听，实时更新预览
                        addActionListener { updatePreview() }
                    }
                    columnCheckboxes.add(checkBox)
                    add(checkBox)
                }
            }

            // 将字段面板包装在滚动面板中
            add(JScrollPane(columnsPanel).apply {
                border = null
                preferredSize = Dimension(400, 150)
                verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            })
        }

        // 全选/全不选事件
        selectAllCheckBox.addActionListener {
            val newState = selectAllCheckBox.isSelected
            columnCheckboxes.forEach { it.isSelected = newState }
            updatePreview()
        }

        // 别名变化监听
        aliasTextField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
        })

        // 生成按钮
        generateButton.addActionListener {
            doOKAction()
        }

        // SQL 预览面板
        val previewPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(10, 0, 0, 0)
            add(JBLabel("SQL 预览："), BorderLayout.NORTH)
            add(JScrollPane(previewTextArea).apply {
                preferredSize = Dimension(400, 100)
            }, BorderLayout.CENTER)
        }

        // 按钮面板
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(generateButton)
            add(Box.createHorizontalStrut(10))
            add(JButton("一键复制 SQL").apply {
                addActionListener {
                    val sql = previewTextArea.text
                    if (sql.isNotBlank() && !sql.startsWith("-- 错误")) {
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(sql), null)
                        val originalText = text
                        text = "已复制!"
                        Timer(2000) { text = originalText }.apply {
                            isRepeats = false
                            start()
                        }
                    } else {
                        Messages.showErrorDialog(this@SelectGeneratorDialog.rootPane, "没有可复制的 SQL", "复制失败")
                    }
                }
            })
        }

        // 使用 FormBuilder 创建表单布局
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(infoPanel)
            .addComponent(aliasPanel)
            .addComponent(JBLabel("选择字段:"))
            .addComponent(columnsWrapperPanel)
            .addComponent(buttonPanel)
            .addComponent(previewPanel)
            .panel

        // 添加滚动支持
        val scrollPane = JScrollPane(formPanel).apply {
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            border = null
            preferredSize = Dimension(450, 400)
        }

        // 初始更新预览
        updatePreview()

        return scrollPane
    }

    /**
     * 更新 SQL 预览
     */
    private fun updatePreview() {
        val selectedColumns = getSelectedColumns()
        val alias = getAlias().takeIf { it.isNotBlank() }
        val sql = SelectGenerator.generateSelect(tableName, selectedColumns, alias)
        previewTextArea.text = sql
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return aliasTextField
    }

    override fun doOKAction() {
        if (columnCheckboxes.none { it.isSelected }) {
            Messages.showErrorDialog(
                this.rootPane,
                "请至少选择一个字段",
                "验证失败"
            )
            return
        }
        super.doOKAction()
    }

    /**
     * 获取选中的字段列表
     */
    fun getSelectedColumns(): List<String> {
        return columnCheckboxes
            .filter { it.isSelected }
            .map { it.text }
    }

    /**
     * 获取表别名
     */
    fun getAlias(): String {
        return aliasTextField.text.trim()
    }

    /**
     * 是否有别名
     */
    fun hasAlias(): Boolean {
        return aliasTextField.text.isNotBlank()
    }

    /**
     * 获取生成的 SQL
     */
    fun getGeneratedSql(): String {
        val selectedColumns = getSelectedColumns()
        val alias = getAlias().takeIf { it.isNotBlank() }
        return SelectGenerator.generateSelect(tableName, selectedColumns, alias)
    }
}

/**
 * WrapLayout - FlowLayout 的变种，支持自动换行
 * 当组件在一行中放不下时，会自动换行到下一行
 */
class WrapLayout : FlowLayout {
    constructor() : super()
    constructor(align: Int, hgap: Int, vgap: Int) : super(align, hgap, vgap)

    override fun preferredLayoutSize(target: Container): Dimension {
        return layoutSize(target, true)
    }

    override fun minimumLayoutSize(target: Container): Dimension {
        return layoutSize(target, false)
    }

    private fun layoutSize(target: Container?, preferred: Boolean): Dimension {
        synchronized(target!!.treeLock) {
            val insets = target.insets
            val tdim = target.size
            val nMembers = target.componentCount
            var rowMaxWidth = 0
            var rowTotalHeight = 0
            var x = insets.left
            var y = insets.top

            for (i in 0 until nMembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = if (preferred) m.preferredSize else m.minimumSize
                    val nextX = x + d.width
                    if (i > 0 && nextX - insets.left > tdim.width) {
                        x = insets.left
                        y += rowTotalHeight + vgap
                        rowTotalHeight = 0
                        rowMaxWidth = 0
                    }
                    x += d.width + hgap
                    rowTotalHeight = maxOf(rowTotalHeight, d.height)
                    rowMaxWidth = maxOf(rowMaxWidth, x)
                }
            }
            return Dimension(insets.left + insets.right + rowMaxWidth - hgap,
                           insets.top + insets.bottom + y + rowTotalHeight)
        }
    }

    override fun layoutContainer(target: Container) {
        synchronized(target.treeLock) {
            val targetSize = target.size
            val insets = target.insets
            val maxWidth = targetSize.width - insets.left - insets.right
            val nMembers = target.componentCount
            var x = insets.left
            var y = insets.top
            var rowHeight = 0
            var rowStartX = x

            for (i in 0 until nMembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = m.preferredSize
                    if (x + d.width > maxWidth + rowStartX && i > 0) {
                        x = rowStartX
                        y += rowHeight + vgap
                        rowHeight = 0
                    }
                    m.setBounds(x, y, d.width, d.height)
                    x += d.width + hgap
                    rowHeight = maxOf(rowHeight, d.height)
                }
            }
        }
    }
}
