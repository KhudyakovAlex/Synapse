package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.NumericKeyboard
import com.awada.synapse.components.NumericKeyboardLeftButton
import com.awada.synapse.components.PinButton
import com.awada.synapse.components.PinButtonState
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.WeekdayButton
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.EventEntity
import com.awada.synapse.db.ScenarioEntity
import com.awada.synapse.ui.theme.ButtonSmall
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.HeadlineMedium
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun PageSchedulePoint(
    controllerId: Int?,
    eventId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var showScenario by remember { mutableStateOf(false) }
    var editingScenarioId by remember { mutableStateOf<Long?>(null) }
    var loadedEventId by remember { mutableStateOf<Long?>(null) }
    var timeDigits by remember { mutableStateOf("0000") }
    var activeIndex by remember { mutableIntStateOf(-1) }
    var showKeyboard by remember { mutableStateOf(false) }
    var scenarioId by remember { mutableStateOf(EventEntity.NO_SCENARIO_ID) }
    val weekdayLabels = remember { listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
    val weekdaysSelected = remember { mutableStateListOf(false, false, false, false, false, false, false) }
    val rooms by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.roomDao().observeAll(controllerId)
        }
    }.collectAsState(initial = emptyList())
    val groups by remember(db) {
        db.groupDao().observeAll()
    }.collectAsState(initial = emptyList())
    val luminaires by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.luminaireDao().observeAllForController(controllerId)
        }
    }.collectAsState(initial = emptyList())
    val actions by remember(db, scenarioId) {
        if (scenarioId == EventEntity.NO_SCENARIO_ID) {
            flowOf(emptyList())
        } else {
            db.actionDao().observeAllForScenario(scenarioId)
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(eventId) {
        if (eventId == null) {
            loadedEventId = null
            timeDigits = "0000"
            scenarioId = EventEntity.NO_SCENARIO_ID
            weekdaysSelected.indices.forEach { index -> weekdaysSelected[index] = true }
            return@LaunchedEffect
        }
        if (loadedEventId == eventId) return@LaunchedEffect

        val event = db.eventDao().getById(eventId)
        if (event != null) {
            timeDigits = ScheduleEventFormatter.sanitizeTimeDigits(event.time)
            scenarioId = event.scenarioId
            ScheduleEventFormatter.parseDaysMask(event.days).forEachIndexed { index, selected ->
                weekdaysSelected[index] = selected
            }
            loadedEventId = eventId
        }
    }

    if (showScenario) {
        PageScenario(
            scenarioId = editingScenarioId,
            buttonPanelId = null,
            controllerId = controllerId,
            onBackClick = { showScenario = false },
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val handleBackClick: () -> Unit = handleBackClick@{
        val resolvedControllerId = controllerId
        if (resolvedControllerId == null) {
            onBackClick()
            return@handleBackClick
        }

        val resolvedTime = ScheduleEventFormatter.sanitizeTimeDigits(timeDigits)
        val resolvedDays = ScheduleEventFormatter.buildDaysMask(weekdaysSelected)
        val shouldPersist = eventId != null || scenarioId != EventEntity.NO_SCENARIO_ID

        scope.launch {
            if (shouldPersist) {
                val entity = EventEntity(
                    id = eventId ?: 0,
                    controllerId = resolvedControllerId,
                    days = resolvedDays,
                    time = resolvedTime,
                    scenarioId = scenarioId,
                )
                if (eventId == null) {
                    db.eventDao().insert(entity)
                } else {
                    db.eventDao().update(entity)
                }
            }
            onBackClick()
        }
    }
    val weekdaySpacing = PixsoDimens.Numeric_20 / 6
    val scenarioTitles = remember(actions, rooms, groups, luminaires) {
        actions.map { action ->
            ScheduleEventFormatter.buildScenarioActionTitle(
                action = action,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
            )
        }
    }
    val openScenarioEditor: () -> Unit = {
        scope.launch {
            val resolvedScenarioId = if (scenarioId == EventEntity.NO_SCENARIO_ID) {
                db.scenarioDao().insert(ScenarioEntity())
            } else {
                scenarioId
            }
            scenarioId = resolvedScenarioId
            editingScenarioId = resolvedScenarioId
            showScenario = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Пункт расписания",
            onBackClick = handleBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_16),
            ) {
                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            if (index == 2) {
                                Box(
                                    modifier = Modifier.height(PixsoDimens.Numeric_56),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = ":",
                                        style = HeadlineMedium,
                                        color = PixsoColors.Color_Text_text_3_level,
                                    )
                                }
                            }

                            val digit = timeDigits.getOrNull(index)?.toString().orEmpty()
                            val state = when {
                                showKeyboard && index == activeIndex -> PinButtonState.Active
                                digit.isNotEmpty() -> PinButtonState.Input
                                else -> PinButtonState.Default
                            }

                            PinButton(
                                state = state,
                                text = digit,
                                onClick = {
                                    if (showKeyboard && activeIndex == index) {
                                        showKeyboard = false
                                        activeIndex = -1
                                    } else {
                                        activeIndex = index
                                        showKeyboard = true
                                    }
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

                if (showKeyboard) {
                    fun isDigitAllowedAt(index: Int, digit: Int): Boolean {
                        return when (index) {
                            0 -> digit in 0..2
                            1 -> {
                                val h1 = timeDigits[0].digitToInt()
                                if (h1 == 2) digit in 0..3 else digit in 0..9
                            }
                            2 -> digit in 0..5
                            3 -> digit in 0..9
                            else -> false
                        }
                    }

                    NumericKeyboard(
                        onDigitClick = { d ->
                            val digit = d.toIntOrNull() ?: return@NumericKeyboard
                            val idx = activeIndex
                            if (idx !in 0..3) return@NumericKeyboard
                            if (!isDigitAllowedAt(idx, digit)) return@NumericKeyboard

                            val chars = timeDigits.toCharArray()
                            chars[idx] = ('0'.code + digit).toChar()
                            timeDigits = String(chars)
                            activeIndex = (idx + 1) % 4
                        },
                        leftButtonMode = NumericKeyboardLeftButton.Close,
                        onCloseClick = {
                            showKeyboard = false
                            activeIndex = -1
                        },
                        onBackspaceClick = {
                            val idx = activeIndex
                            if (idx !in 0..3) return@NumericKeyboard

                            val prevIndex = if (idx == 0) 3 else idx - 1
                            val chars = timeDigits.toCharArray()
                            chars[prevIndex] = '0'
                            timeDigits = String(chars)
                            activeIndex = prevIndex
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(weekdaySpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        weekdayLabels.forEachIndexed { index, label ->
                            WeekdayButton(
                                text = label,
                                selected = weekdaysSelected[index],
                                onSelectedChange = { weekdaysSelected[index] = it },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

                if (scenarioId != EventEntity.NO_SCENARIO_ID) {
                    Text(
                        text = "Сценарий",
                        style = HeadlineExtraSmall,
                        color = PixsoColors.Color_Text_text_1_level,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ScheduleScenarioSummaryBlock(
                        titles = if (scenarioTitles.isEmpty()) listOf("Действие") else scenarioTitles,
                        onClick = openScenarioEditor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (scenarioId == EventEntity.NO_SCENARIO_ID) {
                    PrimaryIconButtonLarge(
                        text = "Добавить сценарий",
                        onClick = openScenarioEditor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleScenarioSummaryBlock(
    titles: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)

    Column(
        modifier = modifier
            .clip(shape)
            .background(PixsoColors.Color_Bg_bg_surface)
            .border(
                width = PixsoDimens.Stroke_S,
                color = PixsoColors.Color_Border_border_primary,
                shape = shape,
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(PixsoDimens.Numeric_8),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8),
    ) {
        titles.forEach { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(PixsoColors.Color_Bg_bg_surface)
                    .border(
                        width = PixsoDimens.Stroke_S,
                        color = PixsoColors.Color_Border_border_primary,
                        shape = shape,
                    )
                    .padding(
                        horizontal = PixsoDimens.Numeric_12,
                        vertical = PixsoDimens.Numeric_8,
                    ),
            ) {
                Text(
                    text = title,
                    style = ButtonSmall,
                    color = PixsoColors.Color_State_on_secondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

