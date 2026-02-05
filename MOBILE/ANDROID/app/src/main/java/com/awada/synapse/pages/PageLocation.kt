package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.data.IconCatalogManager

/**
 * Initial page for Locations.
 * Always placed below AI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onPasswordClick: () -> Unit,
    controllerIconId: Int,
    locationIconId: Int,
    luminaireIconId: Int,
    onControllerIconChange: (Int) -> Unit,
    onLocationIconChange: (Int) -> Unit,
    onLuminaireIconChange: (Int) -> Unit,
    onIconSelectClick: (category: String, currentIconId: Int, onIconSelected: (Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTooltipOneButton by remember { mutableStateOf(false) }
    var showTooltipTwoButtons by remember { mutableStateOf(false) }
    var lastTooltipResult by remember { mutableStateOf<TooltipResult?>(null) }
    
    val context = LocalContext.current
    val catalog = remember { IconCatalogManager.load(context) }
    
    // Get drawable resource IDs for current icons - recalculate on ID change
    val controllerDrawableId by derivedStateOf {
        val iconInfo = catalog.findById(controllerIconId)
        if (iconInfo != null) {
            val resId = context.resources.getIdentifier(
                iconInfo.resourceName,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else R.drawable.controller_100_default
        } else {
            R.drawable.controller_100_default
        }
    }
    
    val locationDrawableId by derivedStateOf {
        val iconInfo = catalog.findById(locationIconId)
        if (iconInfo != null) {
            val resId = context.resources.getIdentifier(
                iconInfo.resourceName,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else R.drawable.location_200_default
        } else {
            R.drawable.location_200_default
        }
    }
    
    val luminaireDrawableId by derivedStateOf {
        val iconInfo = catalog.findById(luminaireIconId)
        if (iconInfo != null) {
            val resId = context.resources.getIdentifier(
                iconInfo.resourceName,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else R.drawable.luminaire_300_default
        } else {
            R.drawable.luminaire_300_default
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onPasswordClick) {
                    Text("Ввести пароль")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showTooltipOneButton = true }) {
                    Text("Показать Tooltip (1 кнопка)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showTooltipTwoButtons = true }) {
                    Text("Показать Tooltip (2 кнопки)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconSelectButton(
                        icon = controllerDrawableId,
                        onClick = {
                            onIconSelectClick("controller", controllerIconId, onControllerIconChange)
                        }
                    )

                    IconSelectButton(
                        icon = locationDrawableId,
                        onClick = {
                            onIconSelectClick("location", locationIconId, onLocationIconChange)
                        }
                    )

                    IconSelectButton(
                        icon = luminaireDrawableId,
                        onClick = {
                            onIconSelectClick("luminaire", luminaireIconId, onLuminaireIconChange)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                lastTooltipResult?.let {
                    Text("Последний результат: $it")
                }
            }
        }

        // Tooltip with one button
        if (showTooltipOneButton) {
            Tooltip(
                text = "Это пример tooltip окна с одной кнопкой. AI панель внизу остаётся активной!",
                primaryButtonText = "Понятно",
                onResult = { result ->
                    lastTooltipResult = result
                    showTooltipOneButton = false
                }
            )
        }

        // Tooltip with two buttons
        if (showTooltipTwoButtons) {
            Tooltip(
                text = "Это пример с двумя кнопками. Вы можете подтвердить или отменить действие.",
                primaryButtonText = "Хорошо",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    lastTooltipResult = result
                    showTooltipTwoButtons = false
                }
            )
        }
    }
}
