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
    val currentScreen: LLMCurrentScreenContext
)

@Serializable
data class LLMCurrentScreenContext(
    @SerialName("name")
    val name: String,
    @SerialName("params")
    val params: LLMCurrentScreenParams = LLMCurrentScreenParams()
)

@Serializable
data class LLMCurrentScreenParams(
    @SerialName("controllerId")
    val controllerId: Int? = null,
    @SerialName("roomId")
    val roomId: Int? = null,
    @SerialName("groupId")
    val groupId: Int? = null,
    @SerialName("luminaireId")
    val luminaireId: Long? = null,
    @SerialName("buttonPanelId")
    val buttonPanelId: Long? = null,
    @SerialName("buttonNumber")
    val buttonNumber: Int? = null,
    @SerialName("scenarioId")
    val scenarioId: Long? = null,
    @SerialName("presSensorId")
    val presSensorId: Long? = null,
    @SerialName("brightSensorId")
    val brightSensorId: Long? = null
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
