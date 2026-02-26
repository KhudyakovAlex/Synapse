package com.awada.synapse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.R

/**
 * Mode for the left bottom button of the numeric keyboard.
 */
enum class NumericKeyboardLeftButton {
    /** "Не могу войти" button - triggers onHelpClick */
    Help,

    /** "Закрыть" button - triggers onCloseClick */
    Close,

    /** No left button */
    None
}

/**
 * Numeric keyboard with digits 0-9, optional left button and backspace.
 * Layout: 3x4 grid (1-2-3, 4-5-6, 7-8-9, [Left Button]-0-Backspace)
 *
 * @param onDigitClick Callback when a digit button is clicked, receives the digit as String
 * @param onBackspaceClick Callback when the backspace button is clicked
 * @param leftButtonMode Mode for the left bottom button (Help, Close, or None)
 * @param onHelpClick Callback when the help button is clicked (only if leftButtonMode = Help)
 * @param onCloseClick Callback when the close button is clicked (only if leftButtonMode = Close)
 * @param modifier Optional modifier for the keyboard container
 */
@Composable
fun NumericKeyboard(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    leftButtonMode: NumericKeyboardLeftButton = NumericKeyboardLeftButton.Help,
    onHelpClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Row 1: 1, 2, 3
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "1",
                onClick = { onDigitClick("1") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "2",
                onClick = { onDigitClick("2") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "3",
                onClick = { onDigitClick("3") }
            )
        }

        // Row 2: 4, 5, 6
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "4",
                onClick = { onDigitClick("4") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "5",
                onClick = { onDigitClick("5") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "6",
                onClick = { onDigitClick("6") }
            )
        }

        // Row 3: 7, 8, 9
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "7",
                onClick = { onDigitClick("7") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "8",
                onClick = { onDigitClick("8") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "9",
                onClick = { onDigitClick("9") }
            )
        }

        // Row 4: [Left Button], 0, Backspace
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            when (leftButtonMode) {
                NumericKeyboardLeftButton.Help -> {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Help,
                        text = "Не могу войти",
                        onClick = onHelpClick
                    )
                }
                NumericKeyboardLeftButton.Close -> {
                    KeyboardButton(
                        style = KeyboardButtonStyle.Help,
                        text = "Закрыть",
                        onClick = onCloseClick
                    )
                }
                NumericKeyboardLeftButton.None -> {
                    // Reserve a full cell so "0" and backspace don't shift left
                    Spacer(modifier = Modifier.size(width = 88.dp, height = 68.dp))
                }
            }
            KeyboardButton(
                style = KeyboardButtonStyle.Default,
                text = "0",
                onClick = { onDigitClick("0") }
            )
            KeyboardButton(
                style = KeyboardButtonStyle.Icon,
                icon = R.drawable.ic_backspace,
                onClick = onBackspaceClick
            )
        }
    }
}
