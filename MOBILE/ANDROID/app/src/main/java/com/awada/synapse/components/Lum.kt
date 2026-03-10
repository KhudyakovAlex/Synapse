package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    typeId: Int? = null,
    hue: Int = 0,
    saturation: Int = 0,
    temperature: Int = 0,
    iconResId: Int = R.drawable.luminaire_300_default,
    statusDotColor: Color = PixsoColors.Color_Bg_bg_surface,
    onClick: (() -> Unit)? = null,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    iconContent: @Composable BoxScope.() -> Unit = {}
) {
    LumInternal(
        title = title,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        brightnessPercent = brightnessPercent,
        typeId = typeId,
        hue = hue,
        saturation = saturation,
        temperature = temperature,
        iconResId = iconResId,
        statusDotColor = statusDotColor,
        forcePressed = false,
        onClick = onClick,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        iconContent = iconContent
    )
}

@Composable
fun Lum(
    title: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER")
    isActive: Boolean = false,
    brightnessPercent: Int = 0,
    typeId: Int? = null,
    hue: Int = 0,
    saturation: Int = 0,
    temperature: Int = 0,
    iconResId: Int = R.drawable.luminaire_300_default,
    statusDotColor: Color = PixsoColors.Color_Bg_bg_surface,
    forcePressed: Boolean,
    onClick: (() -> Unit)? = null,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    iconContent: @Composable BoxScope.() -> Unit = {}
) {
    LumInternal(
        title = title,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        brightnessPercent = brightnessPercent,
        typeId = typeId,
        hue = hue,
        saturation = saturation,
        temperature = temperature,
        iconResId = iconResId,
        statusDotColor = statusDotColor,
        forcePressed = forcePressed,
        onClick = onClick,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        iconContent = iconContent
    )
}

@Composable
private fun LumInternal(
    title: String,
    modifier: Modifier,
    iconSize: Dp,
    enabled: Boolean,
    brightnessPercent: Int,
    typeId: Int?,
    hue: Int,
    saturation: Int,
    temperature: Int,
    iconResId: Int,
    statusDotColor: Color,
    forcePressed: Boolean,
    onClick: (() -> Unit)?,
    onCircleBoundsInRoot: ((Rect) -> Unit)?,
    iconContent: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showPressed by remember { mutableStateOf(false) }

    val instantPressModifier = if (onClick != null && enabled) {
        Modifier.pointerInput(onClick, enabled) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                showPressed = true

                val start = down.position
                var cancelledByMove = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                    if (!change.pressed) break

                    if (!cancelledByMove) {
                        val dist = (change.position - start).getDistance()
                        if (dist > viewConfiguration.touchSlop) {
                            cancelledByMove = true
                            showPressed = false
                        }
                    }
                }

                showPressed = false
            }
        }
    } else {
        Modifier
    }
    val shadowColor = Color.Black.copy(alpha = 1f / 3f)
    val circleBg = if (onClick != null && enabled && showPressed) {
        PixsoColors.Color_State_secondary_pressed
    } else if (forcePressed) {
        PixsoColors.Color_State_primary_pressed
    } else {
        PixsoColors.Color_Bg_bg_surface
    }
    val visualState = remember(typeId, brightnessPercent, hue, saturation, temperature, statusDotColor) {
        if (typeId != null) {
            resolveLumVisualState(
                typeId = typeId,
                brightnessPercent = brightnessPercent,
                hue = hue,
                saturation = saturation,
                temperature = temperature
            )
        } else {
            LumVisualState(
                brightnessPercent = brightnessPercent.coerceIn(0, 100),
                statusDotColor = statusDotColor
            )
        }
    }

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
            .then(instantPressModifier)
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pixso updated: big circle (Oval 43) is 104×104 and starts at (0,0) in the component.
        Box(
            modifier = Modifier
                .size(iconSize)
                .onGloballyPositioned { coords ->
                    onCircleBoundsInRoot?.invoke(coords.boundsInRoot())
                }
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .background(
                    circleBg
                ),
            contentAlignment = Alignment.Center
        ) {
            // Arcs (Pixso: Oval 41/45). Pixso strokeWeight now 4px.
            Canvas(modifier = Modifier.size(iconSize)) {
                val strokePx = 4.dp.toPx()
                val inset = strokePx / 2f

                val baseArcBox = size.minDimension * (80.64049530029297f / 104f)
                // Increase arc radius by +3.dp (=> diameter +6.dp), keeping the same center.
                // Previous value was +4.dp to diameter; now +10.dp total.
                val arcBox = baseArcBox + 10.dp.toPx()
                val arcCenter = Offset(size.width / 2f, size.height / 2f)
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

                val p = visualState.brightnessPercent / 100f
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

            // Icon with guaranteed 2.dp "air" to arcs
            val iconDp = 48.dp * 0.8f
            // Increased mask size to better cover brightness arc behind the icon.
            val iconPad = 8.dp
            Box(
                modifier = Modifier
                    .size(iconDp + iconPad * 2)
                    .clip(CircleShape)
                    .background(circleBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(iconDp),
                    colorFilter = if (forcePressed) {
                        ColorFilter.tint(PixsoColors.Color_State_on_primary)
                    } else {
                        null
                    }
                )
            }
            // TODO: draw complex dynamic luminaire icon here
            iconContent()

            // Small status dot centered on the white circle vertical axis.
            // Must be drawn above the icon "mask" circle.
            visualState.statusDotColor?.let { dotColor ->
                run {
                    val dotSize = iconSize * (16f / 104f)
                    val dotTop = iconSize * (83.64825439453125f / 104f) + 2.dp
                    Box(
                        modifier = Modifier
                            .zIndex(2f)
                            .align(Alignment.TopCenter)
                            .offset(y = dotTop)
                            .size(dotSize)
                            .clip(CircleShape)
                            .border(width = 1.dp, color = PixsoColors.Color_Border_border_shade_8, shape = CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatTileTitle(title),
            style = LabelMedium.copy(lineHeight = LabelMedium.lineHeight * 0.8f),
            color = PixsoColors.Color_Text_text_1_level,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatTileTitle(text: String): String {
    val maxLength = 24
    var result = text.take(maxLength)
    
    val lines = result.split("\n")
    if (lines.size >= 2) {
        return result
    }
    
    val words = result.split(" ")
    if (words.isNotEmpty() && words[0].length > 12) {
        return result.split("").take(13).joinToString("").trim() + "\n" + 
               result.drop(12).take(12)
    }
    
    var currentLine = ""
    val formattedLines = mutableListOf<String>()
    
    for (word in words) {
        if ((currentLine + word).length <= 12) {
            currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        } else {
            if (currentLine.isNotEmpty()) formattedLines.add(currentLine)
            currentLine = word
        }
    }
    if (currentLine.isNotEmpty()) formattedLines.add(currentLine)
    
    return formattedLines.take(2).joinToString("\n")
}

