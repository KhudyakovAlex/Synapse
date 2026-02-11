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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.awada.synapse.ai.AI
import com.awada.synapse.lumcontrol.LumControlLayer
import com.awada.synapse.pages.PageLocation
import com.awada.synapse.pages.PagePassword
import com.awada.synapse.pages.PageSettings
import com.awada.synapse.pages.PageSettingsLum
import com.awada.synapse.pages.PageSettingsSensorPress
import com.awada.synapse.pages.PageSettingsSensorBright
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.SynapseTheme

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
    Settings,
    SettingsLum,
    SettingsSensorPress,
    SettingsSensorBright,
    Password
}

@Composable
private fun MainContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Location) }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var isLumControlVisible by remember { mutableStateOf(true) }

    // Handle system back button
    BackHandler {
        when (currentScreen) {
            AppScreen.Settings, AppScreen.SettingsLum, AppScreen.SettingsSensorPress, 
            AppScreen.SettingsSensorBright, AppScreen.Password -> {
                // If on any settings page or Password, go back to Location
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Navigation between pages with animation
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.Settings || targetState == AppScreen.SettingsLum || 
                    targetState == AppScreen.SettingsSensorPress || targetState == AppScreen.SettingsSensorBright ||
                    targetState == AppScreen.Password) {
                    // Slide in from right when going to settings or password
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
                    PageLocation(
                        onSettingsClick = { currentScreen = AppScreen.Settings },
                        onSettingsLumClick = { currentScreen = AppScreen.SettingsLum },
                        onSettingsSensorPressClick = { currentScreen = AppScreen.SettingsSensorPress },
                        onSettingsSensorBrightClick = { currentScreen = AppScreen.SettingsSensorBright },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.Settings -> {
                    PageSettings(
                        onBackClick = { currentScreen = AppScreen.Location },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.SettingsLum -> {
                    PageSettingsLum(
                        onBackClick = { currentScreen = AppScreen.Location },
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
                AppScreen.Password -> {
                    PagePassword(
                        correctPassword = "1234", // TODO: Get from settings
                        onPasswordCorrect = {
                            currentScreen = AppScreen.Location
                            Toast.makeText(context, "Пароль верный!", Toast.LENGTH_SHORT).show()
                        },
                        onBackClick = { currentScreen = AppScreen.Location },
                        modifier = Modifier.fillMaxSize()
                    )
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
