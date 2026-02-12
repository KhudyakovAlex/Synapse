package com.awada.synapse.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SearchSwampIndicator(
    dotSize: Dp = 24.dp,
    dotSpacing: Dp = 20.dp
) {
    val scales = remember { List(9) { Animatable(1f) } }
    val rnd = remember { Random(System.currentTimeMillis().toInt()) }
    val baseColor = PixsoColors.Color_Bg_bg_elevated
    val activeColor = PixsoColors.Color_State_primary

    // Bigger x2 (defaults), faster x1.5, and controlled overlap:
    // at most 2 dots animate simultaneously (one shrinking, one expanding).
    LaunchedEffect(Unit) {
        // Slower by 20%
        val expandMs = 272
        val shrinkBaseMs = 544
        val shrinkDownMs = (shrinkBaseMs * 0.78f).toInt().coerceAtLeast(1)
        val shrinkBackMs = ((shrinkBaseMs - shrinkDownMs).coerceAtLeast(1)) * 4 // slower bounce-back x4
        val shrinkTotalMs = shrinkDownMs + shrinkBackMs
        var last = -1

        fun pickNextIndex(): Int {
            repeat(24) {
                val candidate = rnd.nextInt(9)
                if (candidate != last && !scales[candidate].isRunning) return candidate
            }
            for (i in 0 until 9) {
                if (i != last && !scales[i].isRunning) return i
            }
            return (last + 1).floorMod(9)
        }

        // Start first pulse immediately.
        last = pickNextIndex()
        launch { pulse(scales[last], expandMs = expandMs, shrinkDownMs = shrinkDownMs, shrinkBackMs = shrinkBackMs) }

        while (true) {
            // Start next pulse so that previous finishes exactly when next ends EXPAND:
            // delay == shrink duration of previous pulse.
            delay(shrinkTotalMs.toLong())

            val idx = pickNextIndex()
            last = idx
            launch { pulse(scales[idx], expandMs = expandMs, shrinkDownMs = shrinkDownMs, shrinkBackMs = shrinkBackMs) }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(dotSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(dotSpacing)) {
                repeat(3) { c ->
                    val i = r * 3 + c
                    val s = scales[i].value
                    // Color animates with scale: small -> base, big -> active
                    val t = ((s - 1f) / 0.65f).coerceIn(0f, 1f) // 1f..1.65f
                    val dotColor = lerp(baseColor, activeColor, t)
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .graphicsLayer {
                                scaleX = s
                                scaleY = s
                            }
                            .background(
                                color = dotColor,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

private suspend fun pulse(
    a: Animatable<Float, AnimationVector1D>,
    expandMs: Int,
    shrinkDownMs: Int,
    shrinkBackMs: Int
) {
    if (a.isRunning) return
    a.animateTo(
        targetValue = 1.65f,
        animationSpec = tween(
            durationMillis = expandMs,
            easing = CubicBezierEasing(0.15f, 0f, 0f, 1f)
        )
    )
    // "Inertia" on shrink: undershoot a bit, then return to 1f.
    val undershoot = 0.78f
    a.animateTo(
        targetValue = undershoot,
        animationSpec = tween(
            durationMillis = shrinkDownMs,
            easing = CubicBezierEasing(0.35f, 0f, 0.2f, 1f)
        )
    )
    a.animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = shrinkBackMs,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        )
    )
}

private fun Int.floorMod(mod: Int): Int {
    val r = this % mod
    return if (r < 0) r + mod else r
}

