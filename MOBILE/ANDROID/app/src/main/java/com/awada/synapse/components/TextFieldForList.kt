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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
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
    var fieldWidth by remember { mutableStateOf(0) }
    var fieldCenterY by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val view = LocalView.current
    val screenHeight = view.rootView.height
    
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
                        fieldWidth = coordinates.size.width
                        val windowPos = IntArray(2)
                        view.getLocationOnScreen(windowPos)
                        fieldCenterY = coordinates.positionInRoot().y.toInt() + 
                            windowPos[1] + coordinates.size.height / 2
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
            
            // Dropdown popup — anchored to the text field Box
        if (isDropdownExpanded && dropdownItems.isNotEmpty()) {
            val gapPx = with(density) { PixsoDimens.Numeric_4.roundToPx() }
            val showBelow = fieldCenterY < screenHeight / 2
            
            val positionProvider = remember(showBelow, gapPx) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val x = anchorBounds.left
                        val y = if (showBelow) {
                            anchorBounds.bottom + gapPx
                        } else {
                            anchorBounds.top - popupContentSize.height - gapPx
                        }
                        return IntOffset(x, y)
                    }
                }
            }
            
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { isDropdownExpanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .width(with(density) { fieldWidth.toDp() })
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
        } // end if dropdown
            } // end text field Box
        } // end Column
    } // end outer Box
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
