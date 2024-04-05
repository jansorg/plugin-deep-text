package dev.ja.deep.markdown.translateParagraphs

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

data class PsiElementWithIgnored(
    val psiElement: PsiElement,
    val ignoredRanges: List<TextRange> = emptyList(),
)

data class PsiElementPointerWithIgnored(
    val elementPointer: SmartPsiElementPointer<PsiElement>,
    val ignoredRanges: List<TextRange>,
)