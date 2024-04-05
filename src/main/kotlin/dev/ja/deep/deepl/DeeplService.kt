package dev.ja.deep.deepl

import com.deepl.api.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiElement
import dev.ja.deep.settings.DeepApplicationSettings
import java.io.ByteArrayOutputStream

@Suppress("UnstableApiUsage")
@Service(Service.Level.APP)
class DeeplService {
    companion object {
        private val LOG = logger<DeeplService>()

        fun getInstance(): DeeplService {
            return service()
        }
    }

    /**
     * Translates the element as document and returns the result.
     */
    fun translateDocumentUpload(element: PsiElement, targetLanguage: String = DeepApplicationSettings.get().defaultTargetLanguage): String? {
        ApplicationManager.getApplication().assertIsNonDispatchThread()

        val (file, elementText) = runReadAction {
            element.containingFile to element.text
        }

        val fileName = file.name
        val elementTextStream = elementText.byteInputStream()

        val translator = createTranslator()
        val translationOptions = DocumentTranslationOptions()

        val handle = translator.translateDocumentUpload(elementTextStream, fileName, null, targetLanguage, translationOptions)
        val status = translator.translateDocumentWaitUntilDone(handle)
        logDocumentStatus(status)

        return when {
            status.ok() -> {
                val target = ByteArrayOutputStream()
                translator.translateDocumentDownload(handle, target)
                target.toString(Charsets.UTF_8)
            }

            else -> null
        }
    }

    /**
     * Translates the element as document and returns the result.
     */
    fun translateTexts(textBlocks: List<String>, targetLanguage: String = DeepApplicationSettings.get().defaultTargetLanguage): List<String>? {
        ApplicationManager.getApplication().assertIsNonDispatchThread()

        val translator = createTranslator()
        val translationOptions = TextTranslationOptions()

        return try {
            val results = translator.translateText(textBlocks, null, targetLanguage, translationOptions)
            results.map { it.text }
        } catch (e: DeepLException) {
            null
        }
    }

    private fun createTranslator(): Translator {
        val settings = DeepApplicationSettings.get()
        val key = settings.deeplApiKey
        val isFreeApi = settings.deeplFreeAcount

        val options = TranslatorOptions().apply {
            this.serverUrl = getApiHost(isFreeApi)
        }

        return Translator(key, options)
    }

    private fun getApiHost(isFreeApi: Boolean) = when {
        isFreeApi -> "https://api-free.deepl.com"
        else -> "https://api.deepl.com"
    }

    private fun logDocumentStatus(status: DocumentStatus) {
        when {
            status.ok() -> LOG.debug("DeepL document status: ${status.status}, ${status.billedCharacters}")
            else -> LOG.debug("DeepL document status: ${status.status}, ${status.billedCharacters}, ${status.errorMessage}")
        }
    }
}