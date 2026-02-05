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
}
