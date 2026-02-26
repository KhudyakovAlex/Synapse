package com.awada.synapse.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.ai.AI
import com.awada.synapse.lumcontrol.LumControlLayer
import com.awada.synapse.pages.LocalBottomOverlayInset
import com.awada.synapse.pages.PageLum
import com.awada.synapse.pages.PageLumSettings
import com.awada.synapse.pages.PageLocation
import com.awada.synapse.pages.PageLocationSettings
import com.awada.synapse.pages.PageLocations
import com.awada.synapse.pages.PageButtonPanel
import com.awada.synapse.pages.PagePassword
import com.awada.synapse.pages.PageButtonPanelSettings
import com.awada.synapse.pages.PageRoom
import com.awada.synapse.pages.PageRoomSettings
import com.awada.synapse.pages.PageSearch
import com.awada.synapse.pages.PageSensorBrightSettings
import com.awada.synapse.pages.PageSensorPressSettings
import com.awada.synapse.pages.PageSettings
import com.awada.synapse.data.IconCatalogManager
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.SynapseTheme
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ControllerEntity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(PixsoColors.Color_Bg_bg_elevated.toArgb())
        )
        setContent {
            SynapseTheme {
                MainContent()
            }
        }
    }
}

enum class AppScreen {
    Location,
    LocationDetails,
    RoomDetails,
    RoomSettings,
    LocationSettings,
    Lum,
    Search,
    Settings,
    LumSettings,
    SensorPressSettings,
    SensorBrightSettings,
    ButtonPanelSettings,
    Panel,
    Password
}

