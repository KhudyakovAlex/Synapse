package com.awada.synapse.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LLMChatMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class LLMUiContext(
    @SerialName("currentScreen")
    val currentScreen: String,
    @SerialName("selectedLocationControllerId")
    val selectedLocationControllerId: Int? = null,
    @SerialName("selectedRoomControllerId")
    val selectedRoomControllerId: Int? = null,
    @SerialName("selectedRoomId")
    val selectedRoomId: Int? = null,
    @SerialName("selectedGroupControllerId")
    val selectedGroupControllerId: Int? = null,
    @SerialName("selectedGroupId")
    val selectedGroupId: Int? = null,
    @SerialName("selectedLuminaireId")
    val selectedLuminaireId: Long? = null,
    @SerialName("selectedButtonPanelId")
    val selectedButtonPanelId: Long? = null,
    @SerialName("selectedButtonNumber")
    val selectedButtonNumber: Int? = null,
    @SerialName("selectedScenarioId")
    val selectedScenarioId: Long? = null,
    @SerialName("selectedPresSensorId")
    val selectedPresSensorId: Long? = null,
    @SerialName("selectedBrightSensorId")
    val selectedBrightSensorId: Long? = null
)

@Serializable
data class LLMStructuredReply(
    @SerialName("assistantText")
    val assistantText: String = "",
    @SerialName("dbPatch")
    val dbPatch: LLMDbPatch? = null,
    @SerialName("navigation")
    val navigation: LLMNavigationCommand? = null
)

@Serializable
data class LLMDbPatch(
    @SerialName("updates")
    val updates: List<LLMDbUpdate> = emptyList()
)

@Serializable
data class LLMDbUpdate(
    @SerialName("table")
    val table: String,
    @SerialName("where")
    val where: JsonObject = JsonObject(emptyMap()),
    @SerialName("values")
    val values: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class LLMNavigationCommand(
    @SerialName("screen")
    val screen: String,
    @SerialName("controllerId")
    val controllerId: Int? = null,
    @SerialName("roomId")
    val roomId: Int? = null,
    @SerialName("groupId")
    val groupId: Int? = null,
    @SerialName("luminaireId")
    val luminaireId: Long? = null,
    @SerialName("presSensorId")
    val presSensorId: Long? = null,
    @SerialName("brightSensorId")
    val brightSensorId: Long? = null,
    @SerialName("buttonPanelId")
    val buttonPanelId: Long? = null,
    @SerialName("buttonNumber")
    val buttonNumber: Int? = null,
    @SerialName("scenarioId")
    val scenarioId: Long? = null
)

data class LLMConversationResult(
    val assistantText: String,
    val navigation: LLMNavigationCommand? = null
)
