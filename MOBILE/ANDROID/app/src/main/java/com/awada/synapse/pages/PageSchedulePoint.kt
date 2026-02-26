package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.NumericKeyboard
import com.awada.synapse.components.NumericKeyboardLeftButton
import com.awada.synapse.components.PinButton
import com.awada.synapse.components.PinButtonState
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.ScenarioBlock
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.Switch
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.WeekdayButton
import com.awada.synapse.ui.theme.BodyMedium
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.HeadlineMedium
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun PageSchedulePoint(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showScenario by remember { mutableStateOf(false) }
    var showSmoothnessHelp by remember { mutableStateOf(false) }

    if (showScenario) {
        PageScenario(
            onBackClick = { showScenario = false },
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Пункт расписания",
            onBackClick = onBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_16),
            ) {
                var timeDigits by remember { mutableStateOf("1000") } // HHmm
                var activeIndex by remember { mutableIntStateOf(-1) } // -1 (none) or 0..3
                var showKeyboard by remember { mutableStateOf(false) }
                var smoothnessEnabled by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val questionIconResId = remember {
                    context.resources.getIdentifier("system_question", "drawable", context.packageName)
                }

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
                                    // Toggle off: remove focus and hide keyboard
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

            val weekdayLabels = remember { listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
            val weekdaysSelected = remember { mutableStateListOf(false, false, false, false, false, false, false) }
            val weekdaySpacing = PixsoDimens.Numeric_20 / 6 // (328 - 7*44) / 6

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PixsoDimens.Numeric_44),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Плавность",
                        style = HeadlineExtraSmall,
                        color = PixsoColors.Color_Text_text_1_level,
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = -PixsoDimens.Numeric_4)
                            .height(PixsoDimens.Numeric_44)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showSmoothnessHelp = true },
                            )
                            .padding(horizontal = PixsoDimens.Numeric_12),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (questionIconResId != 0) {
                            Icon(
                                painter = painterResource(id = questionIconResId),
                                contentDescription = "Плавность",
                                tint = PixsoColors.Color_Text_text_3_level,
                                modifier = Modifier.size(PixsoDimens.Numeric_24),
                            )
                        } else {
                            Text(
                                text = "?",
                                style = BodyMedium,
                                color = PixsoColors.Color_Text_text_3_level,
                            )
                        }
                    }
                }

                Switch(
                    isChecked = smoothnessEnabled,
                    onCheckedChange = { smoothnessEnabled = it },
                )
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

            Text(
                text = "Сценарий",
                style = HeadlineExtraSmall,
                color = PixsoColors.Color_Text_text_1_level,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val scenarioBlockClick: () -> Unit = { showScenario = true }
            ScenarioBlock(
                scenarios = listOf(
                    ScheduleScenario(text = "Кухня – Вкл", onClick = scenarioBlockClick),
                    ScheduleScenario(text = "Моя любимая спаленка - темп. света 4500K", onClick = scenarioBlockClick),
                ),
                modifier = Modifier.fillMaxWidth(),
                onClick = scenarioBlockClick,
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = { showScenario = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

        if (showSmoothnessHelp) {
            Tooltip(
                text = "Плавность: плавное изменение параметров при срабатывании сценария.",
                primaryButtonText = "Понятно",
                onResult = { showSmoothnessHelp = false },
            )
        }
    }
}

