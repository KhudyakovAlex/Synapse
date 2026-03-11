package com.awada.synapse.pages

import com.awada.synapse.db.ActionEntity
import com.awada.synapse.db.GroupEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.RoomEntity

object ScheduleEventFormatter {
    private val weekdayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    fun sanitizeTimeDigits(raw: String?): String {
        val digitsOnly = raw.orEmpty().filter { it.isDigit() }.take(4)
        return digitsOnly.padEnd(4, '0')
    }

    fun formatTime(raw: String?): String {
        val digits = sanitizeTimeDigits(raw)
        return "${digits.substring(0, 2)}:${digits.substring(2, 4)}"
    }

    fun parseDaysMask(mask: String?): List<Boolean> {
        val normalized = mask.orEmpty().uppercase().padEnd(7, 'F').take(7)
        return normalized.map { it == 'T' }
    }

    fun buildDaysMask(selected: List<Boolean>): String {
        return buildString {
            repeat(7) { index ->
                append(if (selected.getOrNull(index) == true) 'T' else 'F')
            }
        }
    }

    fun buildDayLabels(mask: String?): List<String> {
        val selected = parseDaysMask(mask)
        return when {
            selected.all { it } -> listOf("Ежедневно")
            selected.any { it } -> selected.mapIndexedNotNull { index, enabled ->
                weekdayLabels.getOrNull(index)?.takeIf { enabled }
            }
            else -> listOf("Не выбрано")
        }
    }

    fun buildScenarioActionTitle(
        action: ActionEntity,
        rooms: List<RoomEntity>,
        groups: List<GroupEntity>,
        luminaires: List<LuminaireEntity>,
    ): String {
        val roomNames = rooms.associate { it.id to it.name.ifBlank { "Помещение ${it.id + 1}" } }
        val objectTitle = when (action.objectTypeId) {
            1 -> "Вся локация"
            2 -> {
                val roomId = action.objectId?.toInt()
                rooms.firstOrNull { it.id == roomId }?.name?.ifBlank { "Помещение ${(roomId ?: 0) + 1}" }
            }
            3 -> {
                val groupId = action.objectId?.toInt()
                groups.firstOrNull { it.id == groupId }?.name?.ifBlank { "Группа ${(groupId ?: 0) + 1}" }
            }
            4 -> {
                luminaires.firstOrNull { it.id == action.objectId }?.let { luminaire ->
                    val roomTitle = roomNames[luminaire.roomId] ?: "Без помещения"
                    val luminaireTitle = luminaire.name.ifBlank { "Светильник ${luminaire.id}" }
                    "$roomTitle / $luminaireTitle"
                }
            }
            else -> null
        }
        val changeValueTitle = when (action.changeTypeId) {
            1 -> when (action.changeValueId) {
                0 -> "Сцена Выкл"
                1 -> "Сцена 1"
                2 -> "Сцена 2"
                3 -> "Сцена 3"
                4 -> "Сцена Вкл"
                else -> null
            }
            2 -> when (action.changeValueId) {
                0 -> "Выключить АВТО"
                1 -> "Включить АВТО"
                else -> null
            }
            else -> null
        }

        return if (!objectTitle.isNullOrBlank() && !changeValueTitle.isNullOrBlank()) {
            "$objectTitle - $changeValueTitle"
        } else {
            "Действие"
        }
    }
}
