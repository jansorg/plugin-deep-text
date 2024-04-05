package dev.ja.deep.settings

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

    @OptionTag("defaultTargetLanguage")
    @JvmField
    @Volatile
    var defaultTargetLanguage: String = "",
) {
    companion object {
        @JvmStatic
        fun get(): DeepApplicationSettings {
            return DeepApplicationSettingsService.getInstance().state
        }
    }
}
