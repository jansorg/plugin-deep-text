package dev.ja.deep.markdown.translateParagraphs

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.PriorityAction.Priority
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.psi.MarkdownElementVisitor
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement
import org.intellij.plugins.markdown.lang.psi.impl.*

class TranslateMarkdownParagraphInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : MarkdownElementVisitor() {
            override fun visitMarkdownFile(file: MarkdownFile) {
                registerForElementHierarchy(holder, "File", file, Priority.NORMAL)
            }

            override fun visitList(list: MarkdownList) {
                registerForElementHierarchy(holder, "List", list, Priority.HIGH)
            }

            override fun visitTable(table: MarkdownTable) {
                registerForElementHierarchy(holder, "Table", table, Priority.HIGH)
            }

            override fun visitHeader(header: MarkdownHeader) {
                registerForSingleElement(holder, "Header", header)
            }

            override fun visitBlockQuote(blockQuote: MarkdownBlockQuote) {
                registerForSingleElement(holder, "Paragraph", blockQuote)
            }

            override fun visitParagraph(paragraph: MarkdownParagraph) {
                registerForSingleElement(holder, "Paragraph", paragraph)
            }
        }
    }

    private fun registerForSingleElement(holder: ProblemsHolder, elementType: String, element: MarkdownPsiElement) {
        holder.problem(element, "")
            .highlight(ProblemHighlightType.INFORMATION)
            .fix(TranslateMarkdownParagraphQuickfix(elementType, holder.project, holder.file, listOf(element), Priority.TOP))
            .register()
    }

    private fun registerForElementHierarchy(
        holder: ProblemsHolder,
        elementType: String,
        elements: MarkdownPsiElement,
        priority: Priority,
    ) {
        val subElements = findTranslatableHierarchyElements(elements)
        if (subElements.isNotEmpty()) {
            holder.problem(elements, "")
                .highlight(ProblemHighlightType.INFORMATION)
                .fix(TranslateMarkdownParagraphQuickfix(elementType, holder.project, holder.file, subElements.toList(), priority))
                .register()
        }
    }

    private fun findTranslatableHierarchyElements(elements: MarkdownPsiElement): Collection<MarkdownPsiElement> {
        return PsiTreeUtil.findChildrenOfAnyType(elements, *translatableElementTypes)
            .filter {
                it.textLength > 0
            }
    }

    private val translatableElementTypes: Array<Class<out MarkdownPsiElement>> = arrayOf(
        MarkdownParagraph::class.java,
        MarkdownHeaderContent::class.java,
        MarkdownBlockQuote::class.java,
        MarkdownTableCell::class.java,
    )
}