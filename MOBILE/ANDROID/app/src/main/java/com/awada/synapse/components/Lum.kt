package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Lum tile (placeholder).
 *
 * Intended usage: placed in icon-like matrices/grids across the UI.
 * Current version provides only a stable public API and basic layout:
 * - circular icon area
 * - label text below
 *
 * The visual design and dynamic lamp icon drawing will be added later.
 */
@Composable
fun Lum(
    title: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER")
    isActive: Boolean = false,
    brightnessPercent: Int = 0,
    iconResId: Int = R.drawable.luminaire_300_default,
    statusDotColor: Color = PixsoColors.Color_Bg_bg_surface,
    onClick: (() -> Unit)? = null,
    iconContent: @Composable BoxScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    interactionSource.collectIsPressedAsState() // keep interaction tracking for future states

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .widthIn(min = iconSize)
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pixso updated: big circle (Oval 43) is 104×104 and starts at (0,0) in the component.
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(PixsoColors.Color_Bg_bg_surface),
            contentAlignment = Alignment.Center
        ) {
            // Arcs (Pixso: Oval 41/45). Pixso strokeWeight now 4px.
            Canvas(modifier = Modifier.size(iconSize)) {
                val strokePx = 4.dp.toPx()
                val inset = strokePx / 2f

                // Pixso: arc group is positioned at (11.6797, 11.7402) inside 104×104 circle.
                // Use ratios to keep it correct for any iconSize.
                val baseArcBox = size.minDimension * (80.64049530029297f / 104f)
                val baseArcTopLeft = Offset(
                    x = size.minDimension * (11.6796875f / 104f),
                    y = size.minDimension * (11.740234375f / 104f)
                )
                // Increase arc diameter by +4.dp, keeping the same center.
                val arcBox = baseArcBox + 4.dp.toPx()
                val arcCenter = baseArcTopLeft + Offset(baseArcBox / 2f, baseArcBox / 2f)
                val arcTopLeft = arcCenter - Offset(arcBox / 2f, arcBox / 2f)

                val arcStartAngle = 110f
                val arcSweepTotal = 320f

                drawArc(
                    color = PixsoColors.Color_Border_border_shade_8,
                    startAngle = arcStartAngle,
                    sweepAngle = arcSweepTotal,
                    useCenter = false,
                    topLeft = Offset(arcTopLeft.x + inset, arcTopLeft.y + inset),
                    size = Size(arcBox - strokePx, arcBox - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                val p = (brightnessPercent.coerceIn(0, 100) / 100f)
                val arcSweepValue = arcSweepTotal * p
                if (arcSweepValue > 0f) {
                    drawArc(
                        color = Color(0xFFFF9A37),
                        startAngle = arcStartAngle,
                        sweepAngle = arcSweepValue,
                        useCenter = false,
                        topLeft = Offset(arcTopLeft.x + inset, arcTopLeft.y + inset),
                        size = Size(arcBox - strokePx, arcBox - strokePx),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Small status dot (Pixso: Oval 44): 16×16, left=44, top=83.6483 inside 104×104.
            run {
                val dotSize = iconSize * (16f / 104f)
                val dotLeft = iconSize * (44f / 104f)
                val dotTop = iconSize * (83.64825439453125f / 104f)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = dotLeft, y = dotTop)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
            }

            // Icon with guaranteed 2.dp "air" to arcs
            val iconDp = 48.dp * 0.8f
            val iconPad = 2.dp
            Box(
                modifier = Modifier
                    .size(iconDp + iconPad * 2)
                    .clip(CircleShape)
                    .background(PixsoColors.Color_Bg_bg_surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(iconDp)
                )
            }
            // TODO: draw complex dynamic luminaire icon here
            iconContent()
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = LabelMedium.copy(lineHeight = LabelMedium.lineHeight * 0.8f),
            color = PixsoColors.Color_Text_text_1_level,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

