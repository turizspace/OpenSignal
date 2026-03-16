package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisHistoryEntry(
    val id: String,
    val analysis: CopilotAnalysis,
    val createdAtIso: String,
    val queuedForTraining: Boolean = false
)
