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
import androidx.compose.ui.graphics.Color
import com.awada.synapse.R
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import androidx.compose.ui.res.painterResource

@Immutable
data class ScenarioPointField(
    val value: Long?,
    val onValueChange: (Long) -> Unit,
    val placeholder: String = "",
    val enabled: Boolean = true,
    val dropdownItems: List<DropdownItem> = emptyList(),
)

@Composable
fun ScenarioPoint(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    objectTypeField: ScenarioPointField,
    objectField: ScenarioPointField?,
    changeTypeField: ScenarioPointField,
    changeValueField: ScenarioPointField,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = PixsoColors.Color_Bg_bg_surface,
    borderColor: Color = PixsoColors.Color_Border_border_primary,
    headerColor: Color = PixsoColors.Color_State_on_secondary,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = PixsoDimens.Stroke_S,
                color = borderColor,
                shape = shape,
            )
            .padding(PixsoDimens.Numeric_12),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PixsoDimens.Numeric_8)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onExpandedChange(!expanded) },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = BodyLarge,
                color = headerColor,
            )

            Box(
                modifier = Modifier.size(PixsoDimens.Numeric_32),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = headerColor,
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
                    value = objectTypeField.value,
                    onValueChange = objectTypeField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Тип объекта",
                    placeholder = objectTypeField.placeholder,
                    enabled = objectTypeField.enabled,
                    dropdownItems = objectTypeField.dropdownItems,
                )

                objectField?.let { field ->
                    TextFieldForList(
                        value = field.value,
                        onValueChange = field.onValueChange,
                        icon = R.drawable.ic_chevron_down,
                        label = "Объект",
                        placeholder = field.placeholder,
                        enabled = field.enabled,
                        dropdownItems = field.dropdownItems,
                    )
                }

                TextFieldForList(
                    value = changeTypeField.value,
                    onValueChange = changeTypeField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Изменение",
                    placeholder = changeTypeField.placeholder,
                    enabled = changeTypeField.enabled,
                    dropdownItems = changeTypeField.dropdownItems,
                )

                TextFieldForList(
                    value = changeValueField.value,
                    onValueChange = changeValueField.onValueChange,
                    icon = R.drawable.ic_chevron_down,
                    label = "Значение изменения",
                    placeholder = changeValueField.placeholder,
                    enabled = changeValueField.enabled,
                    dropdownItems = changeValueField.dropdownItems,
                )
            }
        }
    }
}

