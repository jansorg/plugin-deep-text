package dev.ja.deep.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider

class DeepApplicationSettingsConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable? {
        return DeepApplicationConfigurable()
    }
}