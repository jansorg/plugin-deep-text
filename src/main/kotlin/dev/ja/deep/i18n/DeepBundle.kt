package dev.ja.deep.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

object DeepBundle {
    private val bundle = DynamicBundle(DeepBundle::class.java, "messages.plugin-deep")

    fun i18n(@PropertyKey(resourceBundle = "messages.plugin-deep") key: String, vararg args: Any): String {
        return bundle.getMessage(key, *args)
    }
}