package dev.ja.deep.settings

import com.deepl.api.Formality
import com.intellij.ui.SimpleListCellRenderer
import javax.swing.JList

object DeeplFormalityRenderer : SimpleListCellRenderer<Formality>() {
    override fun customize(list: JList<out Formality>, value: Formality?, index: Int, selected: Boolean, hasFocus: Boolean) {
        this.text = value?.name
    }
}