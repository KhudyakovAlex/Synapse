package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.awada.synapse.ui.theme.BodyMedium
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Dropdown menu component with list items.
 * 
 * Tokens:
 * - Container background: Color_Bg_bg_surface (white)
 * - Container border: Color_Border_border_shade_8 (1dp)
 * - Container radius: Radius_Radius_S (16dp)
 * - Container padding: Numeric_8 (8dp vertical)
 * - Item height: Numeric_56 (56dp)
 * - Item padding: Numeric_20 (20dp horizontal)
 * - Item background Default: Color_Bg_bg_surface
 * - Item background Pressed: Color_State_secondary_pressed
 * - Text style: BodyMedium (14sp/20sp) with lineHeight override to 24sp
 * - Text color: Color_Text_text_1_level
 */
@Composable
fun DropdownMenu(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_S))
            .background(PixsoColors.Color_Bg_bg_surface)
            .border(
                width = PixsoDimens.Stroke_S,
                color = PixsoColors.Color_Border_border_shade_8,
                shape = RoundedCornerShape(PixsoDimens.Radius_Radius_S)
            )
            .padding(vertical = PixsoDimens.Numeric_8)
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun DropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PixsoDimens.Numeric_56)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                if (isPressed) PixsoColors.Color_State_secondary_pressed
                else PixsoColors.Color_Bg_bg_surface
            )
            .padding(horizontal = PixsoDimens.Numeric_20),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = BodyMedium.copy(
                lineHeight = PixsoDimens.Body_Body_L_Line_Height
            ),
            color = PixsoColors.Color_Text_text_1_level
        )
    }
}
