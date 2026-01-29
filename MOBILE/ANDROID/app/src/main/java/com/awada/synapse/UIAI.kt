package com.awada.synapse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val DRAG_HANDLE_HEIGHT = 48.dp
private val MAIN_PANEL_HEIGHT = 100.dp
private val EXPANDED_TOP_OFFSET = 100.dp

enum class ChatState { Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIAI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val isAIEnabled by settingsRepository.isAIEnabled.collectAsState(initial = true)
    
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
        
        // Container that clips chat at Main panel top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight - MAIN_PANEL_HEIGHT)
                .clipToBounds()
        ) {
            AnimatedVisibility(
                visible = isAIEnabled,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { it / 2 }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                ) {
                    UIAIChat(
                        modifier = Modifier.fillMaxSize(),
                        currentOffsetPx = currentOffsetPx,
                        screenHeightPx = screenHeightPx,
                        expandedTopOffsetPx = expandedOffsetPx,
                        anchoredDraggableState = anchoredDraggableState
                    )
                }
            }
        }
        
        UIAIMain(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            anchoredDraggableState = anchoredDraggableState
        )
    }
}
