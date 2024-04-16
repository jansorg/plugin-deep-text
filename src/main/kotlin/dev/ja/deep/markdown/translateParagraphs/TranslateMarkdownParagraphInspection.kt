package dev.ja.deep.markdown.translateParagraphs

import com.intellij.codeInsight.intention.PriorityAction.Priority
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
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
        val ignoredSubRanges = MarkdownElementRanges.findIgnoredRangesInElement(element)
        val quickfix = TranslateMarkdownElementsQuickfix(
            elementType,
            holder.project,
            listOf(PsiElementWithIgnored(element, ignoredSubRanges)),
            Priority.TOP
        )

        holder.problem(element, "").fix(quickfix).register()
    }

    private fun registerForElementHierarchy(
        holder: ProblemsHolder,
        elementType: String,
        contextElement: MarkdownPsiElement,
        priority: Priority,
    ) {
        val subElements = MarkdownElementRanges.findTranslatableChildren(contextElement)
        if (subElements.isNotEmpty()) {
            val ignoredSubRanges = subElements.map {
                PsiElementWithIgnored(it, MarkdownElementRanges.findIgnoredRangesInElement(it))
            }

            val quickfix = TranslateMarkdownElementsQuickfix(
                elementType,
                holder.project,
                ignoredSubRanges,
                priority
            )

            holder.problem(contextElement, "").fix(quickfix).register()
        }
    }
}