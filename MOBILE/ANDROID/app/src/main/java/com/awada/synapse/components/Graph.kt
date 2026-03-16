package com.awada.synapse.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

private const val MINUTES_PER_DAY = 24 * 60

@Immutable
data class GraphPoint(
    val id: Long,
    val time: String,
    val value: Int,
)

private data class InternalGraphPoint(
    val id: Long,
    val minuteOfDay: Int,
    val value: Int,
    val isVirtual: Boolean = false,
)

private data class ChartMetrics(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun Graph(
    points: List<GraphPoint>,
    valueRange: IntRange,
    modifier: Modifier = Modifier,
    valueFormatter: (Int) -> String = { it.toString() },
    onPointsChange: (List<GraphPoint>) -> Unit,
) {
    val normalizedPoints = remember(points, valueRange) {
        normalizePoints(points = points, valueRange = valueRange)
    }
    val latestPoints = rememberUpdatedState(normalizedPoints)
    val latestOnPointsChange = rememberUpdatedState(onPointsChange)
    val renderedPoints = remember(normalizedPoints) {
        buildRenderedPoints(normalizedPoints)
    }
    val timeLabels = remember {
        listOf("00:00", "06:00", "12:00", "18:00", "24:00")
    }
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .pointerInput(valueRange) {
                    handleGraphGestures(
                        pointsProvider = { latestPoints.value },
                        valueRange = valueRange,
                        onPointsChange = { latestOnPointsChange.value(it) },
                    )
                }
        ) {
            val metrics = chartMetrics(width = size.width, height = size.height)
            val horizontalFractions = listOf(0f, 0.5f, 1f)
            val verticalFractions = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
            val lineStroke = 3.dp.toPx()
            val labelGap = 6.dp.toPx()

            horizontalFractions.forEach { fraction ->
                val y = metrics.top + metrics.height * fraction
                drawLine(
                    color = PixsoColors.Color_Border_border_shade_4,
                    start = Offset(metrics.left, y),
                    end = Offset(metrics.right, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            verticalFractions.forEach { fraction ->
                val x = metrics.left + metrics.width * fraction
                drawLine(
                    color = PixsoColors.Color_Border_border_shade_4,
                    start = Offset(x, metrics.top),
                    end = Offset(x, metrics.bottom),
                    strokeWidth = 1.dp.toPx(),
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
                color = PixsoColors.Color_State_primary,
                style = Stroke(
                    width = lineStroke,
                    cap = StrokeCap.Round,
                )
            )

            renderedPoints.forEach { point ->
                val center = point.toOffset(metrics = metrics, valueRange = valueRange)
                val pointRadius = if (point.isVirtual || point.minuteOfDay == 0) {
                    7.dp.toPx()
                } else {
                    6.dp.toPx()
                }
                val labelText = valueFormatter(point.value)
                val labelLayout = textMeasurer.measure(
                    text = labelText,
                    style = LabelLarge.copy(color = PixsoColors.Color_Text_text_1_level),
                )
                val labelX = (center.x - labelLayout.size.width / 2f)
                    .coerceIn(0f, size.width - labelLayout.size.width)
                val labelY = (center.y - pointRadius - labelLayout.size.height - labelGap)
                    .coerceAtLeast(0f)

                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(labelX, labelY),
                )

                drawCircle(
                    color = PixsoColors.Color_Bg_bg_surface,
                    radius = pointRadius,
                    center = center,
                )
                drawCircle(
                    color = PixsoColors.Color_State_primary,
                    radius = pointRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            timeLabels.forEach { label ->
                Text(
                    text = label,
                    style = LabelLarge,
                    color = PixsoColors.Color_Text_text_3_level,
                )
            }
        }
    }
}

private suspend fun PointerInputScope.handleGraphGestures(
    pointsProvider: () -> List<InternalGraphPoint>,
    valueRange: IntRange,
    onPointsChange: (List<GraphPoint>) -> Unit,
) {
    val metrics = chartMetrics(width = size.width.toFloat(), height = size.height.toFloat())
    val pointHitRadius = 16.dp.toPx()
    val segmentHitDistance = 20.dp.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Initial,
        )
        down.consume()
        var workingPoints = pointsProvider()
        var renderedPoints = buildRenderedPoints(workingPoints)
        val pointHit = findPointHit(
            position = down.position,
            points = renderedPoints,
            metrics = metrics,
            valueRange = valueRange,
            hitRadius = pointHitRadius,
        )

        if (pointHit != null) {
            val pointId = pointHit.id
            val isBoundaryPoint = pointHit.minuteOfDay == 0 || pointHit.isVirtual
            var isDrag = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                if (change == null) break

                change.consume()
                if (!change.pressed) {
                    if (!isDrag && !isBoundaryPoint) {
                        onPointsChange(
                            workingPoints
                                .filterNot { it.id == pointId }
                                .map(InternalGraphPoint::toExternal)
                        )
                    }
                    break
                }

                if (!isDrag && (change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                    isDrag = true
                }

                if (isDrag) {
                    workingPoints = movePoint(
                        points = workingPoints,
                        pointId = pointId,
                        isVirtualPoint = pointHit.isVirtual,
                        position = change.position,
                        valueRange = valueRange,
                        metrics = metrics,
                    )
                    onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))
                }
            }
            return@awaitEachGesture
        }

        val segmentIndex = findSegmentHit(
            position = down.position,
            points = renderedPoints,
            metrics = metrics,
            valueRange = valueRange,
            maxDistance = segmentHitDistance,
        )

        if (segmentIndex == null) {
            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                change?.consume()
                if (change == null || !change.pressed) break
            }
            return@awaitEachGesture
        }

        val insertedPoint = createPointOnSegment(
            position = down.position,
            points = workingPoints,
            renderedPoints = renderedPoints,
            segmentIndex = segmentIndex,
            valueRange = valueRange,
            metrics = metrics,
        ) ?: return@awaitEachGesture

        workingPoints = (workingPoints + insertedPoint).sortedBy(InternalGraphPoint::minuteOfDay)
        onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))

        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
            change?.consume()
            if (change == null || !change.pressed) break

            workingPoints = movePoint(
                points = workingPoints,
                pointId = insertedPoint.id,
                isVirtualPoint = false,
                position = change.position,
                valueRange = valueRange,
                metrics = metrics,
            )
            onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))
        }
    }
}

