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
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

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

        val path = buildPreviewMonotoneCubicPath(
            renderedPoints.map { point ->
                point.toOffset(metrics = metrics, valueRange = valueRange)
            }
        )

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

private fun buildPreviewMonotoneCubicPath(points: List<Offset>): Path {
    val path = Path()
    val firstPoint = points.firstOrNull() ?: return path
    path.moveTo(firstPoint.x, firstPoint.y)
    if (points.size == 1) {
        return path
    }
    if (points.size == 2) {
        val secondPoint = points[1]
        path.lineTo(secondPoint.x, secondPoint.y)
        return path
    }

    val slopes = MutableList(points.lastIndex) { index ->
        val start = points[index]
        val end = points[index + 1]
        val dx = end.x - start.x
        if (abs(dx) < 0.001f) {
            0f
        } else {
            (end.y - start.y) / dx
        }
    }
    val tangents = MutableList(points.size) { 0f }
    tangents[0] = slopes.first()
    tangents[points.lastIndex] = slopes.last()

    for (index in 1 until points.lastIndex) {
        val previousSlope = slopes[index - 1]
        val nextSlope = slopes[index]
        tangents[index] = if (previousSlope == 0f || nextSlope == 0f || previousSlope * nextSlope < 0f) {
            0f
        } else {
            (previousSlope + nextSlope) / 2f
        }
    }

    for (index in slopes.indices) {
        val slope = slopes[index]
        if (abs(slope) < 0.001f) {
            tangents[index] = 0f
            tangents[index + 1] = 0f
            continue
        }

        val alpha = tangents[index] / slope
        val beta = tangents[index + 1] / slope
        val scale = alpha * alpha + beta * beta
        if (scale > 9f) {
            val factor = 3f / sqrt(scale)
            tangents[index] = factor * alpha * slope
            tangents[index + 1] = factor * beta * slope
        }
    }

    for (index in 0 until points.lastIndex) {
        val start = points[index]
        val end = points[index + 1]
        val dx = end.x - start.x
        val controlPoint1 = Offset(
            x = start.x + dx / 3f,
            y = start.y + tangents[index] * dx / 3f,
        )
        val controlPoint2 = Offset(
            x = end.x - dx / 3f,
            y = end.y - tangents[index + 1] * dx / 3f,
        )
        path.cubicTo(
            x1 = controlPoint1.x,
            y1 = controlPoint1.y,
            x2 = controlPoint2.x,
            y2 = controlPoint2.y,
            x3 = end.x,
            y3 = end.y,
        )
    }

    return path
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
