package com.awada.synapse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

@Composable
private fun MainContent() {
    Box(modifier = Modifier.fillMaxSize()) {
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
