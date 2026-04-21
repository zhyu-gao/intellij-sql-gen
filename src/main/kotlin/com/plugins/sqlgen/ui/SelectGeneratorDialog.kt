package com.plugins.sqlgen.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.UIUtil
import com.plugins.sqlgen.model.ColumnInfo
import com.plugins.sqlgen.util.SelectGenerator
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Color
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder
import java.awt.Toolkit

class SelectGeneratorDialog(private val tableName: String, private val columns: List<ColumnInfo>) : DialogWrapper(true) {

    private val columnCheckboxes: MutableList<JBCheckBox> = mutableListOf()
    private val aliasTextField: JBTextField = JBTextField()
    private val selectAllCheckBox: JBCheckBox = JBCheckBox("全选/全不选")
    private val previewTextArea: JTextArea = JTextArea(5, 40)

    init {
        init()
        title = "SQL 生成器"
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(16, 20, 16, 20)
        }

        val contentPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = Insets(0, 0, 12, 0)
            weightx = 1.0
            gridwidth = GridBagConstraints.REMAINDER
        }

        contentPanel.add(createHeaderPanel(), gbc)

        gbc.insets = Insets(0, 0, 16, 0)
        contentPanel.add(createAliasPanel(), gbc)

        gbc.fill = GridBagConstraints.BOTH
        gbc.weighty = 1.0
        contentPanel.add(createColumnsPanel(), gbc)

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weighty = 0.0
        gbc.insets = Insets(0, 0, 12, 0)
        contentPanel.add(createButtonPanel(), gbc)

        gbc.insets = Insets(0, 0, 0, 0)
        contentPanel.add(createPreviewPanel(), gbc)

        mainPanel.add(contentPanel, BorderLayout.CENTER)
        updatePreview()

        return mainPanel
    }

    private fun createHeaderPanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            border = CompoundBorder(
                LineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                EmptyBorder(12, 16, 12, 16)
            )

            val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
            leftPanel.add(JLabel("📋").apply { font = Font("Segoe UI Emoji", Font.PLAIN, 20) })
            leftPanel.add(JBLabel("数据表:").apply { font = UIUtil.getLabelFont().deriveFont(Font.BOLD) })
            add(leftPanel, BorderLayout.WEST)

            add(JBLabel(tableName).apply {
                font = UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f)
                foreground = UIManager.getColor("Link.activeForeground")
            }, BorderLayout.CENTER)
        }
    }

    private fun createAliasPanel(): JPanel {
        return JPanel(BorderLayout(12, 0)).apply {
            add(JBLabel("表别名:").apply { preferredSize = Dimension(70, preferredSize.height) }, BorderLayout.WEST)
            add(aliasTextField.apply {
                emptyText.text = "可选，如: t1"
                toolTipText = "为表设置别名，生成的 SQL 将使用别名引用字段"
                document.addDocumentListener(object : javax.swing.event.DocumentListener {
                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) { updatePreview() }
                })
            }, BorderLayout.CENTER)
        }
    }

    private fun createColumnsPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            border = TitledBorder(
                CompoundBorder(LineBorder(UIManager.getColor("Component.borderColor")), EmptyBorder(8, 8, 8, 8)),
                "选择字段 (${columns.size}个)",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                UIUtil.getLabelFont().deriveFont(Font.BOLD),
                UIManager.getColor("Label.foreground")
            )
        }

        val toolbarPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(0, 0, 8, 0)
            add(selectAllCheckBox.apply {
                isSelected = true
                addActionListener {
                    val newState = isSelected
                    columnCheckboxes.forEach { it.isSelected = newState }
                    updatePreview()
                }
            }, BorderLayout.WEST)
        }
        panel.add(toolbarPanel, BorderLayout.NORTH)

        val columnsContainer = JPanel(WrapLayout(FlowLayout.LEFT, 10, 8)).apply {
            columns.forEach { column ->
                val checkBox = JBCheckBox(column.name).apply {
                    isSelected = column.selected
                    toolTipText = buildString {
                        append("<html><b>字段:</b> ${column.name}<br><b>可空:</b> ${if (column.nullable) "是" else "否"}")
                        column.defaultValue?.let { append("<br><b>默认值:</b> $it") }
                        column.comment?.let { append("<br><b>注释:</b> $it") }
                        append("</html>")
                    }
                    addActionListener { updatePreview() }
                }
                columnCheckboxes.add(checkBox)
                add(checkBox)
            }
        }

        panel.add(JScrollPane(columnsContainer).apply {
            border = null
            preferredSize = Dimension(400, 180)
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }, BorderLayout.CENTER)

        return panel
    }

    private fun createButtonPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT, 12, 0)).apply {
            add(JButton("📋 复制 SQL").apply {
                font = UIUtil.getLabelFont().deriveFont(Font.BOLD)
                toolTipText = "复制生成的 SQL 到剪贴板"
                addActionListener {
                    val sql = previewTextArea.text
                    if (sql.isNotBlank() && !sql.startsWith("-- 错误")) {
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(sql), null)
                        val originalText = text
                        text = "✓ 已复制!"
                        Timer(2000) { text = originalText }.apply { isRepeats = false; start() }
                    } else {
                        Messages.showErrorDialog(rootPane, "没有可复制的 SQL", "复制失败")
                    }
                }
            })

            add(JButton("插入到编辑器 →").apply {
                toolTipText = "将 SQL 插入到当前编辑器光标位置"
                addActionListener { doOKAction() }
            })
        }
    }

    private fun createPreviewPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            border = TitledBorder(
                CompoundBorder(LineBorder(UIManager.getColor("Component.borderColor")), EmptyBorder(8, 8, 8, 8)),
                "SQL 预览",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                UIUtil.getLabelFont().deriveFont(Font.BOLD),
                UIManager.getColor("Label.foreground")
            )
        }

        previewTextArea.apply {
            font = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.let { fonts ->
                when {
                    fonts.contains("JetBrains Mono") -> Font("JetBrains Mono", Font.PLAIN, 13)
                    fonts.contains("Consolas") -> Font("Consolas", Font.PLAIN, 13)
                    else -> Font(Font.MONOSPACED, Font.PLAIN, 13)
                }
            }
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            background = Color(250, 250, 250)
            foreground = Color(51, 51, 51)
            border = EmptyBorder(12, 12, 12, 12)
        }

        panel.add(JScrollPane(previewTextArea).apply {
            border = LineBorder(Color(221, 221, 221), 1)
            preferredSize = Dimension(400, 120)
        }, BorderLayout.CENTER)

        return panel
    }

    private fun updatePreview() {
        val selectedColumns = getSelectedColumns()
        val alias = getAlias().takeIf { it.isNotBlank() }
        previewTextArea.text = SelectGenerator.generateSelect(tableName, selectedColumns, alias)
    }

    override fun getPreferredFocusedComponent() = aliasTextField

    override fun doOKAction() {
        if (columnCheckboxes.none { it.isSelected }) {
            Messages.showErrorDialog(rootPane, "请至少选择一个字段", "验证失败")
            return
        }
        super.doOKAction()
    }

    fun getSelectedColumns() = columnCheckboxes.filter { it.isSelected }.map { it.text }
    fun getAlias() = aliasTextField.text.trim()
    fun hasAlias() = aliasTextField.text.isNotBlank()
    fun getGeneratedSql() = SelectGenerator.generateSelect(tableName, getSelectedColumns(), getAlias().takeIf { it.isNotBlank() })
}

class WrapLayout : FlowLayout {
    constructor() : super()
    constructor(align: Int, hgap: Int, vgap: Int) : super(align, hgap, vgap)

    override fun preferredLayoutSize(target: Container) = layoutSize(target, true)
    override fun minimumLayoutSize(target: Container) = layoutSize(target, false)

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
            return Dimension(insets.left + insets.right + rowMaxWidth - hgap, insets.top + insets.bottom + y + rowTotalHeight)
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