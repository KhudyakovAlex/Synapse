package com.awada.synapse

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.awada.synapse.ui.theme.SynapseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen activity.
 * Shows animated splash for 2 seconds, then launches MainActivity.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make navigation bar transparent
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        setContent {
            SynapseTheme {
                SplashScreen()
            }
        }
        
        // Launch MainActivity after 2 seconds
        lifecycleScope.launch {
            delay(2000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}

@Composable
private fun SplashScreen() {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0.5f) }
    
    LaunchedEffect(Unit) {
        // Scale and fade animation: 80% → 110% → 100%, alpha: 0.5 → 1.0
        launch {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(
                    durationMillis = 667,
                    easing = FastOutSlowInEasing
                )
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 533,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Animated logo
        Image(
            painter = painterResource(id = R.drawable.ic_logo_splash),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        )
    }
}
