package com.awada.synapse.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.data.SettingsRepository
import com.awada.synapse.ui.theme.PixsoColors

private val DRAG_HANDLE_HEIGHT = 48.dp
private val MAIN_PANEL_HEIGHT = 100.dp
private val EXPANDED_TOP_OFFSET = 60.dp

enum class ChatState { Collapsed, Expanded }

private const val SCRIM_MAX_ALPHA = 0.5f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AI(
    modifier: Modifier = Modifier,
    onMainPanelTopPxChanged: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val settingsRepository: SettingsRepository = remember { SettingsRepository(context) }
    val isAIEnabled by settingsRepository.isAIEnabled.collectAsState(initial = true)
    
    // Drag progress: 0 = collapsed, 1 = expanded
    var dragProgress by remember { mutableFloatStateOf(0f) }
    
    // Animated multiplier for AI toggle (smooth fade when AI is disabled)
    val aiMultiplier by animateFloatAsState(
        targetValue = if (isAIEnabled) 1f else 0f,
        animationSpec = tween(300),
        label = "scrimAiMultiplier"
    )
    
    // Final scrim alpha
    val scrimAlpha = dragProgress * SCRIM_MAX_ALPHA * aiMultiplier
    
    val density = LocalDensity.current
    val navigationBarHeightDp = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f) // Above tooltips (999f) to keep AI interactive
            .drawBehind {
                if (scrimAlpha > 0.001f) {
                    drawRect(Color.Black.copy(alpha = scrimAlpha))
                }
            }
    ) {
        // White area under navigation bar (covers scrim artifacts)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(navigationBarHeightDp)
                .align(Alignment.BottomCenter)
                .background(PixsoColors.Color_Bg_bg_surface)
        )
        
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
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
                    positionalThreshold = { distance: Float -> distance * 0.3f },
                    velocityThreshold = { with(density) { 125.dp.toPx() } },
                    snapAnimationSpec = tween(
                        durationMillis = 550,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)  // Slow end
                    ),
                    decayAnimationSpec = exponentialDecay(frictionMultiplier = 2f)
                ).apply {
                    updateAnchors(anchors)
                }
            }
            
            val currentOffsetPx = try {
                anchoredDraggableState.requireOffset()
            } catch (e: IllegalStateException) {
                collapsedOffsetPx
            }
            
            // Update drag progress for scrim
            dragProgress = if (collapsedOffsetPx != expandedOffsetPx) {
                ((collapsedOffsetPx - currentOffsetPx) / (collapsedOffsetPx - expandedOffsetPx)).coerceIn(0f, 1f)
            } else {
                0f
            }
            
            // Container for chat - extends under main panel to fill corner gaps
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = isAIEnabled,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                    ) {
                        AIChat(
                            modifier = Modifier.fillMaxSize(),
                            currentOffsetPx = currentOffsetPx,
                            screenHeightPx = screenHeightPx,
                            expandedTopOffsetPx = expandedOffsetPx,
                            mainPanelHeightPx = with(density) { MAIN_PANEL_HEIGHT.toPx() },
                            anchoredDraggableState = anchoredDraggableState
                        )
                    }
                }
            }
            
            AIMain(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coords ->
                        onMainPanelTopPxChanged?.invoke(coords.boundsInRoot().top)
                    },
                anchoredDraggableState = anchoredDraggableState
            )
        }
    }
}
