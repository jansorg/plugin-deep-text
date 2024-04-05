package dev.ja.deep.markdown.translateParagraphs

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownInlineLink

internal object MarkdownElementRanges {
    fun findTranslatableChildren(context: MarkdownPsiElement): List<PsiElement> {
        if (context.elementType in translatableElementTypes) {
            return listOf(context)
        }

        return PsiTreeUtil
            .collectElements(context) { it.elementType in translatableElementTypes }
            .filter { it.textLength > 0 }
    }

    fun findIgnoredRangesInElement(element: PsiElement): List<TextRange> {
        val ignoredRanges = mutableListOf<TextRange>()
        var c = element.firstChild
        while (c != null) {
            if (c.isRejectedElement()) {
                val maxIgnored = c.textRange

                var lastIgnoredEnd = maxIgnored.startOffset
                for (translated in c.findValidNestedElements()) {
                    val translatedStartOffset = translated.textRange.startOffset
                    if (lastIgnoredEnd < translatedStartOffset) {
                        ignoredRanges += TextRange(lastIgnoredEnd, translatedStartOffset)
                    }
                    lastIgnoredEnd = translated.textRange.endOffset
                }

                if (lastIgnoredEnd < maxIgnored.endOffset) {
                    ignoredRanges += TextRange(lastIgnoredEnd, maxIgnored.endOffset)
                }
            }
            c = c.nextSibling
        }

        // make relative to context element
        val delta = element.textRange.startOffset
        return ignoredRanges.map { it.shiftLeft(delta) }
    }

    /*
        private fun splitCompositeElement(element: PsiElement): List<PsiElement> {
            val first = element.firstChild ?: return emptyList()
            val last = element.lastChild ?: return emptyList()
            if (first == last) {
                return listOf(element)
            }

            val result = mutableListOf<PsiElement>()
            var hasRejected = false
            var c = element.firstChild
            while (c != null) {
                if (c.isRejectedElement()) {
                    result += c.findValidNestedElements()
                    hasRejected = true
                } else {
                    result += c
                }
                c = c.nextSibling
            }

            return when {
                hasRejected -> result
                else -> listOf(element)
            }
        }
    */

    private fun PsiElement.isRejectedElement(): Boolean {
        val elementType = this.node.elementType
        return elementType in rejectedElements
    }

    @Suppress("UnstableApiUsage")
    private fun PsiElement.findValidNestedElements(): List<PsiElement> {
        val elementType = this.elementType
        if (elementType in inlineFormattingElements) {
            val textContainer = when {
                elementType == MarkdownElementTypes.INLINE_LINK -> (this as MarkdownInlineLink).linkText
                else -> this
            } ?: return emptyList()

            return textContainer.childLeafs().filter { it.elementType == MarkdownTokenTypes.TEXT }.toList()
        }

        return emptyList()
    }

    // inline elements, which contain TEXT leafs, which are part of the translated content
    private val inlineFormattingElements = TokenSet.create(
        MarkdownElementTypes.EMPH,
        MarkdownElementTypes.STRONG,
        MarkdownElementTypes.STRIKETHROUGH,
        MarkdownElementTypes.INLINE_LINK
    )

    private val rejectedElements = TokenSet.orSet(
        inlineFormattingElements,
        TokenSet.create(
            MarkdownElementTypes.INLINE_LINK,
            MarkdownElementTypes.CODE_SPAN,
        )
    )

    private val translatableElementTypes = TokenSet.orSet(
        MarkdownTokenTypeSets.HEADER_CONTENT,
        TokenSet.create(
            MarkdownElementTypes.PARAGRAPH,
            MarkdownElementTypes.BLOCK_QUOTE,
            MarkdownElementTypes.TABLE_HEADER,
            MarkdownElementTypes.TABLE_CELL,
        )
    )
}
