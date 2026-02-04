package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.KeyboardButton
import com.awada.synapse.components.KeyboardButtonStyle
import com.awada.synapse.components.PinButton
import com.awada.synapse.components.PinButtonState
import com.awada.synapse.components.Tooltip
import com.awada.synapse.ui.theme.HeadlineMedium
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Password entry page with PIN code input.
 * 4-digit PIN code with numeric keyboard.
 * 
 * @param correctPassword The correct 4-digit password to verify against
 * @param onPasswordCorrect Callback when password is entered correctly
 * @param onBackClick Callback for back button
 */
@Composable
fun PagePassword(
    correctPassword: String,
    onPasswordCorrect: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinCode by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showHelpTooltip by remember { mutableStateOf(false) }
    
    // Check password when 4 digits entered
    LaunchedEffect(pinCode) {
        if (pinCode.length == 4) {
            if (pinCode == correctPassword) {
                // Correct password
                onPasswordCorrect()
            } else {
                // Wrong password - show error and clear after 1 second
                pinError = true
                delay(1000)
                pinError = false
                pinCode = ""
            }
        }
    }

    PageContainer(
        title = "Пароль",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // AIMain panel height
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Headline
            Text(
                text = "Введите пароль",
                style = HeadlineMedium,
                color = PixsoColors.Color_Text_text_1_level,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN indicators (4 buttons)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(4) { index ->
                    val state = when {
                        pinError -> PinButtonState.Error
                        index < pinCode.length -> PinButtonState.Input
                        else -> PinButtonState.Default
                    }
                    PinButton(
                        state = state,
                        text = if (index < pinCode.length) pinCode[index].toString() else "",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric keyboard (3x4 grid)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: 1, 2, 3
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "1",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "1"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "2",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "2"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "3",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "3"
                                pinError = false
                            }
                        }
                    )
                }

                // Row 2: 4, 5, 6
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "4",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "4"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "5",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "5"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "6",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "6"
                                pinError = false
                            }
                        }
                    )
                }

                // Row 3: 7, 8, 9
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "7",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "7"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "8",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "8"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "9",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "9"
                                pinError = false
                            }
                        }
                    )
                }

                // Row 4: Help, 0, Backspace
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Help,
                        text = "Не могу войти",
                        onClick = {
                            showHelpTooltip = true
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Default,
                        text = "0",
                        onClick = {
                            if (pinCode.length < 4) {
                                pinCode += "0"
                                pinError = false
                            }
                        }
                    )
                    KeyboardButton(
                        style = KeyboardButtonStyle.Icon,
                        icon = R.drawable.ic_backspace,
                        onClick = {
                            if (pinCode.isNotEmpty()) {
                                pinCode = pinCode.dropLast(1)
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
