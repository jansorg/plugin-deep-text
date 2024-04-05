package dev.ja.deep.settings

import com.deepl.api.Language
import com.intellij.ui.SimpleListCellRenderer
import javax.swing.JList

object DeeplLanguageRenderer : SimpleListCellRenderer<Language>() {
    override fun customize(list: JList<out Language>, value: Language?, index: Int, selected: Boolean, hasFocus: Boolean) {
        this.text = value?.name
    }
}
