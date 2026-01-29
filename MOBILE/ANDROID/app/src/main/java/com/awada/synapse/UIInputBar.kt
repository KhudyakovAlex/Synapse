package com.awada.synapse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

private val INPUT_BAR_HEIGHT = 56.dp
private val SEND_BUTTON_SIZE = 48.dp
private val SEND_ICON_SIZE = 32.dp

@Composable
fun UIInputBar(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onSendClick: () -> Unit = {}
) {
    val hasText = value.isNotEmpty()
    val inputShape = RoundedCornerShape(PixsoDimens.Radius_Radius_Full)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(INPUT_BAR_HEIGHT)
            .background(PixsoColors.Color_Bg_bg_surface, inputShape)
            .border(
                width = 1.dp,
                color = PixsoColors.Color_Border_border_shade_8,
                shape = inputShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Placeholder
                if (value.isEmpty()) {
                    Text(
                        text = "Сообщение...",
                        style = BodyLarge,
                        color = PixsoColors.Color_Text_text_4_level
                    )
                }
                
                // Text field
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = BodyLarge.merge(
                        TextStyle(color = PixsoColors.Color_Text_text_1_level)
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(PixsoColors.Color_Text_text_1_level)
                )
            }
            
            // Send button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            val buttonColor = when {
                !hasText -> PixsoColors.Color_State_disabled
                isPressed -> PixsoColors.Color_State_primary_pressed
                else -> PixsoColors.Color_State_primary
            }
            
            Box(
                modifier = Modifier
                    .size(SEND_BUTTON_SIZE)
                    .background(
                        color = buttonColor,
                        shape = RoundedCornerShape(PixsoDimens.Radius_Radius_Full)
                    )
                    .clickable(
                        enabled = hasText,
                        interactionSource = interactionSource,
                        indication = null
                    ) { onSendClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "Send",
                    tint = if (hasText) {
                        PixsoColors.Color_State_on_primary
                    } else {
                        PixsoColors.Color_State_on_disabled
                    },
                    modifier = Modifier.size(SEND_ICON_SIZE)
                )
            }
        }
    }
}
