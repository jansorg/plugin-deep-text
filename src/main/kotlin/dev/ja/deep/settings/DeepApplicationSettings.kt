package dev.ja.deep.settings

import com.deepl.api.Formality
import com.intellij.util.xmlb.annotations.OptionTag

data class DeepApplicationSettings(
    @OptionTag("deeplApiKey")
    @JvmField
    @Volatile
    var deeplApiKey: String = "",

    @OptionTag("deeplFree")
    @JvmField
    @Volatile
    var deeplFreeAcount: Boolean = false,

    @OptionTag("defaultSourceLanguage")
    @JvmField
    @Volatile
    var defaultSourceLanguage: String = "",

    @OptionTag("defaultTargetLanguage")
    @JvmField
    @Volatile
    var defaultTargetLanguage: String = "",

    @OptionTag("defaultTargetFormality")
    @JvmField
    @Volatile
    var defaultTargetFormality: Formality = Formality.Default,
) {
    companion object {
        @JvmStatic
        fun get(): DeepApplicationSettings {
            return DeepApplicationSettingsService.getInstance().state
        }
    }
}
