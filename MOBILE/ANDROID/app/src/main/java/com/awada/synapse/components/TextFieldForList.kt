package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.BodyMedium
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Data class for dropdown items with ID and text.
 */
@Immutable
data class DropdownItem(
    val id: Int,
    val text: String
)

@Composable
fun TextFieldForList(
    value: Int?,
    onValueChange: (Int) -> Unit,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    enabled: Boolean = true,
    dropdownItems: List<DropdownItem> = emptyList()
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var fieldPositionY by remember { mutableStateOf(0f) }
    var fieldPositionX by remember { mutableStateOf(0f) }
    var fieldWidth by remember { mutableStateOf(0) }
    var fieldHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    // Find text for current value
    val displayText = remember(value, dropdownItems) {
        dropdownItems.find { it.id == value }?.text ?: ""
    }
    
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8)
        ) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = LabelLarge.copy(
                    color = if (enabled) PixsoColors.Color_Text_text_3_level 
                           else PixsoColors.Color_State_on_disabled
                ),
                modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12)
            )
        }
        
            // Text field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PixsoDimens.Numeric_56)
                    .onGloballyPositioned { coordinates ->
                        fieldPositionY = coordinates.positionInRoot().y
                        fieldPositionX = coordinates.positionInRoot().x
                        fieldWidth = coordinates.size.width
                        fieldHeight = coordinates.size.height
                    }
                    .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_S))
                    .background(
                        if (enabled) PixsoColors.Color_Bg_bg_surface 
                        else PixsoColors.Color_State_disabled
                    )
                    .border(
                        width = PixsoDimens.Stroke_S,
                        color = if (enabled) PixsoColors.Color_Border_border_shade_8
                               else PixsoColors.Color_Border_border_shade_4,
                        shape = RoundedCornerShape(PixsoDimens.Radius_Radius_S)
                    )
                    .clickable(enabled = enabled && dropdownItems.isNotEmpty()) {
                        isDropdownExpanded = !isDropdownExpanded
                    }
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = PixsoDimens.Numeric_20, end = PixsoDimens.Numeric_12),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    // Text display (read-only for dropdown)
                    Text(
                        text = displayText.ifEmpty { placeholder },
                        style = BodyLarge,
                        color = if (displayText.isEmpty()) {
                            PixsoColors.Color_Text_text_4_level
                        } else if (enabled) {
                            PixsoColors.Color_Text_text_1_level
                        } else {
                            PixsoColors.Color_State_on_disabled
                        },
                        modifier = Modifier.weight(1f)
                    )
                
                    // Icon button
                    Box(
                        modifier = Modifier.size(PixsoDimens.Numeric_32),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = if (enabled) PixsoColors.Color_State_tertiary 
                                  else PixsoColors.Color_State_on_disabled
                        )
                    }
                }
            }
        }
        
        // Dropdown popup
        if (isDropdownExpanded && dropdownItems.isNotEmpty()) {
            Popup(
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                // Background scrim to catch clicks outside
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1001f)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            isDropdownExpanded = false
                        }
                ) {
                    val offsetPx = with(density) { PixsoDimens.Numeric_4.toPx() }
                    
                    // Determine if field is above or below middle of screen
                    val fieldCenterY = fieldPositionY + (fieldHeight / 2)
                    val showBelow = fieldCenterY < screenHeight / 2
                    
                    // Calculate available space and position
                    val (dropdownY, maxHeight) = if (showBelow) {
                        // Show below field
                        val availableSpace = screenHeight - fieldPositionY - fieldHeight - offsetPx
                        val yPos = fieldPositionY + fieldHeight + offsetPx
                        Pair(yPos, with(density) { availableSpace.toDp() })
                    } else {
                        // Show above field
                        val availableSpace = fieldPositionY - offsetPx
                        val yPos = fieldPositionY - offsetPx - availableSpace
                        Pair(yPos, with(density) { availableSpace.toDp() })
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(with(density) { fieldWidth.toDp() })
                            .offset {
                                IntOffset(
                                    x = fieldPositionX.toInt(),
                                    y = dropdownY.toInt()
                                )
                            }
                            .heightIn(max = maxHeight)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { /* Prevent click propagation */ }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_S))
                                .background(PixsoColors.Color_Bg_bg_surface)
                                .border(
                                    width = PixsoDimens.Stroke_S,
                                    color = PixsoColors.Color_Border_border_shade_8,
                                    shape = RoundedCornerShape(PixsoDimens.Radius_Radius_S)
                                )
                                .padding(vertical = PixsoDimens.Numeric_8)
                                .verticalScroll(rememberScrollState())
                        ) {
                            dropdownItems.forEach { item ->
                                DropdownMenuItem(
                                    text = item.text,
                                    onClick = {
                                        onValueChange(item.id)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
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