private fun movePoint(
    points: List<InternalGraphPoint>,
    pointId: Long,
    isVirtualPoint: Boolean,
    position: Offset,
    valueRange: IntRange,
    metrics: ChartMetrics,
): List<InternalGraphPoint> {
    if (isVirtualPoint) {
        val boundaryPoint = points.firstOrNull { it.minuteOfDay == 0 } ?: return points
        val updatedValue = yToValue(position.y, metrics, valueRange)
        return points.map { point ->
            if (point.id == boundaryPoint.id) {
                point.copy(value = updatedValue)
            } else {
                point
            }
        }
    }

    val currentIndex = points.indexOfFirst { it.id == pointId }
    if (currentIndex == -1) return points

    val point = points[currentIndex]
    val minMinute = when {
        point.minuteOfDay == 0 -> 0
        currentIndex == 0 -> 0
        else -> points[currentIndex - 1].minuteOfDay + 1
    }
    val maxMinute = when {
        point.minuteOfDay == 0 -> 0
        currentIndex == points.lastIndex -> MINUTES_PER_DAY - 1
        else -> points[currentIndex + 1].minuteOfDay - 1
    }

    val updatedMinute = if (point.minuteOfDay == 0) {
        0
    } else {
        xToMinute(position.x, metrics).coerceIn(minMinute, maxMinute)
    }
    val updatedValue = yToValue(position.y, metrics, valueRange)
    val updatedPoint = point.copy(minuteOfDay = updatedMinute, value = updatedValue)

    return points
        .map { if (it.id == pointId) updatedPoint else it }
        .sortedBy(InternalGraphPoint::minuteOfDay)
}

private fun createPointOnSegment(
    position: Offset,
    points: List<InternalGraphPoint>,
    renderedPoints: List<InternalGraphPoint>,
    segmentIndex: Int,
    valueRange: IntRange,
    metrics: ChartMetrics,
): InternalGraphPoint? {
    val start = renderedPoints.getOrNull(segmentIndex) ?: return null
    val end = renderedPoints.getOrNull(segmentIndex + 1) ?: return null
    val minMinute = start.minuteOfDay + 1
    val maxMinute = end.minuteOfDay - 1
    if (minMinute > maxMinute) return null

    val minute = xToMinute(position.x, metrics).coerceIn(minMinute, maxMinute)
    val value = yToValue(position.y, metrics, valueRange)
    val nextId = nextTemporaryInternalId(points)

    return InternalGraphPoint(
        id = nextId,
        minuteOfDay = minute,
        value = value,
    )
}

private fun findPointHit(
    position: Offset,
    points: List<InternalGraphPoint>,
    metrics: ChartMetrics,
    valueRange: IntRange,
    hitRadius: Float,
): InternalGraphPoint? {
    return points
        .sortedByDescending(InternalGraphPoint::isVirtual)
        .firstOrNull { point ->
        val offset = point.toOffset(metrics = metrics, valueRange = valueRange)
        (offset - position).getDistance() <= hitRadius
    }
}

private fun findSegmentHit(
    position: Offset,
    points: List<InternalGraphPoint>,
    metrics: ChartMetrics,
    valueRange: IntRange,
    maxDistance: Float,
): Int? {
    var nearestIndex: Int? = null
    var nearestDistance = Float.MAX_VALUE

    for (index in 0 until points.lastIndex) {
        val start = points[index].toOffset(metrics = metrics, valueRange = valueRange)
        val end = points[index + 1].toOffset(metrics = metrics, valueRange = valueRange)
        val distance = distanceToSegment(position = position, start = start, end = end)
        if (distance <= maxDistance && distance < nearestDistance) {
            nearestDistance = distance
            nearestIndex = index
        }
    }

    return nearestIndex
}

