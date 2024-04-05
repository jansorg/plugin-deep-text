package dev.ja.deep.markdown.translateParagraphs

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.findParentOfType
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import dev.ja.deep.deepl.DeeplService
import dev.ja.deep.i18n.DeepBundle.i18n
import dev.ja.deep.settings.DeepApplicationSettings
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraph

@Suppress("UnstableApiUsage")
internal class ParagraphsTranslator {
    fun isAvailable(context: DataContext): Boolean {
        return isAvailable(PlatformDataKeys.PROJECT.getData(context), findContextElement(context))
    }

    fun isAvailable(project: Project?, psiElement: PsiElement?): Boolean {
        val settings = DeepApplicationSettings.get()
        return project != null && settings.deeplApiKey.isNotEmpty() && psiElement != null
    }

    @RequiresEdt
    fun translate(context: DataContext) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        val project = PlatformDataKeys.PROJECT.getData(context)!!
        translate(project, listOfNotNull(findElement(context)))
    }

    fun translate(project: Project, psiElements: List<MarkdownPsiElement>) {
        if (psiElements.isEmpty()) {
            return
        }

        runWithModalProgressBlocking(project, i18n("action.translateParagraph.progressTitle")) {
            val textBlocks = runReadAction {
                psiElements.map { it.text }
            }

            val translations = DeeplService.getInstance().translateTexts(textBlocks)
            if (translations != null) {
                writeAction {
                    CommandProcessor.getInstance().executeCommand(
                        project,
                        { applyTranslation(project, psiElements, translations) },
                        i18n("action.translateParagraph.commandName"),
                        null
                    )
                }
            }
        }
    }

    @RequiresWriteLock
    private fun applyTranslation(project: Project, sourceElements: List<MarkdownPsiElement>, translations: List<String>) {
        ApplicationManager.getApplication().assertWriteAccessAllowed()

        val sourcesWithTranslation = sourceElements.zip(translations).sortedByDescending {
            it.first.textOffset
        }

        for ((sourceElement, translation) in sourcesWithTranslation) {
            val newFile = MarkdownPsiElementFactory.createFile(project, translation)

            if (newFile.firstChild != null) {
                val sourceParent = sourceElement.parent
                sourceParent.addRangeAfter(newFile.firstChild, newFile.lastChild, sourceElement)
                sourceElement.delete()
            }
        }
    }

    private fun findElement(context: DataContext): MarkdownParagraph? {
        val contextElement = findContextElement(context) ?: return null
        return contextElement.findParentOfType(strict = false)
    }

    private fun findContextElement(context: DataContext): PsiElement? {
        val project = PlatformDataKeys.PROJECT.getData(context) ?: return null
        val editor = PlatformDataKeys.EDITOR.getData(context) ?: return null
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return null
        return psiFile.findElementAt(editor.caretModel.offset)
    }
}