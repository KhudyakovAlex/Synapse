package com.awada.synapse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.R

/**
 * Numeric keyboard with digits 0-9, help button and backspace.
 * Layout: 3x4 grid (1-2-3, 4-5-6, 7-8-9, Help-0-Backspace)
 *
 * @param onDigitClick Callback when a digit button is clicked, receives the digit as String
 * @param onHelpClick Callback when the help button is clicked
 * @param onBackspaceClick Callback when the backspace button is clicked
 * @param modifier Optional modifier for the keyboard container
 */
@Composable
fun NumericKeyboard(
    onDigitClick: (String) -> Unit,
    onHelpClick: () -> Unit,
    onBackspaceClick: () -> Unit,
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

        // Row 4: Help, 0, Backspace
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            KeyboardButton(
                style = KeyboardButtonStyle.Help,
                text = "Не могу войти",
                onClick = onHelpClick
            )
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
