package com.awada.synapse

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val DRAG_HANDLE_HEIGHT = 48.dp
private val MAIN_PANEL_HEIGHT = 100.dp
private val EXPANDED_TOP_OFFSET = 100.dp

enum class ChatState { Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIAI(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx() }
        
        val collapsedOffsetPx = with(density) { (maxHeight - MAIN_PANEL_HEIGHT - DRAG_HANDLE_HEIGHT).toPx() }
        val expandedOffsetPx = with(density) { EXPANDED_TOP_OFFSET.toPx() }
        
        val anchors = DraggableAnchors {
            ChatState.Collapsed at collapsedOffsetPx
            ChatState.Expanded at expandedOffsetPx
        }
        
        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = ChatState.Collapsed,
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.3f },
                velocityThreshold = { with(density) { 125.dp.toPx() } },
                snapAnimationSpec = tween(
                    durationMillis = 550,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)  // Slow end
                ),
                decayAnimationSpec = exponentialDecay(frictionMultiplier = 2f)
            )
        }
        
        val currentOffsetPx = try {
            anchoredDraggableState.requireOffset()
        } catch (e: IllegalStateException) {
            collapsedOffsetPx
        }
        
        UIAIChat(
            modifier = Modifier.fillMaxSize(),
            currentOffsetPx = currentOffsetPx,
            screenHeightPx = screenHeightPx,
            dragHandleHeightPx = with(density) { DRAG_HANDLE_HEIGHT.toPx() },
            anchoredDraggableState = anchoredDraggableState
        )
        
        UIAIMain(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            anchoredDraggableState = anchoredDraggableState
        )
    }
}
