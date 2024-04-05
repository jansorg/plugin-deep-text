package dev.ja.deep.deepl

import com.intellij.openapi.util.TextRange

data class TextBlock(
    val content: String,
    val ignoredRanges: List<TextRange>,
) {

    companion object {
        const val TAG_NAME = "x"
        const val START_TAG = "<$TAG_NAME>"
        const val END_TAG = "</$TAG_NAME>"
    }

    fun toApiInputString(): String {
        if (ignoredRanges.isEmpty()) {
            return content
        }

        val updated = StringBuilder(content)
        for (range in ignoredRanges.sortedByDescending { it.endOffset }) {
            updated.replace(range.startOffset, range.endOffset, "$START_TAG${range.substring(content)}$END_TAG")
        }
        return updated.toString()
    }
}