package com.awada.synapse.lumcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * LumControl Layer - lighting control component
 * Panel positioned above AI layer in MainActivity
 * 
 * Features:
 * - Collapsible/expandable with drag handle on top
 * - Scene buttons (always visible when collapsed)
 * - Sliders (visible when expanded)
 * - Animated appearance from bottom
 * - Bottom padding to stay above AI component
 */
@Composable
fun LumControlLayer(
    isVisible: Boolean = true,
    sliders: List<String> = emptyList(),
    bottomPadding: Int = 178, // Default: AIMain height (100dp) + drag handle (48dp) + spacing (30dp)
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding.dp)
        ) {
            // Main panel container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp), // Space for drag handle (half of 48dp height)
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp)
                    .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_L))
                    .background(
                        color = PixsoColors.Color_State_tertiary_variant,
                        shape = RoundedCornerShape(PixsoDimens.Radius_Radius_L)
                    )
                    .padding(12.dp)
            ) {
                // Sliders section (visible only when expanded)
                if (isExpanded && sliders.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sliders.forEach { sliderType ->
                            when (sliderType) {
                                "Color" -> ColorSlider(
                                    value = 0f,
                                    onValueChange = { /* TODO */ }
                                )
                                "Saturation" -> SaturationSlider(
                                    value = 50f,
                                    onValueChange = { /* TODO */ },
                                    dynamicColor = androidx.compose.ui.graphics.Color(0xFFFF1A1A) // TODO: Get from current color
                                )
                                "Temperature" -> TemperatureSlider(
                                    value = 4000f,
                                    onValueChange = { /* TODO */ }
                                )
                                "Brightness" -> BrightnessSlider(
                                    value = 50f,
                                    onValueChange = { /* TODO */ }
                                )
                            }
                        }
                    }
                }
                
                // Quick buttons (always visible)
                QuickButtonsRow(
                    buttons = defaultQuickButtons,
                    onButtonSelected = { button ->
                        // TODO: Handle button selection
                    }
                )
            }
            }
            
            // Drag handle on top - overlaps panel
            DragHandle(
                isExpanded = isExpanded,
                onExpandToggle = { isExpanded = !isExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Drag handle for expanding/collapsing the layer
 * Positioned on top of the main panel
 */
@Composable
private fun DragHandle(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(isExpanded) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        // Swipe up = expand (negative offset)
                        // Swipe down = collapse (positive offset)
                        if (dragOffset < -30f && !isExpanded) {
                            onExpandToggle()
                        } else if (dragOffset > 30f && isExpanded) {
                            onExpandToggle()
                        }
                        dragOffset = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        // Drag handle indicator (horizontal line) - positioned 12dp from top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_Full))
                .background(PixsoColors.Color_State_tertiary_variant)
        )
    }
}

