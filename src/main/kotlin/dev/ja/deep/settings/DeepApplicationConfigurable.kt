package dev.ja.deep.settings

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import dev.ja.deep.i18n.DeepBundle.i18n

class DeepApplicationConfigurable : BoundSearchableConfigurable(i18n("settings.title"), "") {
    override fun createPanel(): DialogPanel {
        val settings = DeepApplicationSettings.get()

        return panel {
            row(i18n("settings.deeplApiKey.label")) {
                textField()
                    .bindText(settings::deeplApiKey)
                    .columns(COLUMNS_LARGE)
            }

            row {
                checkBox(i18n("settings.deeplFreeAccount.label")).bindSelected(settings::deeplFreeAcount)
            }

            row(i18n("settings.deeplDefaultSourceLanguage.label")) {
                textField()
                    .bindText(settings::defaultSourceLanguage)
                    .columns(COLUMNS_MEDIUM)
            }

            row(i18n("settings.deeplDefaultTargetLanguage.label")) {
                textField()
                    .bindText(settings::defaultTargetLanguage)
                    .columns(COLUMNS_MEDIUM)
            }
        }
    }
}
