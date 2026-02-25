package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.PinButton
import com.awada.synapse.components.PinButtonState
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Password change page.
 * TODO: Implement password change functionality
 *
 * @param onBackClick Callback for back button
 */
@Composable
fun PageChangePassword(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    var currentPin by remember { mutableStateOf("") }
                    var newPin by remember { mutableStateOf("") }
                    var confirmPin by remember { mutableStateOf("") }

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
                                index < currentPin.length -> PinButtonState.Input
                                index == currentPin.length -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < currentPin.length) currentPin[index].toString() else "",
                                onClick = { }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

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
                                index < newPin.length -> PinButtonState.Input
                                index == newPin.length -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < newPin.length) newPin[index].toString() else "",
                                onClick = { }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

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
                                index < confirmPin.length -> PinButtonState.Input
                                index == confirmPin.length -> PinButtonState.Active
                                else -> PinButtonState.Default
                            }
                            PinButton(
                                state = state,
                                text = if (index < confirmPin.length) confirmPin[index].toString() else "",
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
