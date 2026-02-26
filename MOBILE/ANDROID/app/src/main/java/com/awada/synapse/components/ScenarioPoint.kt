package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import com.awada.synapse.R
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import androidx.compose.ui.res.painterResource

@Immutable
data class ScenarioPointField(
    val value: Int?,
    val onValueChange: (Int) -> Unit,
    val placeholder: String = "",
    val enabled: Boolean = true,
    val dropdownItems: List<DropdownItem> = emptyList(),
)

@Composable
fun ScenarioPoint(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    whereField: ScenarioPointField,
    whatField: ScenarioPointField,
    valueField: ScenarioPointField,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(shape)
            .background(PixsoColors.Color_Bg_bg_surface)
            .border(
                width = PixsoDimens.Stroke_S,
                color = PixsoColors.Color_Border_border_primary,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onExpandedChange(!expanded) },
            )
            .padding(PixsoDimens.Numeric_12),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PixsoDimens.Numeric_8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = title,
                style = HeadlineExtraSmall,
                color = PixsoColors.Color_State_on_secondary,
            )

            Box(
                modifier = Modifier.size(PixsoDimens.Numeric_32),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = PixsoColors.Color_Text_text_3_level,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_20))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_20),
            ) {
                TextFieldForList(
                    value = whereField.value,
                    onValueChange = whereField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Где меняем",
                    placeholder = whereField.placeholder,
                    enabled = whereField.enabled,
                    dropdownItems = whereField.dropdownItems,
                )

                TextFieldForList(
                    value = whatField.value,
                    onValueChange = whatField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Что меняем",
                    placeholder = whatField.placeholder,
                    enabled = whatField.enabled,
                    dropdownItems = whatField.dropdownItems,
                )

                TextFieldForList(
                    value = valueField.value,
                    onValueChange = valueField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Значение",
                    placeholder = valueField.placeholder,
                    enabled = valueField.enabled,
                    dropdownItems = valueField.dropdownItems,
                )
            }
        }
    }
}

