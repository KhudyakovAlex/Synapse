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
import com.awada.synapse.pages.PageIconSelect
import com.awada.synapse.pages.PageLocation
import com.awada.synapse.pages.PagePassword
import com.awada.synapse.pages.PageSettings
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
    Password,
    IconSelect
}

@Composable
private fun MainContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Location) }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    
    // Icon selection state
    var iconSelectCategory by remember { mutableStateOf("") }
    var iconSelectCurrentId by remember { mutableStateOf(0) }
    var iconSelectCallback by remember { mutableStateOf<((Int) -> Unit)?>(null) }
    
    // Icon IDs - lifted from PageLocation to preserve across navigation
    var controllerIconId by remember { mutableStateOf(100) }
    var locationIconId by remember { mutableStateOf(200) }
    var luminaireIconId by remember { mutableStateOf(300) }

    // Handle system back button
    BackHandler {
        when (currentScreen) {
            AppScreen.Settings, AppScreen.Password, AppScreen.IconSelect -> {
                // If on Settings, Password or IconSelect, go back to Location
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
                if (targetState == AppScreen.Settings || targetState == AppScreen.Password || targetState == AppScreen.IconSelect) {
                    // Slide in from right when going to settings, password or icon select
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
                        onPasswordClick = { currentScreen = AppScreen.Password },
                        controllerIconId = controllerIconId,
                        locationIconId = locationIconId,
                        luminaireIconId = luminaireIconId,
                        onControllerIconChange = { controllerIconId = it },
                        onLocationIconChange = { locationIconId = it },
                        onLuminaireIconChange = { luminaireIconId = it },
                        onIconSelectClick = { category, currentIconId, onIconSelected ->
                            iconSelectCategory = category
                            iconSelectCurrentId = currentIconId
                            iconSelectCallback = onIconSelected
                            currentScreen = AppScreen.IconSelect
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.Settings -> {
                    PageSettings(
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
                AppScreen.IconSelect -> {
                    PageIconSelect(
                        category = iconSelectCategory,
                        currentIconId = iconSelectCurrentId,
                        onIconSelected = { selectedIconId ->
                            iconSelectCallback?.invoke(selectedIconId)
                            currentScreen = AppScreen.Location
                        },
                        onBackClick = { currentScreen = AppScreen.Location },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
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
