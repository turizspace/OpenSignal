package opensignal.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemorySettingsRepository(
    initial: UserSettings = UserSettings()
) : SettingsRepository {

    private val state = MutableStateFlow(initial)

    override val settings: StateFlow<UserSettings> = state

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        state.value = transform(state.value)
    }
}
