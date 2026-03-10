package opensignal.settings

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<UserSettings>

    suspend fun update(transform: (UserSettings) -> UserSettings)
}
