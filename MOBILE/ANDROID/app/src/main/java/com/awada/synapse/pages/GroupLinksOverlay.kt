package com.awada.synapse.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private data class GroupLinkSegment(
    val groupId: Int,
    val startInRoot: Offset,
    val endInRoot: Offset
)

@Composable
internal fun GroupLinksOverlay(
    circleBoundsInRootByKey: Map<DeviceKey, Rect>,
    groupIdByKey: Map<DeviceKey, Int?>,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    var canvasOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    val strokePx = 2.dp

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onGloballyPositioned { coords ->
                    canvasOriginInRoot = coords.boundsInRoot().topLeft
                }
        ) {
            val color = PixsoColors.Color_Text_text_4_level.copy(alpha = 0.55f)
            val strokeWidthPx = strokePx.toPx()

            groupLinkSegmentsInRoot(
                circleBoundsInRootByKey = circleBoundsInRootByKey,
                groupIdByKey = groupIdByKey
            ).forEach { segment ->
                drawLine(
                    color = color,
                    start = segment.startInRoot - canvasOriginInRoot,
                    end = segment.endInRoot - canvasOriginInRoot,
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

internal fun groupLinkCoveredKeysInRoot(
    circleBoundsInRootByKey: Map<DeviceKey, Rect>,
    groupIdByKey: Map<DeviceKey, Int?>,
    lineHitSlopPx: Float = 0f
): Set<DeviceKey> {
    val segments = groupLinkSegmentsInRoot(
        circleBoundsInRootByKey = circleBoundsInRootByKey,
        groupIdByKey = groupIdByKey
    )
    if (segments.isEmpty()) return emptySet()

    return buildSet {
        for ((key, rect) in circleBoundsInRootByKey) {
            val ownGroupId = groupIdByKey[key]
            val center = rect.center
            val radius = min(rect.width, rect.height) / 2f + lineHitSlopPx
            if (
                segments.any { segment ->
                    segment.groupId != ownGroupId &&
                        distanceFromPointToSegment(center, segment.startInRoot, segment.endInRoot) < radius
                }
            ) {
                add(key)
            }
        }
    }
}

private fun groupLinkSegmentsInRoot(
    circleBoundsInRootByKey: Map<DeviceKey, Rect>,
    groupIdByKey: Map<DeviceKey, Int?>
): List<GroupLinkSegment> {
    val nodesByGroup: Map<Int, List<Pair<DeviceKey, Offset>>> = buildMap {
        for ((key, gid) in groupIdByKey) {
            if (gid == null) continue
            if (key.type != DeviceType.Luminaire && key.type != DeviceType.BrightSensor) continue
            val rect = circleBoundsInRootByKey[key] ?: continue
            getOrPut(gid) { mutableListOf() }.let { (it as MutableList).add(key to rect.center) }
        }
    }

    return buildList {
        nodesByGroup.forEach { (groupId, nodes) ->
            if (nodes.size < 2) return@forEach
            nearestTreeEdges(nodes.map { it.second }).forEach { (a, b) ->
                add(GroupLinkSegment(groupId = groupId, startInRoot = a, endInRoot = b))
            }
        }
    }
}

private fun distanceFromPointToSegment(point: Offset, start: Offset, end: Offset): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    if (dx == 0f && dy == 0f) return (point - start).getDistance()

    val t = (((point.x - start.x) * dx) + ((point.y - start.y) * dy)) / (dx * dx + dy * dy)
    val clampedT = max(0f, min(1f, t))
    val projection = Offset(start.x + dx * clampedT, start.y + dy * clampedT)
    return (point - projection).getDistance()
}

private fun nearestTreeEdges(points: List<Offset>): List<Pair<Offset, Offset>> {
    if (points.size < 2) return emptyList()
    val n = points.size
    val inTree = BooleanArray(n)
    inTree[0] = true
    var inCount = 1
    val edges = ArrayList<Pair<Offset, Offset>>(n - 1)

    fun dist(a: Offset, b: Offset): Float {
        // Euclidean distance in pixels
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    while (inCount < n) {
        var bestFrom = -1
        var bestTo = -1
        var bestDist = Float.MAX_VALUE
        for (i in 0 until n) if (inTree[i]) {
            val pi = points[i]
            for (j in 0 until n) if (!inTree[j]) {
                val d = dist(pi, points[j])
                if (d < bestDist) {
                    bestDist = d
                    bestFrom = i
                    bestTo = j
                }
            }
        }
        if (bestFrom == -1 || bestTo == -1) break
        edges.add(points[bestFrom] to points[bestTo])
        inTree[bestTo] = true
        inCount += 1
    }
    return edges
}

