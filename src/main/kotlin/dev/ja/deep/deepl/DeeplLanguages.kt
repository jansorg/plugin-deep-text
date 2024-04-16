package dev.ja.deep.deepl

import com.deepl.api.DeepLException
import com.deepl.api.Language
import com.deepl.api.Translator
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationManager
import dev.ja.deep.settings.DeepApplicationSettings
import java.util.concurrent.TimeUnit

object DeeplLanguages {
    private var cachedSourceLanguages: List<Language>? = null
    private var cachedTargetLanguages: List<Language>? = null

    private val fallbackSourceLangauges: List<Language>
    private val fallbackTargetLangauges: List<Language>

    init {
        val gson = GsonBuilder().create()

        this.fallbackSourceLangauges = this.javaClass.getResource("/deepl/sourceLanguages.json")?.readText()?.let { json ->
            gson.fromJson(json, Array<Language>::class.java).sortedBy { it.name }
        } ?: emptyList()

        this.fallbackTargetLangauges = this.javaClass.getResource("/deepl/targetLanguages.json")?.readText()?.let { json ->
            gson.fromJson(json, Array<Language>::class.java).sortedBy { it.name }
        } ?: emptyList()
    }

    fun sourceLanguages(): List<Language> {
        if (cachedSourceLanguages == null) {
            cachedSourceLanguages = fetchLanguages { it.sourceLanguages }
        }
        return cachedSourceLanguages ?: fallbackSourceLangauges
    }

    fun sourceLanguageByCode(code: String): Language? {
        return sourceLanguages().firstOrNull { it.code == code }
    }

    fun targetLanguages(): List<Language> {
        if (cachedTargetLanguages == null) {
            cachedTargetLanguages = fetchLanguages { it.targetLanguages }
        }
        return cachedTargetLanguages ?: fallbackTargetLangauges
    }

    fun targetLanguageByCode(code: String): Language? {
        return targetLanguages().firstOrNull { it.code == code }
    }

    private fun fetchLanguages(loader: (Translator) -> List<Language>): List<Language>? {
        val apiKey = DeepApplicationSettings.get().deeplApiKey
        return when {
            apiKey.isNotEmpty() -> try {
                ApplicationManager.getApplication().executeOnPooledThread<List<Language>> {
                    val translator = Translator(DeepApplicationSettings.get().deeplApiKey)
                    loader(translator).sortedBy { it.name }
                }.get(1_000, TimeUnit.MILLISECONDS)
            } catch (e: DeepLException) {
                null
            }

            else -> null
        }
    }
}