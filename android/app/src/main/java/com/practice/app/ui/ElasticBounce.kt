package com.practice.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Whole-screen rubber-band bounce for mindless thumbing.
 * Uses [scrollable] so vertical drags stretch the page, then spring back —
 * taps on buttons/cards still work (scroll waits for touch slop).
 */
fun Modifier.elasticBounce(
    maxOverscroll: Dp = 80.dp,
    springDampingRatio: Float = 0.62f,
    springStiffness: Float = 1_400f,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val overscroll = remember { Animatable(0f) }
    val maxPx = with(LocalDensity.current) { maxOverscroll.toPx() }
    val rawDrag = remember { floatArrayOf(0f) }

    val scrollState = rememberScrollableState { delta ->
        rawDrag[0] += delta
        val next = rubberBand(rawDrag[0], maxPx)
        scope.launch { overscroll.snapTo(next) }
        delta
    }

    LaunchedEffect(scrollState, maxPx, springDampingRatio, springStiffness) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { inProgress ->
                if (!inProgress && (overscroll.value != 0f || rawDrag[0] != 0f)) {
                    rawDrag[0] = 0f
                    overscroll.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness,
                        ),
                    )
                }
            }
    }

    // No post-fling coast — spring back the moment the thumb lifts.
    val noFling = remember {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float = 0f
        }
    }

    this
        .graphicsLayer { translationY = overscroll.value }
        .scrollable(
            orientation = Orientation.Vertical,
            state = scrollState,
            flingBehavior = noFling,
        )
}

/**
 * Map finger travel into a soft elastic range (±limit).
 * Pulling farther yields diminishing movement.
 */
private fun rubberBand(offset: Float, limit: Float): Float {
    if (offset == 0f || limit <= 0f) return 0f
    val direction = sign(offset)
    val x = abs(offset)
    return direction * limit * (1f - 1f / (x / limit + 1f))
}
