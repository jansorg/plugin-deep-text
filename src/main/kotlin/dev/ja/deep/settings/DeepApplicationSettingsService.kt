package dev.ja.deep.settings

import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(name = "deep", storages = [Storage("deep.xml")])
class DeepApplicationSettingsService : PersistentStateComponent<DeepApplicationSettings> {
    @Volatile
    private var state = DeepApplicationSettings()

    override fun getState(): DeepApplicationSettings {
        return state
    }

    override fun loadState(state: DeepApplicationSettings) {
        this.state = state
    }

    companion object {
        @JvmStatic
        fun getInstance(): DeepApplicationSettingsService {
            return service()
        }
    }
}