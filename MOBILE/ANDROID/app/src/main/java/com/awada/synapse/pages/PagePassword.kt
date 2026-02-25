package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.NumericKeyboard
import com.awada.synapse.components.NumericKeyboardLeftButton
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

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
        title = "Пароль",
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
                // Headline
            Text(
                text = "Введите пароль",
                style = HeadlineMedium,
                color = PixsoColors.Color_Text_text_1_level,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PIN indicators (4 buttons)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(4) { index ->
                    val state = when {
                        pinError -> PinButtonState.Error
                        index < pinCode.length -> PinButtonState.Input
                        index == pinCode.length -> PinButtonState.Active
                        else -> PinButtonState.Default
                    }
                    PinButton(
                        state = state,
                        text = if (index < pinCode.length) pinCode[index].toString() else "",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Numeric keyboard
            NumericKeyboard(
                onDigitClick = { digit ->
                    if (pinCode.length < 4) {
                        pinCode += digit
                        pinError = false
                    }
                },
                leftButtonMode = NumericKeyboardLeftButton.Help,
                onHelpClick = {
                    showHelpTooltip = true
                },
                onBackspaceClick = {
                    if (pinCode.isNotEmpty()) {
                        pinCode = pinCode.dropLast(1)
                        pinError = false
                    }
                }
            )
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