@Composable
private fun MainContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Location) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var lumBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var systemSettingsBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var settingsLumBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var buttonPanelBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var selectedLocation by remember {
        mutableStateOf<com.awada.synapse.components.LocationItem?>(null)
    }
    var selectedRoom by remember {
        mutableStateOf<RoomState?>(null)
    }
    // Show LumControlLayer on Lum + LocationDetails + RoomDetails (collapsed by default except Lum).
    val isLumControlVisible by remember {
        derivedStateOf {
            currentScreen == AppScreen.Lum ||
                currentScreen == AppScreen.LocationDetails ||
                currentScreen == AppScreen.RoomDetails
        }
    }

    // For vertical centering between AppBar (top) and LumControlLayer (bottom)
    var rootHeightPx by remember { mutableFloatStateOf(0f) }
    var lumPanelTopPx by remember { mutableFloatStateOf(Float.NaN) }
    var aiPanelTopPx by remember { mutableFloatStateOf(Float.NaN) }
    val lumBottomInsetDp by remember {
        derivedStateOf {
            if (!lumPanelTopPx.isFinite() || rootHeightPx <= 0f) return@derivedStateOf 0.dp
            with(density) { (rootHeightPx - lumPanelTopPx).coerceAtLeast(0f).toDp() }
        }
    }
    var aiBottomInsetDp by remember { mutableStateOf(0.dp) }

    // Handle system back button
    BackHandler {
        when (currentScreen) {
            AppScreen.LumSettings -> currentScreen = settingsLumBackTarget
            AppScreen.SensorPressSettings, AppScreen.SensorBrightSettings, AppScreen.ButtonPanelSettings -> currentScreen = systemSettingsBackTarget
            AppScreen.Panel -> currentScreen = buttonPanelBackTarget
            AppScreen.Lum, AppScreen.Search, AppScreen.Settings, AppScreen.Password -> {
                currentScreen = if (currentScreen == AppScreen.Lum) lumBackTarget else AppScreen.Location
            }
            AppScreen.LocationSettings -> {
                currentScreen = AppScreen.LocationDetails
            }
            AppScreen.RoomDetails -> {
                currentScreen = AppScreen.LocationDetails
            }
            AppScreen.RoomSettings -> {
                currentScreen = AppScreen.RoomDetails
            }
            AppScreen.LocationDetails -> {
                currentScreen = AppScreen.Location
            }
            AppScreen.Location -> {
                // If on Location, handle exit with double press
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    (context as? ComponentActivity)?.finish()
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootHeightPx = coords.size.height.toFloat()
            }
    ) {
        // Cache the smallest observed AI inset (collapsed panel). When AI expands, we
        // don't want pages to "jump" their layout based on the temporary expanded top.
        val aiInsetCandidateDp by remember {
            derivedStateOf {
                if (!aiPanelTopPx.isFinite() || rootHeightPx <= 0f) return@derivedStateOf 0.dp
                with(density) { (rootHeightPx - aiPanelTopPx).coerceAtLeast(0f).toDp() }
            }
        }
        LaunchedEffect(aiInsetCandidateDp, aiBottomInsetDp) {
            if (aiInsetCandidateDp > 0.dp && (aiBottomInsetDp == 0.dp || aiInsetCandidateDp < aiBottomInsetDp)) {
                aiBottomInsetDp = aiInsetCandidateDp
            }
        }

        // Navigation between pages with animation.
        //
        // Important: AnimatedContent keeps both initial+target screens composed during the transition.
        // If we compute bottom inset from `currentScreen` outside of AnimatedContent, the outgoing screen
        // may "jump" its layout when overlays (e.g. LumControlLayer) become visible for the target screen.
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState != AppScreen.Location) {
                    // Slide in from right when going away from locations
                    (slideInHorizontally { it } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it / 3 } + fadeOut()
                    )
                } else {
                    // Slide in from left when going back to locations
                    (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(
                        slideOutHorizontally { it } + fadeOut()
                    )
                }.using(SizeTransform(clip = false))
            },
            label = "ScreenNavigation"
        ) { screen ->
            val isLumControlVisibleForScreen = when (screen) {
                AppScreen.Lum, AppScreen.LocationDetails, AppScreen.RoomDetails -> true
                else -> false
            }
            val bottomOverlayInsetForScreen = maxOf(
                aiBottomInsetDp,
                if (isLumControlVisibleForScreen) lumBottomInsetDp else 0.dp
            )

            CompositionLocalProvider(LocalBottomOverlayInset provides bottomOverlayInsetForScreen) {
                when (screen) {
                    AppScreen.Location -> {
                        PageLocations(
                            onSettingsClick = { currentScreen = AppScreen.Settings },
                            onLocationClick = { item ->
                                selectedLocation = item
                                currentScreen = AppScreen.LocationDetails
                            },
                            onFindControllerClick = { currentScreen = AppScreen.Search },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LocationDetails -> {
                        val loc = selectedLocation
                            ?: com.awada.synapse.components.LocationItem(
                                title = "Локация",
                                iconResId = R.drawable.controller_100_default
                            )
                        PageLocation(
                            location = loc,
                            onBackClick = { currentScreen = AppScreen.Location },
                            onSettingsClick = { currentScreen = AppScreen.LocationSettings },
                            onRoomClick = { roomTitle, roomIconResId ->
                                val iconId = iconIdFromDrawableResId(context, roomIconResId)
                                    ?: iconIdFromDrawableResId(context, R.drawable.location_208_kuhnya)
                                    ?: 208
                                selectedRoom = RoomState(title = roomTitle, iconId = iconId)
                                currentScreen = AppScreen.RoomDetails
                            },
                            onLumClick = {
                                lumBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.Lum
                            },
                            onSensorPressSettingsClick = {
                                systemSettingsBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.SensorPressSettings
                            },
                            onSensorBrightSettingsClick = {
                                systemSettingsBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.SensorBrightSettings
                            },
                            onButtonPanelSettingsClick = {
                                buttonPanelBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.Panel
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.RoomDetails -> {
                        val room = selectedRoom ?: RoomState(
                            title = "Помещение",
                            iconId = iconIdFromDrawableResId(context, R.drawable.location_208_kuhnya) ?: 208
                        )
                        PageRoom(
                            roomTitle = room.title,
                            onBackClick = { currentScreen = AppScreen.LocationDetails },
                            onSettingsClick = { currentScreen = AppScreen.RoomSettings },
                            onLumClick = {
                                lumBackTarget = AppScreen.RoomDetails
                                currentScreen = AppScreen.Lum
                            },
                            onSensorPressSettingsClick = {
                                systemSettingsBackTarget = AppScreen.RoomDetails
                                currentScreen = AppScreen.SensorPressSettings
                            },
                            onSensorBrightSettingsClick = {
                                systemSettingsBackTarget = AppScreen.RoomDetails
                                currentScreen = AppScreen.SensorBrightSettings
                            },
                            onButtonPanelSettingsClick = {
                                buttonPanelBackTarget = AppScreen.RoomDetails
                                currentScreen = AppScreen.Panel
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.RoomSettings -> {
                        val room = selectedRoom ?: RoomState(
                            title = "Помещение",
                            iconId = iconIdFromDrawableResId(context, R.drawable.location_208_kuhnya) ?: 208
                        )
                        PageRoomSettings(
                            initialName = room.title,
                            initialIconId = room.iconId,
                            onBackClick = { currentScreen = AppScreen.RoomDetails },
                            onSaved = { name, iconId ->
                                selectedRoom = room.copy(title = name, iconId = iconId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LocationSettings -> {
                        PageLocationSettings(
                            controllerId = selectedLocation?.controllerId,
                            onBackClick = { currentScreen = AppScreen.LocationDetails },
                            onSaved = { newName, newIconId ->
                                selectedLocation = selectedLocation?.copy(
                                    title = newName,
                                    iconResId = com.awada.synapse.components.iconResId(context, newIconId)
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Lum -> {
                        PageLum(
                            onBackClick = { currentScreen = lumBackTarget },
                            onSettingsClick = {
                                settingsLumBackTarget = AppScreen.Lum
                                currentScreen = AppScreen.LumSettings
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Search -> {
                        PageSearch(
                            onBackClick = { currentScreen = AppScreen.Location },
                            onAutoNavigateNext = { currentScreen = AppScreen.Password },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Settings -> {
                        PageSettings(
                            onBackClick = { currentScreen = AppScreen.Location },
                            onFindControllerClick = { currentScreen = AppScreen.Search },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LumSettings -> {
                        PageLumSettings(
                            onBackClick = { currentScreen = settingsLumBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SensorPressSettings -> {
                        PageSensorPressSettings(
                            onBackClick = { currentScreen = systemSettingsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SensorBrightSettings -> {
                        PageSensorBrightSettings(
                            onBackClick = { currentScreen = systemSettingsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.ButtonPanelSettings -> {
                        PageButtonPanelSettings(
                            onBackClick = { currentScreen = systemSettingsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Panel -> {
                        PageButtonPanel(
                            onBackClick = { currentScreen = buttonPanelBackTarget },
                            onSettingsClick = {
                                systemSettingsBackTarget = AppScreen.Panel
                                currentScreen = AppScreen.ButtonPanelSettings
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    AppScreen.Password -> {
                        PagePassword(
                            correctPassword = "1234", // TODO: Get from settings
                            onPasswordCorrect = {
                                scope.launch {
                                    val dao = db.controllerDao()
                                    val suffix = (System.currentTimeMillis() % 100_000_000)
                                        .toString()
                                        .padStart(8, '0')
                                    val name = "SYN_$suffix"
                                    val nextPos = (dao.getMaxGridPos() ?: -1) + 1
                                    dao.insert(
                                        ControllerEntity(
                                            name = name,
                                            password = "1234",
                                            icoNum = 100,
                                            gridPos = nextPos
                                        )
                                    )
                                    currentScreen = AppScreen.Location
                                }
                            },
                            onBackClick = { currentScreen = AppScreen.Location },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        // Lighting control layer - between pages and AI
        // Positioned above AI component with proper padding
        LumControlLayer(
            isVisible = isLumControlVisible,
            sliders = listOf("Color", "Saturation", "Temperature", "Brightness"), // TODO: Get from current page/device
            autoExpandOnShow = currentScreen == AppScreen.Lum,
            stateKey = currentScreen,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    lumPanelTopPx = coords.boundsInRoot().top
                }
        )
        
        AI(
            modifier = Modifier.fillMaxSize(),
            onMainPanelTopPxChanged = { aiPanelTopPx = it }
        )
    }
}

private data class RoomState(
    val title: String,
    val iconId: Int
)

private fun iconIdFromDrawableResId(context: Context, drawableResId: Int): Int? {
    val entryName = runCatching { context.resources.getResourceEntryName(drawableResId) }.getOrNull()
        ?: return null
    val catalog = IconCatalogManager.load(context)
    return catalog.icons.firstOrNull { it.resourceName == entryName }?.id
}

@Preview(showBackground = true)
@Composable
private fun MainPreview() {
    SynapseTheme {
        MainContent()
    }
}
