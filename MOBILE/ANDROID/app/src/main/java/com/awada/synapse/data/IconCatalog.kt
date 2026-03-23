package com.awada.synapse.data

import kotlinx.serialization.Serializable

@Serializable
data class IconInfo(
    val category: String,
    val id: Int,
    val description: String,
    val resourceName: String
)

@Serializable
data class IconCatalog(
    val icons: List<IconInfo>
) {
    // Фильтрация по категории с сортировкой по ID
    fun getByCategory(category: String): List<IconInfo> =
        icons.filter { it.category == category }.sortedBy { it.id }
    
    // Поиск по ID
    fun findById(id: Int): IconInfo? =
        icons.find { it.id == id }
    
    // Все категории
    val categories: List<String>
        get() = icons.map { it.category }.distinct()

    fun suggestRoomIconIdByName(roomName: String): Int? {
        val normalized = normalizeIconLookupText(roomName)
        if (normalized.isBlank()) return null

        val roomIcons = getByCategory("location")
        if (roomIcons.isEmpty()) return null

        val directMatch = ROOM_ICON_MATCHERS.firstOrNull { (_, keywords) ->
            keywords.any { keyword -> normalized.contains(keyword) }
        }?.first
        if (directMatch != null && roomIcons.any { it.id == directMatch }) {
            return directMatch
        }

        return roomIcons.firstOrNull { icon ->
            tokenizeIconLookupText(icon.description).any { token ->
                token.length >= 4 && normalized.contains(token)
            }
        }?.id
    }
}

private fun normalizeIconLookupText(text: String): String = text
    .lowercase()
    .replace('ё', 'е')
    .replace('/', ' ')
    .replace('-', ' ')
    .replace(Regex("\\s+"), " ")
    .trim()

private fun tokenizeIconLookupText(text: String): List<String> = normalizeIconLookupText(text)
    .split(' ', '.', ',', ':', ';', '(', ')')
    .map { it.trim() }
    .filter { it.isNotBlank() }

private val ROOM_ICON_MATCHERS: List<Pair<Int, List<String>>> = listOf(
    203 to listOf("переговор", "meeting", "conference"),
    206 to listOf("сануз", "туалет", "wc", "уборн"),
    208 to listOf("кух", "kitchen"),
    209 to listOf("спальн", "bedroom"),
    210 to listOf("детск"),
    211 to listOf("ванн", "bathroom"),
    212 to listOf("гардероб"),
    213 to listOf("прихож"),
    214 to listOf("балкон", "лодж"),
    215 to listOf("террас"),
    216 to listOf("кладов"),
    217 to listOf("сервер"),
    218 to listOf("приемн", "ресепш", "reception"),
    219 to listOf("архив"),
    220 to listOf("отдых", "лаунж", "lounge"),
    221 to listOf("раздев"),
    222 to listOf("душев", "душ"),
    223 to listOf("котель"),
    224 to listOf("щит", "электро"),
    225 to listOf("мастерск", "workshop"),
    226 to listOf("торгов"),
    227 to listOf("примероч"),
    228 to listOf("ресторан"),
    229 to listOf("аудитор", "класс", "classroom"),
    230 to listOf("палат"),
    231 to listOf("лестниц"),
    232 to listOf("лифт"),
    233 to listOf("бассейн"),
    234 to listOf("саун", "парн"),
    235 to listOf("спортзал", "тренаж", "gym", "fit"),
    201 to listOf("кабинет", "офис"),
    202 to listOf("холл", "лобби", "lobby"),
    204 to listOf("столов"),
    205 to listOf("корид"),
    207 to listOf("гостин")
)
