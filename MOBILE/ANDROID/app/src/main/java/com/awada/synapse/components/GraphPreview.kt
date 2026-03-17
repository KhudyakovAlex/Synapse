package com.awada.synapse.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.math.roundToInt

private const val PREVIEW_MINUTES_PER_DAY = 24 * 60
private const val PREVIEW_TIME_STEP_MINUTES = 5
private const val PREVIEW_TIME_GRID_STEP_MINUTES = 6 * 60

@Immutable
private data class PreviewGraphPoint(
    val minuteOfDay: Int,
    val value: Int,
    val isVirtual: Boolean = false,
)

private data class PreviewChartMetrics(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

@Composable
fun GraphPreview(
    points: List<GraphPoint>,
    valueRange: IntRange,
    modifier: Modifier = Modifier,
) {
    val normalizedPoints = remember(points, valueRange) {
        normalizePreviewPoints(points = points, valueRange = valueRange)
    }
    val renderedPoints = remember(normalizedPoints, valueRange) {
        buildPreviewRenderedPoints(points = normalizedPoints, valueRange = valueRange)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        val metrics = previewChartMetrics(width = size.width, height = size.height)
        val gridColor = PixsoColors.Color_Border_border_primary.copy(
            alpha = PixsoColors.Color_Border_border_primary.alpha * 0.5f
        )
        val gridStroke = 1.dp.toPx()
        val lineStroke = 3.dp.toPx()
        val pointRadius = 4.dp.toPx()

        listOf(0f, 0.5f, 1f).forEach { fraction ->
            val y = metrics.top + metrics.height * fraction
            drawLine(
                color = gridColor,
                start = Offset(metrics.left, y),
                end = Offset(metrics.right, y),
                strokeWidth = gridStroke,
            )
        }

        allPreviewGridMinutes().forEach { minute ->
            val x = previewMinuteToX(minuteOfDay = minute, metrics = metrics)
            drawLine(
                color = gridColor,
                start = Offset(x, metrics.top),
                end = Offset(x, metrics.bottom),
                strokeWidth = gridStroke,
            )
        }

        val path = Path()
        renderedPoints.forEachIndexed { index, point ->
            val offset = point.toOffset(metrics = metrics, valueRange = valueRange)
            if (index == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }

        drawPath(
            path = path,
            color = PixsoColors.Color_Bg_bg_elevated,
            style = Stroke(width = lineStroke, cap = StrokeCap.Round),
        )

        renderedPoints.forEach { point ->
            val center = point.toOffset(metrics = metrics, valueRange = valueRange)
            val radius = if (point.isVirtual || point.minuteOfDay == 0) {
                pointRadius + 1.dp.toPx()
            } else {
                pointRadius
            }
            drawCircle(
                color = PixsoColors.Color_Bg_bg_surface,
                radius = radius,
                center = center,
            )
            drawCircle(
                color = PixsoColors.Color_Bg_bg_elevated,
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

private fun Density.previewChartMetrics(width: Float, height: Float): PreviewChartMetrics {
    val padding = 8.dp.toPx()
    return PreviewChartMetrics(
        left = padding,
        top = padding,
        right = width - padding,
        bottom = height - padding,
    )
}

private fun normalizePreviewPoints(
    points: List<GraphPoint>,
    valueRange: IntRange,
): List<PreviewGraphPoint> {
    val uniqueByMinute = linkedMapOf<Int, PreviewGraphPoint>()

    points.forEach { point ->
        val minute = snapPreviewMinuteToStep(timeToPreviewMinuteOfDay(point.time))
        uniqueByMinute[minute] = PreviewGraphPoint(
            minuteOfDay = minute,
            value = snapPreviewValueToStep(point.value, valueRange),
        )
    }

    if (0 !in uniqueByMinute) {
        uniqueByMinute[0] = PreviewGraphPoint(
            minuteOfDay = 0,
            value = defaultPreviewValueForRange(valueRange),
        )
    }

    return uniqueByMinute.values.sortedBy(PreviewGraphPoint::minuteOfDay)
}

private fun buildPreviewRenderedPoints(
    points: List<PreviewGraphPoint>,
    valueRange: IntRange,
): List<PreviewGraphPoint> {
    val boundaryValue = points.firstOrNull { it.minuteOfDay == 0 }?.value
        ?: points.firstOrNull()?.value
        ?: defaultPreviewValueForRange(valueRange)
    return points + PreviewGraphPoint(
        minuteOfDay = PREVIEW_MINUTES_PER_DAY,
        value = boundaryValue,
        isVirtual = true,
    )
}

private fun PreviewGraphPoint.toOffset(
    metrics: PreviewChartMetrics,
    valueRange: IntRange,
): Offset {
    return Offset(
        x = previewMinuteToX(minuteOfDay = minuteOfDay, metrics = metrics),
        y = previewValueToY(value = value, metrics = metrics, valueRange = valueRange),
    )
}

private fun previewMinuteToX(
    minuteOfDay: Int,
    metrics: PreviewChartMetrics,
): Float {
    return metrics.left + (minuteOfDay / PREVIEW_MINUTES_PER_DAY.toFloat()) * metrics.width
}

private fun previewValueToY(
    value: Int,
    metrics: PreviewChartMetrics,
    valueRange: IntRange,
): Float {
    val span = (valueRange.last - valueRange.first).coerceAtLeast(1)
    val normalized = (value - valueRange.first).toFloat() / span.toFloat()
    return metrics.bottom - normalized * metrics.height
}

private fun allPreviewGridMinutes(): List<Int> {
    return buildList {
        var minute = 0
        while (minute <= PREVIEW_MINUTES_PER_DAY) {
            add(minute)
            minute += PREVIEW_TIME_GRID_STEP_MINUTES
        }
    }
}

private fun timeToPreviewMinuteOfDay(raw: String): Int {
    val digits = raw.filter(Char::isDigit).padEnd(4, '0').take(4)
    val hours = digits.substring(0, 2).toIntOrNull()?.coerceIn(0, 23) ?: 0
    val minutes = digits.substring(2, 4).toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hours * 60 + minutes
}

private fun snapPreviewMinuteToStep(
    minute: Int,
    minAllowed: Int = 0,
    maxAllowed: Int = PREVIEW_MINUTES_PER_DAY - PREVIEW_TIME_STEP_MINUTES,
): Int {
    val safeMin = minAllowed.coerceIn(0, PREVIEW_MINUTES_PER_DAY - PREVIEW_TIME_STEP_MINUTES)
    val safeMax = maxAllowed.coerceIn(safeMin, PREVIEW_MINUTES_PER_DAY - PREVIEW_TIME_STEP_MINUTES)
    val snapped = ((minute.toFloat() / PREVIEW_TIME_STEP_MINUTES).roundToInt() * PREVIEW_TIME_STEP_MINUTES)
    return snapped.coerceIn(safeMin, safeMax)
}

private fun snapPreviewValueToStep(
    value: Int,
    valueRange: IntRange,
): Int {
    val step = previewValueStepForRange(valueRange)
    val normalized = ((value - valueRange.first).toFloat() / step).roundToInt() * step
    return (valueRange.first + normalized).coerceIn(valueRange.first, valueRange.last)
}

private fun previewValueStepForRange(valueRange: IntRange): Int {
    return if (valueRange.first >= 3000) 100 else 5
}

private fun defaultPreviewValueForRange(valueRange: IntRange): Int {
    return if (valueRange.first >= 3000) 4000 else 0
}
