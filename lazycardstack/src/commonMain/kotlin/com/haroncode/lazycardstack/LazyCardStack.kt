package com.haroncode.lazycardstack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ThresholdConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haroncode.lazycardstack.swiper.SwipeDirection
import com.haroncode.lazycardstack.swiper.swiper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Deprecated("Use LazyCardStack without ThresholdConfig, due to it deprecated")
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun LazyCardStack(
    modifier: Modifier = Modifier,
    threshold: (Orientation) -> ThresholdConfig = { FractionalThreshold(0.3f) },
    velocityThreshold: Dp = 125.dp,
    directions: Set<SwipeDirection> = setOf(SwipeDirection.Left, SwipeDirection.Right),
    state: LazyCardStackState = rememberLazyCardStackState(),
    onSwipedItem: (Int, SwipeDirection) -> Unit = { _, _ -> },
    content: LazyCardStackScope.() -> Unit
) {
    LazyCardStack(
        modifier = modifier,
        directions = directions,
        state = state,
        onSwipedItem = onSwipedItem,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun LazyCardStack(
    modifier: Modifier = Modifier,
    directions: Set<SwipeDirection> = setOf(SwipeDirection.Left, SwipeDirection.Right),
    state: LazyCardStackState = rememberLazyCardStackState(),
    onSwipedItem: (Int, SwipeDirection) -> Unit = { _, _ -> },
    content: LazyCardStackScope.() -> Unit
) {
    val itemProviderLambda = rememberLazyCardStackItemProviderLambda(state, content)
    val measurePolicy = rememberLazyCardStackMeasurePolicy(state, itemProviderLambda)
    val scope = rememberCoroutineScope()
    LazyLayout(
        itemProvider = itemProviderLambda,
        modifier = Modifier
            .then(state.remeasurementModifier)
            .then(state.awaitLayoutModifier)
            .swiper(
                state = state.swiperState,
                directions = directions,
                onSwiped = { direction ->
                    val currentIndex = state.visibleItemIndex

                    when (direction) {
                        SwipeDirection.Left,
                        SwipeDirection.Up,
                        SwipeDirection.Down -> onNextItem(scope, state, currentIndex)
                        SwipeDirection.Right -> onReturnPreviousItem(scope, state, currentIndex)
                    }

                    onSwipedItem(currentIndex, direction)
                }
            )
            .then(modifier),
        measurePolicy = measurePolicy
    )
}

/**
 * Snaps the visible card to the next item in the stack.
 *
 * @param scope The coroutine scope used for asynchronous operations.
 * @param state The state of the lazy stack.
 * @param currentIndex The index of the currently visible item.
 */
public fun onNextItem(scope: CoroutineScope, state: LazyCardStackState, currentIndex: Int) {
    scope.launch {
        state.snapTo(currentIndex + 1)
    }
}

/**
 * Snaps the visible card to the previous item in the stack.

 * @param scope The coroutine scope used for asynchronous operations.
 * @param state The state of the lazy stack.
 * @param currentIndex The index of the currently visible item.
 */
public fun onReturnPreviousItem(scope: CoroutineScope, state: LazyCardStackState, currentIndex: Int) {
    scope.launch {
        // Ensure currentIndex is within the valid range
        val newIndex = maxOf(currentIndex - 1, 0)
        state.snapTo(newIndex)
    }
}
