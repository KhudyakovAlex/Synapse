package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = BodyLarge.copy(
                    color = if (enabled) PixsoColors.Color_Text_text_1_level 
                           else PixsoColors.Color_State_on_disabled
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_20)
                    .align(Alignment.CenterStart),
                decorationBox = { innerTextField ->
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = BodyLarge,
                            color = PixsoColors.Color_Text_text_4_level
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
