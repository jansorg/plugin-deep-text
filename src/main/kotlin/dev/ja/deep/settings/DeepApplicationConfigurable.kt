package dev.ja.deep.settings

import com.deepl.api.Formality
import com.deepl.api.Language
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import dev.ja.deep.deepl.DeeplLanguages
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
                comboBox(DeeplLanguages.sourceLanguages(), DeeplLanguageRenderer).bindItem(
                    {
                        DeeplLanguages.sourceLanguageByCode(settings.defaultSourceLanguage)
                    },
                    {
                        settings.defaultSourceLanguage = it?.code ?: ""
                    }
                ).columns(COLUMNS_LARGE)
            }

            val enableFormality = AtomicBooleanProperty(DeeplLanguages.targetLanguageByCode(settings.defaultTargetLanguage)?.supportsFormality == true)

            row(i18n("settings.deeplDefaultTargetLanguage.label")) {
                comboBox(DeeplLanguages.targetLanguages(), DeeplLanguageRenderer).bindItem(
                    {
                        DeeplLanguages.targetLanguageByCode(settings.defaultTargetLanguage)
                    },
                    {
                        settings.defaultTargetLanguage = it?.code ?: ""
                    }

                ).columns(COLUMNS_LARGE).onChanged {
                    enableFormality.set((it.selectedItem as? Language)?.supportsFormality == true)
                }
            }

            row(i18n("settings.deeplFormality.label")) {
                comboBox(Formality.entries, DeeplFormalityRenderer)
                    .bindItem(
                        { settings.defaultTargetFormality },
                        { settings.defaultTargetFormality = it ?: Formality.Default }
                    ).columns(COLUMNS_LARGE).enabledIf(enableFormality)
            }
        }
    }
}
