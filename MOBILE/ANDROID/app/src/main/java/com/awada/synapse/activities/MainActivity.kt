package com.awada.synapse.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.awada.synapse.R
import com.awada.synapse.ai.AI
import com.awada.synapse.lumcontrol.LumControlLayer
import com.awada.synapse.pages.LocalBottomOverlayInset
import com.awada.synapse.pages.PageLum
import com.awada.synapse.pages.PageLocation
import com.awada.synapse.pages.PageLocations
import com.awada.synapse.pages.PagePassword
import com.awada.synapse.pages.PageSearch
import com.awada.synapse.pages.PageSettings
import com.awada.synapse.pages.PageSettingsButtonPanel
import com.awada.synapse.pages.PageSettingsLum
import com.awada.synapse.pages.PageSettingsSensorPress
import com.awada.synapse.pages.PageSettingsSensorBright
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.SynapseTheme
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ControllerEntity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynapseTheme {
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as ComponentActivity).window
                    window.navigationBarColor = PixsoColors.Color_Bg_bg_elevated.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }
                MainContent()
            }
        }
    }
}

enum class AppScreen {
    Location,
    LocationDetails,
    Lum,
    Search,
    Settings,
    SettingsLum,
    SettingsSensorPress,
    SettingsSensorBright,
    SettingsButtonPanel,
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
    var settingsLumBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var selectedLocation by remember {
        mutableStateOf<com.awada.synapse.components.LocationItem?>(null)
    }
    // Hardcoded: show LumControlLayer only on Lum page
    val isLumControlVisible by remember {
        derivedStateOf { currentScreen == AppScreen.Lum }
    }

    // For vertical centering between AppBar (top) and LumControlLayer (bottom)
    var rootHeightPx by remember { mutableFloatStateOf(0f) }
    var lumPanelTopPx by remember { mutableFloatStateOf(Float.NaN) }
    val lumBottomInsetDp by remember {
        derivedStateOf {
            if (!lumPanelTopPx.isFinite() || rootHeightPx <= 0f) return@derivedStateOf 0.dp
            with(density) { (rootHeightPx - lumPanelTopPx).coerceAtLeast(0f).toDp() }
        }
    }

    // Handle system back button
    BackHandler {
        when (currentScreen) {
            AppScreen.Lum, AppScreen.Search, AppScreen.Settings, AppScreen.SettingsLum, AppScreen.SettingsSensorPress,
            AppScreen.SettingsSensorBright, AppScreen.SettingsButtonPanel, AppScreen.Password -> {
                // If on any settings page or Password, go back to Location
                currentScreen = AppScreen.Location
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
        CompositionLocalProvider(
            LocalBottomOverlayInset provides (if (isLumControlVisible) lumBottomInsetDp else 0.dp)
        ) {
            // Navigation between pages with animation
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
                            onSettingsClick = { currentScreen = AppScreen.Settings },
                            onSearchClick = { currentScreen = AppScreen.Search },
                            onLumClick = { currentScreen = AppScreen.Lum },
                            onSensorPressSettingsClick = { currentScreen = AppScreen.SettingsSensorPress },
                            onSensorBrightSettingsClick = { currentScreen = AppScreen.SettingsSensorBright },
                            onButtonPanelSettingsClick = { currentScreen = AppScreen.SettingsButtonPanel },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Lum -> {
                        PageLum(
                            onBackClick = { currentScreen = AppScreen.Location },
                            onSettingsClick = {
                                settingsLumBackTarget = AppScreen.Lum
                                currentScreen = AppScreen.SettingsLum
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
                            locations = listOf(
                                com.awada.synapse.components.LocationItem(
                                    title = "Новое здание",
                                    iconResId = R.drawable.controller_102_dom
                                )
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SettingsLum -> {
                        PageSettingsLum(
                            onBackClick = { currentScreen = settingsLumBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SettingsSensorPress -> {
                        PageSettingsSensorPress(
                            onBackClick = { currentScreen = AppScreen.Location },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SettingsSensorBright -> {
                        PageSettingsSensorBright(
                            onBackClick = { currentScreen = AppScreen.Location },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SettingsButtonPanel -> {
                        PageSettingsButtonPanel(
                            onBackClick = { currentScreen = AppScreen.Location },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Password -> {
                        PagePassword(
                            correctPassword = "1234", // TODO: Get from settings
                            onPasswordCorrect = {
                                scope.launch {
                                    val dao = db.controllerDao()
                                    val name = "SYNAPSE12345678"
                                    val existing = dao.getByName(name)
                                    if (existing == null) {
                                        dao.insert(
                                            ControllerEntity(
                                                name = name,
                                                password = "1234",
                                                icoNum = 100
                                            )
                                        )
                                    }
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
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    lumPanelTopPx = coords.boundsInRoot().top
                }
        )
        
        AI(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun MainPreview() {
    SynapseTheme {
        MainContent()
    }
}
