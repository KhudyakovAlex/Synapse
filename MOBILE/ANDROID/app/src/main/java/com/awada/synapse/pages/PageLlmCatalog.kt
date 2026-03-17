package com.awada.synapse.pages

data class LLMPageDescriptor(
    val fileName: String,
    val screenName: String? = null,
    val titleRu: String,
    val description: String
)

internal val LlmPageCatalog: List<LLMPageDescriptor> = listOf(
    PageLocationsLlmDescriptor,
    PageLocationLlmDescriptor,
    PageRoomLlmDescriptor,
    PageGroupLlmDescriptor,
    PageRoomSettingsLlmDescriptor,
    PageLocationSettingsLlmDescriptor,
    PageLumLlmDescriptor,
    PageSearchLlmDescriptor,
    PageSettingsLlmDescriptor,
    PageLumSettingsLlmDescriptor,
    PageSensorPressSettingsLlmDescriptor,
    PageSensorBrightSettingsLlmDescriptor,
    PageButtonPanelSettingsLlmDescriptor,
    PageButtonSettingsLlmDescriptor,
    PageScenarioLlmDescriptor,
    PageButtonPanelLlmDescriptor,
    PagePasswordLlmDescriptor,
    PageChangePasswordLlmDescriptor,
    PageGraphLlmDescriptor,
    PageGraphsLlmDescriptor,
    PageIconSelectLlmDescriptor,
    PageScheduleLlmDescriptor,
    PageSchedulePointLlmDescriptor
)

fun buildLlmPageCatalogMarkdown(): String = buildString {
    val navigationScreens = LlmPageCatalog
        .mapNotNull { it.screenName }
        .joinToString(", ") { "`$it`" }

    appendLine("Допустимые значения `navigation.screen`:")
    appendLine(navigationScreens)
    appendLine()
    appendLine("Все страницы приложения:")
    LlmPageCatalog.forEach { page ->
        val navigationNote = page.screenName?.let { " screen=`$it`." } ?: " Вложенная страница без прямого значения `navigation.screen`."
        appendLine("- `${page.fileName}` - ${page.titleRu}. ${page.description}$navigationNote")
    }
}
