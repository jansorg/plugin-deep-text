package dev.ja.deep.markdown.translateParagraphs

import com.intellij.codeInsight.intention.CustomizableIntentionAction.RangeToHighlight
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.PriorityAction.Priority
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPointerManager
import dev.ja.deep.i18n.DeepBundle.i18n
import dev.ja.deep.settings.DeepApplicationSettings

class TranslateMarkdownElementsQuickfix(
    private val contextName: String,
    project: Project,
    psiElements: List<PsiElementWithIgnored>,
    private val priority: Priority,
) : LocalQuickFix, PriorityAction {
    private val translator = PsiElementTranslator()
    private val psiElementPointers: List<PsiElementPointerWithIgnored>

    init {
        val manager = SmartPointerManager.getInstance(project)
        psiElementPointers = psiElements.map {
            PsiElementPointerWithIgnored(manager.createSmartPsiElementPointer(it.psiElement), it.ignoredRanges)
        }
    }

    override fun getPriority(): Priority {
        return priority
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getFamilyName(): String {
        return i18n("quickfix.markdown.family")
    }

    override fun getName(): String {
        return i18n("quickfix.markdown.translateParagraphs.text", contextName)
    }

    override fun availableInBatchMode(): Boolean {
        return false
    }

    override fun getRangesToHighlight(project: Project, descriptor: ProblemDescriptor): List<RangeToHighlight> {
        val file = descriptor.psiElement.containingFile
        return psiElementPointers
            .mapNotNull { it.elementPointer.psiRange }
            .map { RangeToHighlight(file, TextRange(it.startOffset, it.endOffset), EditorColors.SEARCH_RESULT_ATTRIBUTES) }
            .takeIf { it.size == psiElementPointers.size }
            ?: emptyList()
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (DeepApplicationSettings.get().deeplApiKey.isEmpty()) {
            Messages.showErrorDialog(project, i18n("error.apiKeyMissing.message"), i18n("error.apiKeyMissing.title"))
            return
        }
        if (DeepApplicationSettings.get().defaultTargetLanguage.isEmpty()) {
            Messages.showErrorDialog(project, i18n("error.targetLanguageMissing.message"), i18n("error.targetLanguageMissing.title"))
            return
        }

        val markdownElementsWithIgnored = runReadAction {
            psiElementPointers.mapNotNull { withIgnored ->
                withIgnored.elementPointer.element?.let {
                    PsiElementWithIgnored(it, withIgnored.ignoredRanges)
                }
            }
        }

        if (markdownElementsWithIgnored.size != psiElementPointers.size) {
            return
        }

        runInEdt(ModalityState.defaultModalityState()) {
            translator.translate(project, markdownElementsWithIgnored)
        }
    }
}