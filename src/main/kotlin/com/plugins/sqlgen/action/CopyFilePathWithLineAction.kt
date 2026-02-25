package com.plugins.sqlgen.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection

/**
 * 复制文件路径和选中行号的动作
 */
class CopyFilePathWithLineAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        // 获取文件路径
        val filePath = virtualFile.path
        
        // 获取选中的行号（如果有选中的文本）
        val selectionModel = editor.selectionModel
        val document = editor.document
        
        val lineInfo = if (selectionModel.hasSelection()) {
            // 有选中文本，获取起始行和结束行
            val startOffset = selectionModel.selectionStart
            val endOffset = selectionModel.selectionEnd
            val startLine = document.getLineNumber(startOffset) + 1 // 转换为1-based
            val endLine = document.getLineNumber(endOffset) + 1
            
            if (startLine == endLine) {
                ":$startLine"
            } else {
                ":$startLine-$endLine"
            }
        } else {
            // 没有选中文本，获取光标所在行
            val caretModel = editor.caretModel
            val line = document.getLineNumber(caretModel.offset) + 1
            ":$line"
        }
        
        // 组合成最终格式: filepath:line 或 filepath:start-end
        val result = "$filePath$lineInfo"
        
        // 复制到剪贴板
        CopyPasteManager.getInstance().setContents(StringSelection(result))
    }

    override fun update(e: AnActionEvent) {
        // 只在有打开的文件时显示
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = virtualFile != null
    }
}
