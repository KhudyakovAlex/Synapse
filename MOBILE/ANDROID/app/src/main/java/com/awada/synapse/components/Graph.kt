package com.awada.synapse.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val MINUTES_PER_DAY = 24 * 60
private const val TIME_STEP_MINUTES = 5
private const val TIME_GRID_STEP_MINUTES = 6 * 60
private const val VIRTUAL_POINT_ID = Long.MIN_VALUE
private const val MAX_HORIZONTAL_ZOOM = 3f

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

private data class TimeViewport(
    val startMinute: Float,
    val endMinute: Float,
) {
    val duration: Float get() = endMinute - startMinute
}

private data class ViewportFlingRequest(
    val viewport: TimeViewport,
    val velocityMinutesPerSecond: Float,
)

private data class PointRenderInfo(
    val point: InternalGraphPoint,
    val center: Offset,
    val radius: Float,
    val valueLayout: TextLayoutResult,
    val timeLayout: TextLayoutResult,
    val valueBaseTop: Float,
    val timeBaseTop: Float,
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun Graph(
    points: List<GraphPoint>,
    valueRange: IntRange,
    modifier: Modifier = Modifier,
    viewportResetKey: Any? = Unit,
    valueFormatter: (Int) -> String = { it.toString() },
    onPointsChange: (List<GraphPoint>) -> Unit,
) {
    val normalizedPoints = remember(points, valueRange) {
        normalizePoints(points = points, valueRange = valueRange)
    }
    val latestPoints = rememberUpdatedState(normalizedPoints)
    val latestOnPointsChange = rememberUpdatedState(onPointsChange)
    var viewport by remember(viewportResetKey) { mutableStateOf(fullDayViewport()) }
    var viewportFlingRequest by remember(viewportResetKey) { mutableStateOf<ViewportFlingRequest?>(null) }
    val latestViewport = rememberUpdatedState(viewport)
    val latestOnViewportChange = rememberUpdatedState<(TimeViewport) -> Unit> { viewport = it }
    val latestOnViewportFlingChange =
        rememberUpdatedState<(ViewportFlingRequest?) -> Unit> { viewportFlingRequest = it }
    var activePointId by remember { mutableStateOf<Long?>(null) }
    val renderedPoints = remember(normalizedPoints) {
        buildRenderedPoints(normalizedPoints)
    }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(viewportFlingRequest) {
        val request = viewportFlingRequest ?: return@LaunchedEffect
        val maxStart = (MINUTES_PER_DAY.toFloat() - request.viewport.duration).coerceAtLeast(0f)
        if (maxStart <= 0f || abs(request.velocityMinutesPerSecond) < 1f) {
            if (viewportFlingRequest === request) {
                viewportFlingRequest = null
            }
            return@LaunchedEffect
        }

        var currentStart = request.viewport.startMinute
        var currentVelocity = request.velocityMinutesPerSecond
        var lastFrameNanos = 0L

        while (abs(currentVelocity) >= 1f) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    return@withFrameNanos
                }

                val dtSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameNanos

                currentStart = (currentStart + currentVelocity * dtSeconds).coerceIn(0f, maxStart)
                viewport = TimeViewport(
                    startMinute = currentStart,
                    endMinute = currentStart + request.viewport.duration,
                )

                currentVelocity *= exp(-4f * dtSeconds)
                if (currentStart <= 0.1f || currentStart >= maxStart - 0.1f) {
                    currentVelocity = 0f
                }
            }
        }

        if (viewportFlingRequest === request) {
            viewportFlingRequest = null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(288.dp)
                .pointerInput(valueRange, viewportResetKey) {
                    handleGraphGestures(
                        pointsProvider = { latestPoints.value },
                        viewportProvider = { latestViewport.value },
                        valueRange = valueRange,
                        onPointsChange = { latestOnPointsChange.value(it) },
                        onViewportChange = { latestOnViewportChange.value(it) },
                        onViewportFlingChange = { latestOnViewportFlingChange.value(it) },
                        onActivePointChange = { activePointId = it },
                    )
                }
        ) {
            val metrics = chartMetrics(width = size.width, height = size.height)
            val horizontalFractions = listOf(0f, 0.5f, 1f)
            val lineStroke = 3.dp.toPx()
            val labelGap = 10.dp.toPx()
            val timeGap = 10.dp.toPx()
            val labelStackGap = 6.dp.toPx()

            horizontalFractions.forEach { fraction ->
                val y = metrics.top + metrics.height * fraction
                val gridStartX = minuteToX(
                    minuteOfDay = 0,
                    metrics = metrics,
                    viewport = viewport,
                )
                val gridEndX = minuteToX(
                    minuteOfDay = MINUTES_PER_DAY,
                    metrics = metrics,
                    viewport = viewport,
                )
                drawLine(
                    color = Color.White,
                    start = Offset(gridStartX, y),
                    end = Offset(gridEndX, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            allTimeGridMinutes().forEach { minute ->
                val x = minuteToX(
                    minuteOfDay = minute,
                    metrics = metrics,
                    viewport = viewport,
                )
                drawLine(
                    color = Color.White,
                    start = Offset(x, metrics.top),
                    end = Offset(x, metrics.bottom),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val path = buildMonotoneCubicPath(
                renderedPoints.map { point ->
                    point.toOffset(
                        metrics = metrics,
                        valueRange = valueRange,
                        viewport = viewport,
                    )
                }
            )

            drawPath(
                path = path,
                color = PixsoColors.Color_Bg_bg_elevated,
                style = Stroke(
                    width = lineStroke,
                    cap = StrokeCap.Round,
                )
            )

            val pointInfos = renderedPoints.map { point ->
                val center = point.toOffset(
                    metrics = metrics,
                    valueRange = valueRange,
                    viewport = viewport,
                )
                val isActive = activePointId == point.id
                val basePointRadius = if (point.isVirtual || point.minuteOfDay == 0) {
                    7.dp.toPx()
                } else {
                    6.dp.toPx()
                }
                val pointRadius = if (isActive) {
                    basePointRadius * 2f
                } else {
                    basePointRadius
                }
                val labelText = valueFormatter(point.value)
                val labelLayout = textMeasurer.measure(
                    text = labelText,
                    style = LabelLarge.copy(color = PixsoColors.Color_Text_text_1_level),
                )
                val timeText = if (point.isVirtual) {
                    "24:00"
                } else {
                    formatTimeLabel(point.minuteOfDay)
                }
                val timeLayout = textMeasurer.measure(
                    text = timeText,
                    style = LabelLarge.copy(color = PixsoColors.Color_Text_text_3_level),
                )
                val valueGapMultiplier = if (isActive) 3f else 1f
                val timeGapMultiplier = if (isActive) 4f else 1f

                PointRenderInfo(
                    point = point,
                    center = center,
                    radius = pointRadius,
                    valueLayout = labelLayout,
                    timeLayout = timeLayout,
                    valueBaseTop = (center.y - pointRadius - labelLayout.size.height - labelGap * valueGapMultiplier)
                        .coerceAtLeast(0f),
                    timeBaseTop = (center.y + pointRadius + timeGap * timeGapMultiplier)
                        .coerceAtMost(size.height - timeLayout.size.height.toFloat()),
                )
            }
            val valuePositions = placeLabelsAbove(
                infos = pointInfos,
                spacing = labelStackGap,
            )
            val timePositions = placeLabelsBelow(
                infos = pointInfos,
                canvasHeight = size.height,
                spacing = labelStackGap,
            )

            pointInfos.forEach { info ->
                valuePositions[info.point.id]?.let { topLeft ->
                    drawText(
                        textLayoutResult = info.valueLayout,
                        topLeft = topLeft,
                    )
                }

                timePositions[info.point.id]?.let { topLeft ->
                    drawText(
                        textLayoutResult = info.timeLayout,
                        topLeft = topLeft,
                    )
                }

                drawCircle(
                    color = PixsoColors.Color_Bg_bg_surface,
                    radius = info.radius,
                    center = info.center,
                )
                drawCircle(
                    color = PixsoColors.Color_Bg_bg_elevated,
                    radius = info.radius,
                    center = info.center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

private suspend fun PointerInputScope.handleGraphGestures(
    pointsProvider: () -> List<InternalGraphPoint>,
    viewportProvider: () -> TimeViewport,
    valueRange: IntRange,
    onPointsChange: (List<GraphPoint>) -> Unit,
    onViewportChange: (TimeViewport) -> Unit,
    onViewportFlingChange: (ViewportFlingRequest?) -> Unit,
    onActivePointChange: (Long?) -> Unit,
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
        onViewportFlingChange(null)
        var workingPoints = pointsProvider()
        var workingViewport = viewportProvider()
        var renderedPoints = buildRenderedPoints(workingPoints)
        val pointHit = findPointHit(
            position = down.position,
            points = renderedPoints,
            metrics = metrics,
            valueRange = valueRange,
            viewport = workingViewport,
            hitRadius = pointHitRadius,
        )

        if (pointHit != null) {
            onActivePointChange(pointHit.id)
            val pointId = pointHit.id
            val isBoundaryPoint = pointHit.minuteOfDay == 0 || pointHit.isVirtual
            var isDrag = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    onActivePointChange(null)
                    zoomViewportWhilePressed(
                        firstEvent = event,
                        initialViewport = workingViewport,
                        metrics = metrics,
                        onViewportChange = {
                            workingViewport = it
                            onViewportChange(it)
                        },
                    )
                    return@awaitEachGesture
                }
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
                    onActivePointChange(null)
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
                        viewport = workingViewport,
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
            viewport = workingViewport,
            maxDistance = segmentHitDistance,
        )
        var insertedPointId: Long? = null
        var isPan = false
        val canPanFromDown = down.position.isInsideChart(metrics)
        val velocityTracker = if (segmentIndex == null && canPanFromDown) VelocityTracker() else null
        velocityTracker?.addPosition(down.uptimeMillis, down.position)

        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            if (event.changes.count { it.pressed } >= 2) {
                onActivePointChange(null)
                zoomViewportWhilePressed(
                    firstEvent = event,
                    initialViewport = workingViewport,
                    metrics = metrics,
                    onViewportChange = {
                        workingViewport = it
                        onViewportChange(it)
                    },
                )
                return@awaitEachGesture
            }
            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
            change?.consume()
            if (change != null) {
                velocityTracker?.addPosition(change.uptimeMillis, change.position)
            }
            if (segmentIndex == null) {
                if (change != null && change.pressed && canPanFromDown) {
                    val totalDelta = change.position - down.position
                    if (!isPan &&
                        totalDelta.getDistance() > viewConfiguration.touchSlop &&
                        abs(totalDelta.x) > abs(totalDelta.y)
                    ) {
                        isPan = true
                    }

                    if (isPan) {
                        val updatedViewport = applyHorizontalPan(
                            viewport = workingViewport,
                            deltaX = change.position.x - change.previousPosition.x,
                            metrics = metrics,
                        )
                        if (updatedViewport != workingViewport) {
                            workingViewport = updatedViewport
                            onViewportChange(updatedViewport)
                        }
                    }
                }
                if (change == null || !change.pressed) {
                    if (isPan) {
                        createViewportFlingRequest(
                            viewport = workingViewport,
                            velocityX = velocityTracker?.calculateVelocity()?.x ?: 0f,
                            metrics = metrics,
                        )?.let { onViewportFlingChange(it) }
                    } else if (canPanFromDown) {
                        val updatedViewport = toggleViewportZoom(
                            viewport = workingViewport,
                            tapX = change?.position?.x ?: down.position.x,
                            metrics = metrics,
                        )
                        if (updatedViewport != workingViewport) {
                            workingViewport = updatedViewport
                            onViewportChange(updatedViewport)
                        }
                    }
                    onActivePointChange(null)
                    break
                }
                continue
            }

            if (change == null) {
                onActivePointChange(null)
                break
            }

            val movedBeyondSlop = (change.position - down.position).getDistance() > viewConfiguration.touchSlop

            if (!change.pressed) {
                if (insertedPointId == null) {
                    val insertedPoint = createPointOnSegment(
                        position = change.position,
                        points = workingPoints,
                        renderedPoints = renderedPoints,
                        segmentIndex = segmentIndex,
                        valueRange = valueRange,
                        metrics = metrics,
                        viewport = workingViewport,
                    )
                    if (insertedPoint != null) {
                        workingPoints = (workingPoints + insertedPoint).sortedBy(InternalGraphPoint::minuteOfDay)
                        onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))
                    }
                }
                onActivePointChange(null)
                break
            }

            if (movedBeyondSlop && insertedPointId == null) {
                val insertedPoint = createPointOnSegment(
                    position = change.position,
                    points = workingPoints,
                    renderedPoints = renderedPoints,
                    segmentIndex = segmentIndex,
                    valueRange = valueRange,
                    metrics = metrics,
                    viewport = workingViewport,
                ) ?: break
                workingPoints = (workingPoints + insertedPoint).sortedBy(InternalGraphPoint::minuteOfDay)
                insertedPointId = insertedPoint.id
                onActivePointChange(insertedPointId)
                onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))
            }

            if (insertedPointId != null) {
                workingPoints = movePoint(
                    points = workingPoints,
                    pointId = insertedPointId,
                    isVirtualPoint = false,
                    position = change.position,
                    valueRange = valueRange,
                    metrics = metrics,
                    viewport = workingViewport,
                )
                onPointsChange(workingPoints.map(InternalGraphPoint::toExternal))
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.zoomViewportWhilePressed(
    firstEvent: androidx.compose.ui.input.pointer.PointerEvent,
    initialViewport: TimeViewport,
    metrics: ChartMetrics,
    onViewportChange: (TimeViewport) -> Unit,
) {
    var currentViewport = initialViewport
    var event = firstEvent

    while (true) {
        val pressedChanges = event.changes.filter { it.pressed }
        pressedChanges.forEach { it.consume() }
        if (pressedChanges.size < 2) {
            return
        }

        val updatedViewport = applyHorizontalZoom(
            viewport = currentViewport,
            changes = pressedChanges,
            metrics = metrics,
        )
        if (updatedViewport != currentViewport) {
            currentViewport = updatedViewport
            onViewportChange(updatedViewport)
        }

        event = awaitPointerEvent(pass = PointerEventPass.Initial)
    }
}

private fun movePoint(
    points: List<InternalGraphPoint>,
    pointId: Long,
    isVirtualPoint: Boolean,
    position: Offset,
    valueRange: IntRange,
    metrics: ChartMetrics,
    viewport: TimeViewport,
): List<InternalGraphPoint> {
    if (isVirtualPoint) {
        val boundaryPoint = points.firstOrNull { it.minuteOfDay == 0 } ?: return points
        val updatedValue = snapValueToStep(
            value = yToValue(position.y, metrics, valueRange),
            valueRange = valueRange,
        )
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
        snapMinuteToStep(
            minute = xToMinute(position.x, metrics, viewport),
            minAllowed = minMinute,
            maxAllowed = maxMinute,
        )
    }
    val updatedValue = snapValueToStep(
        value = yToValue(position.y, metrics, valueRange),
        valueRange = valueRange,
    )
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
    viewport: TimeViewport,
): InternalGraphPoint? {
    val start = renderedPoints.getOrNull(segmentIndex) ?: return null
    val end = renderedPoints.getOrNull(segmentIndex + 1) ?: return null
    val minMinute = start.minuteOfDay + 1
    val maxMinute = end.minuteOfDay - 1
    if (minMinute > maxMinute) return null

    val minute = snapMinuteToStep(
        minute = xToMinute(position.x, metrics, viewport),
        minAllowed = minMinute,
        maxAllowed = maxMinute,
    )
    val value = snapValueToStep(
        value = yToValue(position.y, metrics, valueRange),
        valueRange = valueRange,
    )
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
    viewport: TimeViewport,
    hitRadius: Float,
): InternalGraphPoint? {
    return points
        .sortedByDescending(InternalGraphPoint::isVirtual)
        .firstOrNull { point ->
        val offset = point.toOffset(metrics = metrics, valueRange = valueRange, viewport = viewport)
        (offset - position).getDistance() <= hitRadius
    }
}

private fun findSegmentHit(
    position: Offset,
    points: List<InternalGraphPoint>,
    metrics: ChartMetrics,
    valueRange: IntRange,
    viewport: TimeViewport,
    maxDistance: Float,
): Int? {
    var nearestIndex: Int? = null
    var nearestDistance = Float.MAX_VALUE

    for (index in 0 until points.lastIndex) {
        val start = points[index].toOffset(metrics = metrics, valueRange = valueRange, viewport = viewport)
        val end = points[index + 1].toOffset(metrics = metrics, valueRange = valueRange, viewport = viewport)
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

private fun buildMonotoneCubicPath(points: List<Offset>): Path {
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

private fun buildRenderedPoints(points: List<InternalGraphPoint>): List<InternalGraphPoint> {
    val boundaryValue = points.firstOrNull { it.minuteOfDay == 0 }?.value ?: points.firstOrNull()?.value ?: 0
    return points + InternalGraphPoint(
        id = VIRTUAL_POINT_ID,
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
        val minuteOfDay = snapMinuteToStep(timeToMinuteOfDay(point.time))
        uniqueByMinute[minuteOfDay] = InternalGraphPoint(
            id = point.id,
            minuteOfDay = minuteOfDay,
            value = snapValueToStep(point.value, valueRange),
        )
    }

    if (0 !in uniqueByMinute) {
        uniqueByMinute[0] = InternalGraphPoint(
            id = nextTemporaryExternalId(points),
            minuteOfDay = 0,
            value = defaultGraphValueForRange(valueRange),
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
    viewport: TimeViewport,
): Offset {
    return Offset(
        x = minuteToX(minuteOfDay, metrics, viewport),
        y = valueToY(value, metrics, valueRange),
    )
}

private fun Density.chartMetrics(width: Float, height: Float): ChartMetrics {
    val horizontalPadding = 10.dp.toPx()
    val topPadding = 56.dp.toPx()
    val bottomPadding = 104.dp.toPx()
    return ChartMetrics(
        left = horizontalPadding,
        top = topPadding,
        right = width - horizontalPadding,
        bottom = height - bottomPadding,
    )
}

private fun fullDayViewport(): TimeViewport {
    return TimeViewport(
        startMinute = 0f,
        endMinute = MINUTES_PER_DAY.toFloat(),
    )
}

private fun minuteToX(
    minuteOfDay: Int,
    metrics: ChartMetrics,
    viewport: TimeViewport,
): Float {
    return metrics.left + ((minuteOfDay - viewport.startMinute) / viewport.duration) * metrics.width
}

private fun xToMinute(
    x: Float,
    metrics: ChartMetrics,
    viewport: TimeViewport,
): Int {
    val fraction = ((x - metrics.left) / metrics.width).coerceIn(0f, 1f)
    return (viewport.startMinute + fraction * viewport.duration)
        .roundToInt()
        .coerceIn(0, MINUTES_PER_DAY)
}

private fun allTimeGridMinutes(): List<Int> {
    return buildList {
        var minute = 0
        while (minute <= MINUTES_PER_DAY) {
            add(minute)
            minute += TIME_GRID_STEP_MINUTES
        }
    }
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

private fun formatTimeLabel(minuteOfDay: Int): String {
    val clampedMinute = minuteOfDay.coerceIn(0, MINUTES_PER_DAY)
    val hours = clampedMinute / 60
    val minutes = clampedMinute % 60
    return "%02d:%02d".format(hours, minutes)
}

private fun snapMinuteToStep(
    minute: Int,
    minAllowed: Int = 0,
    maxAllowed: Int = MINUTES_PER_DAY - 1,
): Int {
    val safeMin = minAllowed.coerceIn(0, MINUTES_PER_DAY - 1)
    val safeMax = maxAllowed.coerceIn(safeMin, MINUTES_PER_DAY - 1)
    val firstStep = ((safeMin + TIME_STEP_MINUTES - 1) / TIME_STEP_MINUTES) * TIME_STEP_MINUTES
    val lastStep = (safeMax / TIME_STEP_MINUTES) * TIME_STEP_MINUTES
    if (firstStep > lastStep) {
        return minute.coerceIn(safeMin, safeMax)
    }

    val snapped = ((minute.toFloat() / TIME_STEP_MINUTES).roundToInt() * TIME_STEP_MINUTES)
        .coerceIn(firstStep, lastStep)
    return snapped
}

private fun snapValueToStep(
    value: Int,
    valueRange: IntRange,
): Int {
    val step = valueStepForRange(valueRange)
    val normalized = ((value - valueRange.first).toFloat() / step).roundToInt() * step
    return (valueRange.first + normalized).coerceIn(valueRange.first, valueRange.last)
}

private fun valueStepForRange(valueRange: IntRange): Int {
    return if (valueRange.first >= 3000) 100 else 5
}

private fun defaultGraphValueForRange(valueRange: IntRange): Int {
    return if (valueRange.first >= 3000) 4000 else 0
}

private fun applyHorizontalZoom(
    viewport: TimeViewport,
    changes: List<androidx.compose.ui.input.pointer.PointerInputChange>,
    metrics: ChartMetrics,
): TimeViewport {
    if (changes.size < 2) return viewport

    val first = changes[0]
    val second = changes[1]
    val previousDistanceX = abs(first.previousPosition.x - second.previousPosition.x)
    val currentDistanceX = abs(first.position.x - second.position.x)
    if (previousDistanceX < 8f || currentDistanceX < 8f) {
        return viewport
    }

    val zoomChange = currentDistanceX / previousDistanceX
    if (!zoomChange.isFinite() || abs(zoomChange - 1f) < 0.01f) {
        return viewport
    }

    val anchorX = (first.position.x + second.position.x) / 2f
    val currentZoom = MINUTES_PER_DAY / viewport.duration
    val targetZoom = (currentZoom * zoomChange).coerceIn(1f, MAX_HORIZONTAL_ZOOM)
    return viewportForZoom(
        currentViewport = viewport,
        targetZoom = targetZoom,
        anchorX = anchorX,
        metrics = metrics,
    )
}

private fun applyHorizontalPan(
    viewport: TimeViewport,
    deltaX: Float,
    metrics: ChartMetrics,
): TimeViewport {
    if (abs(deltaX) < 0.5f || viewport.duration >= MINUTES_PER_DAY) {
        return viewport
    }

    val deltaMinutes = (deltaX / metrics.width) * viewport.duration
    val maxStart = (MINUTES_PER_DAY.toFloat() - viewport.duration).coerceAtLeast(0f)
    val targetStart = (viewport.startMinute - deltaMinutes).coerceIn(0f, maxStart)
    val targetEnd = targetStart + viewport.duration

    if (abs(targetStart - viewport.startMinute) < 0.1f) {
        return viewport
    }

    return TimeViewport(
        startMinute = targetStart,
        endMinute = targetEnd,
    )
}

private fun createViewportFlingRequest(
    viewport: TimeViewport,
    velocityX: Float,
    metrics: ChartMetrics,
): ViewportFlingRequest? {
    if (viewport.duration >= MINUTES_PER_DAY || abs(velocityX) < 80f) {
        return null
    }

    val velocityMinutesPerSecond = -(velocityX / metrics.width) * viewport.duration
    if (abs(velocityMinutesPerSecond) < 1f) {
        return null
    }

    return ViewportFlingRequest(
        viewport = viewport,
        velocityMinutesPerSecond = velocityMinutesPerSecond,
    )
}

private fun toggleViewportZoom(
    viewport: TimeViewport,
    tapX: Float,
    metrics: ChartMetrics,
): TimeViewport {
    val currentZoom = MINUTES_PER_DAY / viewport.duration
    val targetZoom = if (currentZoom <= 1.01f) MAX_HORIZONTAL_ZOOM else 1f
    return viewportForZoom(
        currentViewport = viewport,
        targetZoom = targetZoom,
        anchorX = tapX,
        metrics = metrics,
    )
}

private fun viewportForZoom(
    currentViewport: TimeViewport,
    targetZoom: Float,
    anchorX: Float,
    metrics: ChartMetrics,
): TimeViewport {
    val normalizedTargetZoom = targetZoom.coerceIn(1f, MAX_HORIZONTAL_ZOOM)
    val anchorFraction = ((anchorX - metrics.left) / metrics.width).coerceIn(0f, 1f)
    val anchorMinute = currentViewport.startMinute + anchorFraction * currentViewport.duration
    val targetDuration = MINUTES_PER_DAY / normalizedTargetZoom
    val maxStart = (MINUTES_PER_DAY.toFloat() - targetDuration).coerceAtLeast(0f)
    val targetStart = (anchorMinute - anchorFraction * targetDuration).coerceIn(0f, maxStart)
    val targetEnd = targetStart + targetDuration

    if (abs(targetStart - currentViewport.startMinute) < 0.1f &&
        abs(targetEnd - currentViewport.endMinute) < 0.1f
    ) {
        return currentViewport
    }

    return TimeViewport(
        startMinute = targetStart,
        endMinute = targetEnd,
    )
}

private fun Offset.isInsideChart(metrics: ChartMetrics): Boolean {
    return x in metrics.left..metrics.right && y in metrics.top..metrics.bottom
}

private fun placeLabelsAbove(
    infos: List<PointRenderInfo>,
    spacing: Float,
): Map<Long, Offset> {
    val result = linkedMapOf<Long, Offset>()
    val placedRects = mutableListOf<Rect>()

    infos.sortedBy { it.center.x }.forEach { info ->
        val left = info.center.x - info.valueLayout.size.width / 2f
        var top = info.valueBaseTop

        while (true) {
            val rect = Rect(
                left = left,
                top = top,
                right = left + info.valueLayout.size.width,
                bottom = top + info.valueLayout.size.height,
            )
            val overlapping = placedRects.filter { it.overlaps(rect) }
            if (overlapping.isEmpty()) {
                result[info.point.id] = Offset(left, top)
                placedRects += rect
                break
            }

            val nextTop = (overlapping.minOf { it.top } - info.valueLayout.size.height - spacing)
                .coerceAtLeast(0f)
            if (abs(nextTop - top) < 0.5f) {
                result[info.point.id] = Offset(left, top)
                placedRects += rect
                break
            }
            top = nextTop
        }
    }

    return result
}

private fun placeLabelsBelow(
    infos: List<PointRenderInfo>,
    canvasHeight: Float,
    spacing: Float,
): Map<Long, Offset> {
    val result = linkedMapOf<Long, Offset>()
    val placedRects = mutableListOf<Rect>()

    infos.sortedBy { it.center.x }.forEach { info ->
        val left = info.center.x - info.timeLayout.size.width / 2f
        var top = info.timeBaseTop

        while (true) {
            val rect = Rect(
                left = left,
                top = top,
                right = left + info.timeLayout.size.width,
                bottom = top + info.timeLayout.size.height,
            )
            val overlapping = placedRects.filter { it.overlaps(rect) }
            if (overlapping.isEmpty()) {
                result[info.point.id] = Offset(left, top)
                placedRects += rect
                break
            }

            val nextTop = (overlapping.maxOf { it.bottom } + spacing)
                .coerceAtMost(canvasHeight - info.timeLayout.size.height.toFloat())
            if (abs(nextTop - top) < 0.5f) {
                result[info.point.id] = Offset(left, top)
                placedRects += rect
                break
            }
            top = nextTop
        }
    }

    return result
}