private fun distanceToSegment(
    position: Offset,
    start: Offset,
    end: Offset,
): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    if (abs(dx) < 0.001f && abs(dy) < 0.001f) {
        return (position - start).getDistance()
    }

    val t = (((position.x - start.x) * dx) + ((position.y - start.y) * dy)) /
        ((dx * dx) + (dy * dy))
    val clampedT = t.coerceIn(0f, 1f)
    val projection = Offset(
        x = start.x + dx * clampedT,
        y = start.y + dy * clampedT,
    )
    return hypot(position.x - projection.x, position.y - projection.y)
}

private fun buildRenderedPoints(points: List<InternalGraphPoint>): List<InternalGraphPoint> {
    val boundaryValue = points.firstOrNull { it.minuteOfDay == 0 }?.value ?: points.firstOrNull()?.value ?: 0
    return points + InternalGraphPoint(
        id = Long.MIN_VALUE,
        minuteOfDay = MINUTES_PER_DAY,
        value = boundaryValue,
        isVirtual = true,
    )
}

private fun normalizePoints(
    points: List<GraphPoint>,
    valueRange: IntRange,
): List<InternalGraphPoint> {
    val uniqueByMinute = linkedMapOf<Int, InternalGraphPoint>()

    points.forEach { point ->
        val minuteOfDay = timeToMinuteOfDay(point.time)
        uniqueByMinute[minuteOfDay] = InternalGraphPoint(
            id = point.id,
            minuteOfDay = minuteOfDay,
            value = point.value.coerceIn(valueRange.first, valueRange.last),
        )
    }

    if (0 !in uniqueByMinute) {
        uniqueByMinute[0] = InternalGraphPoint(
            id = nextTemporaryExternalId(points),
            minuteOfDay = 0,
            value = valueRange.first,
        )
    }

    return uniqueByMinute.values.sortedBy(InternalGraphPoint::minuteOfDay)
}

private fun nextTemporaryInternalId(points: List<InternalGraphPoint>): Long {
    val currentMin = points.minOfOrNull(InternalGraphPoint::id) ?: 0L
    return currentMin.coerceAtMost(0L) - 1L
}

private fun nextTemporaryExternalId(points: List<GraphPoint>): Long {
    val currentMin = points.minOfOrNull(GraphPoint::id) ?: 0L
    return currentMin.coerceAtMost(0L) - 1L
}

private fun InternalGraphPoint.toExternal(): GraphPoint {
    return GraphPoint(
        id = id,
        time = minuteOfDayToTime(minuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1)),
        value = value,
    )
}

private fun InternalGraphPoint.toOffset(
    metrics: ChartMetrics,
    valueRange: IntRange,
): Offset {
    return Offset(
        x = minuteToX(minuteOfDay, metrics),
        y = valueToY(value, metrics, valueRange),
    )
}

private fun Density.chartMetrics(width: Float, height: Float): ChartMetrics {
    val horizontalPadding = 10.dp.toPx()
    val topPadding = 28.dp.toPx()
    val bottomPadding = 16.dp.toPx()
    return ChartMetrics(
        left = horizontalPadding,
        top = topPadding,
        right = width - horizontalPadding,
        bottom = height - bottomPadding,
    )
}

private fun minuteToX(minuteOfDay: Int, metrics: ChartMetrics): Float {
    return metrics.left + (minuteOfDay / MINUTES_PER_DAY.toFloat()) * metrics.width
}

private fun xToMinute(x: Float, metrics: ChartMetrics): Int {
    val fraction = ((x - metrics.left) / metrics.width).coerceIn(0f, 1f)
    return (fraction * MINUTES_PER_DAY).roundToInt().coerceIn(0, MINUTES_PER_DAY)
}

private fun valueToY(
    value: Int,
    metrics: ChartMetrics,
    valueRange: IntRange,
): Float {
    val normalized = (value - valueRange.first).toFloat() / (valueRange.last - valueRange.first).toFloat()
    return metrics.bottom - normalized * metrics.height
}

private fun yToValue(
    y: Float,
    metrics: ChartMetrics,
    valueRange: IntRange,
): Int {
    val fraction = ((metrics.bottom - y) / metrics.height).coerceIn(0f, 1f)
    return (valueRange.first + fraction * (valueRange.last - valueRange.first))
        .roundToInt()
        .coerceIn(valueRange.first, valueRange.last)
}

private fun timeToMinuteOfDay(raw: String): Int {
    val digits = raw.filter(Char::isDigit).padEnd(4, '0').take(4)
    val hours = digits.substring(0, 2).toIntOrNull()?.coerceIn(0, 23) ?: 0
    val minutes = digits.substring(2, 4).toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hours * 60 + minutes
}

private fun minuteOfDayToTime(minuteOfDay: Int): String {
    val clampedMinute = minuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1)
    val hours = clampedMinute / 60
    val minutes = clampedMinute % 60
    return "%02d%02d".format(hours, minutes)
}
