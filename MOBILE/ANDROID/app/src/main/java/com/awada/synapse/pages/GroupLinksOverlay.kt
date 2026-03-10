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
import kotlin.math.sqrt

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

            // Collect centers grouped by groupId, only for luminaire + bright sensor keys.
            val centersByGroup: Map<Int, List<Offset>> = buildMap {
                for ((key, gid) in groupIdByKey) {
                    if (gid == null) continue
                    if (key.type != DeviceType.Luminaire && key.type != DeviceType.BrightSensor) continue
                    val rect = circleBoundsInRootByKey[key] ?: continue
                    val centerInRoot = rect.center
                    val center = centerInRoot - canvasOriginInRoot
                    getOrPut(gid) { mutableListOf() }.let { (it as MutableList).add(center) }
                }
            }

            centersByGroup.values.forEach { points ->
                if (points.size < 2) return@forEach
                val edges = nearestTreeEdges(points)
                edges.forEach { (a, b) ->
                    drawLine(color = color, start = a, end = b, strokeWidth = strokeWidthPx, cap = StrokeCap.Round)
                }
            }
        }
    }
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

