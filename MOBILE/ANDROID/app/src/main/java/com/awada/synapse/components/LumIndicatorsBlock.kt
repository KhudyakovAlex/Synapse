package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.DisplayLarge
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Unified indicators block for a luminaire page.
 * Currently renders a single Pixso "Luminaires_info" indicator.
 */
@Composable
fun LumIndicatorsBlock(
    brightnessPercent: Int,
    modifier: Modifier = Modifier
) {
    LumInfoIndicator(
        valueText = brightnessPercent.coerceIn(0, 100).toString(),
        labelText = "Яркость",
        progress = brightnessPercent.coerceIn(0, 100) / 100f,
        modifier = modifier
    )
}

/**
 * Pixso component: "Luminaires_info" (200×200).
 * Circle with progress ring, centered value/label, and status dot at the bottom.
 */
@Composable
fun LumInfoIndicator(
    valueText: String,
    labelText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    statusDotColor: Color = Color(0xFF81E83A)
) {
    // Tokens and geometry are intentionally copied from `Lum.kt`,
    // but scaled up to a larger `size`.
    val ringBg = PixsoColors.Color_Border_border_shade_8
    val ringFg = Color(0xFFFF9A37) // same as in `Lum.kt`

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PixsoColors.Color_Bg_bg_surface)
    ) {
        // Arcs (same start/sweep logic as `Lum.kt`)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 8.dp.toPx()
            val inset = strokePx / 2f

            // Keep Pixso-like proportions (104×104 basis from `Lum.kt`)
            val baseArcBox = this.size.minDimension * (80.64049530029297f / 104f)
            val baseArcTopLeft = Offset(
                x = this.size.minDimension * (11.6796875f / 104f),
                y = this.size.minDimension * (11.740234375f / 104f)
            )
            // Increase arc diameter by +4.dp, keeping the same center.
            val arcBox = baseArcBox + 4.dp.toPx()
            val arcCenter = baseArcTopLeft + Offset(baseArcBox / 2f, baseArcBox / 2f)
            val arcTopLeft = arcCenter - Offset(arcBox / 2f, arcBox / 2f)

            val arcStartAngle = 110f
            val arcSweepTotal = 320f

            // Background arc
            drawArc(
                color = ringBg,
                startAngle = arcStartAngle,
                sweepAngle = arcSweepTotal,
                useCenter = false,
                topLeft = Offset(arcTopLeft.x + inset, arcTopLeft.y + inset),
                size = androidx.compose.ui.geometry.Size(arcBox - strokePx, arcBox - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Foreground "brightness sector": 0..100 -> 0..320 degrees
            val p = progress.coerceIn(0f, 1f)
            val arcSweepValue = arcSweepTotal * p
            if (arcSweepValue > 0f) {
                drawArc(
                    color = ringFg,
                    startAngle = arcStartAngle,
                    sweepAngle = arcSweepValue,
                    useCenter = false,
                    topLeft = Offset(arcTopLeft.x + inset, arcTopLeft.y + inset),
                    size = androidx.compose.ui.geometry.Size(arcBox - strokePx, arcBox - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center text stack (replaces icon in `Lum.kt`)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(min = 75.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valueText,
                style = DisplayLarge,
                color = PixsoColors.Color_Secondary_Secondary_30, // #313A3C
                modifier = Modifier.offset(y = 2.dp)
            )
            Text(
                text = labelText,
                style = LabelMedium,
                color = PixsoColors.Color_Text_text_4_level,
                modifier = Modifier.offset(y = (-2).dp)
            )
        }

        // Small status dot (same ratios as `Lum.kt`, 104×104 basis)
        val dotSize = size * (16f / 104f)
        val dotLeft = size * (44f / 104f)
        val dotTop = size * (83.64825439453125f / 104f)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = dotLeft, y = dotTop)
                .size(dotSize)
                .clip(CircleShape)
                .background(statusDotColor)
        )
    }
}

