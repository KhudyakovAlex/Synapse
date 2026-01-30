package com.awada.synapse

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
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
    Settings
}

@Composable
private fun MainContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Location) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Navigation between pages with animation
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.Settings) {
                    // Slide in from right when going to settings
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
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.Settings -> {
                    PageSettings(
                        onBackClick = { currentScreen = AppScreen.Location },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        UIAI(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun MainPreview() {
    SynapseTheme {
        MainContent()
    }
}
