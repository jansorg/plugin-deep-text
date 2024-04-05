package dev.ja.deep.markdown.translateParagraphs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import dev.ja.deep.deepl.DeeplService
import dev.ja.deep.deepl.TextBlock
import dev.ja.deep.i18n.DeepBundle.i18n
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PsiElementTranslator {
    @Suppress("UnstableApiUsage")
    fun translate(project: Project, psiElementsWithIgnored: List<PsiElementWithIgnored>) {
        if (psiElementsWithIgnored.isEmpty()) {
            return
        }

        runWithModalProgressBlocking(project, i18n("action.translateParagraph.progressTitle")) {
            val textBlocks = runReadAction {
                psiElementsWithIgnored.map {
                    TextBlock(it.psiElement.text, it.ignoredRanges)
                }
            }

            val translations = DeeplService.getInstance().translateTexts(textBlocks)
            if (translations != null && translations.size == psiElementsWithIgnored.size) {
                withContext(Dispatchers.EDT) {
                    blockingContext {
                        WriteCommandAction.runWriteCommandAction(
                            project,
                            i18n("action.translateParagraph.commandName"),
                            null,
                            { applyTranslation(project, psiElementsWithIgnored.zip(translations)) })
                    }
                }
            }
        }
    }

    @RequiresWriteLock
    private fun applyTranslation(project: Project, translations: List<Pair<PsiElementWithIgnored, String>>) {
        ApplicationManager.getApplication().assertWriteAccessAllowed()

        val sortedTranslations = translations.sortedByDescending {
            it.first.psiElement.textRange.endOffset
        }

        for ((sourceElement, translation) in sortedTranslations) {
            val psiFile = sourceElement.psiElement.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            if (document != null) {
                DeeplService.getInstance().applyTranslatedTextBlock(document, sourceElement.psiElement.textRange, translation)
            }
        }
    }
}