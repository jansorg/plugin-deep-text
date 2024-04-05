package dev.ja.deep.markdown.translateParagraphs

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class TranslateMarkdownParagraphAction : AnAction(), DumbAware {
    private val translator = ParagraphsTranslator()

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = translator.isAvailable(e.dataContext)
    }

    override fun actionPerformed(e: AnActionEvent) {
        translator.translate(e.dataContext)
    }
}