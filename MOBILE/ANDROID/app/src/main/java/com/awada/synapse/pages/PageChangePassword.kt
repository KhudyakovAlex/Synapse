package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.NumericKeyboard
import com.awada.synapse.components.NumericKeyboardLeftButton
import com.awada.synapse.components.PinButton
import com.awada.synapse.components.PinButtonState
import com.awada.synapse.components.Tooltip
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.delay

/**
 * Password change page.
 *
 * @param onBackClick Callback for back button
 */
@Composable
fun PageChangePassword(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var activeField by remember { mutableIntStateOf(0) } // 0 = current, 1 = new, 2 = confirm
    var pinError by remember { mutableStateOf(false) }
    var showHelpTooltip by remember { mutableStateOf(false) }

    // Get active pin based on field
    val activePin = when (activeField) {
        0 -> currentPin
        1 -> newPin
        2 -> confirmPin
        else -> currentPin
    }

    // Check password when 4 digits entered
    LaunchedEffect(activePin) {
        if (activePin.length == 4) {
            delay(100) // Small delay for visual feedback
            when (activeField) {
                0 -> {
                    // Check current password
                    if (currentPin != "1234") {
                        pinError = true
                        delay(1000)
                        pinError = false
                        currentPin = ""
                    } else {
                        // Correct password - move to next field
                        activeField = 1
                    }
                }
                1 -> {
                    // New password entered - move to next field
                    activeField = 2
                }
                2 -> {
                    // Confirm password entered - check match
                    if (newPin == confirmPin) {
                        // Passwords match - close page
                        onBackClick()
                    } else {
                        // Passwords don't match - show error
                        pinError = true
                        delay(1000)
                        pinError = false
                        confirmPin = ""
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Сменить пароль",
            onBackClick = onBackClick,
            isScrollable = false,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current password
                    Text(
                        text = "Текущий пароль",
                        style = HeadlineExtraSmall,
                        color = PixsoColors.Color_Text_text_1_level,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(4) { index ->
                            val state = when {
                                pinError && activeField == 0 -> PinButtonState.Error
                                index < currentPin.length -> PinButtonState.Input
                                index == currentPin.length && activeField == 0 -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < currentPin.length) currentPin[index].toString() else "",
                                onClick = { activeField = 0 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    // Numeric keyboard - shown when on field 0
                    if (activeField == 0) {
                        NumericKeyboard(
                            onDigitClick = { digit ->
                                if (currentPin.length < 4) {
                                    currentPin += digit
                                    pinError = false
                                }
                            },
                            leftButtonMode = NumericKeyboardLeftButton.Help,
                            onHelpClick = {
                                showHelpTooltip = true
                            },
                            onBackspaceClick = {
                                if (currentPin.isNotEmpty()) {
                                    currentPin = currentPin.dropLast(1)
                                    pinError = false
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(38.dp))
                    }

                    // New password
                    Text(
                        text = "Новый пароль",
                        style = HeadlineExtraSmall,
                        color = PixsoColors.Color_Text_text_1_level,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(4) { index ->
                            val state = when {
                                pinError && activeField == 1 -> PinButtonState.Error
                                index < newPin.length -> PinButtonState.Input
                                index == newPin.length && activeField == 1 -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < newPin.length) newPin[index].toString() else "",
                                onClick = { activeField = 1 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    // Numeric keyboard - shown when on field 1
                    if (activeField == 1) {
                        NumericKeyboard(
                            onDigitClick = { digit ->
                                if (newPin.length < 4) {
                                    newPin += digit
                                    pinError = false
                                }
                            },
                            leftButtonMode = NumericKeyboardLeftButton.None,
                            onBackspaceClick = {
                                if (newPin.isNotEmpty()) {
                                    newPin = newPin.dropLast(1)
                                    pinError = false
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(38.dp))
                    }

                    // Confirm password
                    Text(
                        text = "Подтвердить пароль",
                        style = HeadlineExtraSmall,
                        color = PixsoColors.Color_Text_text_1_level,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(4) { index ->
                            val state = when {
                                pinError && activeField == 2 -> PinButtonState.Error
                                index < confirmPin.length -> PinButtonState.Input
                                index == confirmPin.length && activeField == 2 -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < confirmPin.length) confirmPin[index].toString() else "",
                                onClick = { activeField = 2 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    // Numeric keyboard - shown when on field 2
                    if (activeField == 2) {
                        NumericKeyboard(
                            onDigitClick = { digit ->
                                if (confirmPin.length < 4) {
                                    confirmPin += digit
                                    pinError = false
                                }
                            },
                            leftButtonMode = NumericKeyboardLeftButton.None,
                            onBackspaceClick = {
                                if (confirmPin.isNotEmpty()) {
                                    confirmPin = confirmPin.dropLast(1)
                                    pinError = false
                                }
                            }
                        )
                    }
                }
            }
        }

        // Help tooltip
        if (showHelpTooltip) {
            Tooltip(
                text = "Если пароль не менялся, он указан на корпусе устройства",
                primaryButtonText = "Понятно",
                onResult = {
                    showHelpTooltip = false
                }
            )
        }
    }
}
