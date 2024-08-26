package com.haroncode.lazycardstack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.haroncode.lazycardstack.swiper.SwipeDirection
import com.haroncode.lazycardstack.swiper.swiper
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun LazyCardStack(
    modifier: Modifier = Modifier,
    directions: Set<SwipeDirection> = setOf(SwipeDirection.Left, SwipeDirection.Right),
    state: LazyCardStackState = rememberLazyCardStackState(),
    config: LazyCardStackConfig = LazyCardStackConfig(),
    onSwipedItem: (Int, SwipeDirection) -> Unit = { _, _ -> },
    content: LazyCardStackScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val itemProviderLambda = rememberLazyCardStackItemProviderLambda(state, content)
    val measurePolicy = rememberLazyCardStackMeasurePolicy(
        state,
        itemProviderLambda,
        config.visibleCards,
        config.scaleFactor,
        config.offsetXFactor,
        config.stackPosition
    )

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
                    scope.launch {
                        state.snapTo(currentIndex + 1)
                        onSwipedItem(currentIndex, direction)
                    }
                }
            )
            .then(modifier),
        measurePolicy = measurePolicy
    )
}

/**
 * Configuration options for the `LazyCardStack` composable.
 *
 * @param visibleCards The number of cards to display in the stack. Default is 3.
 * @param scaleFactor The scaling factor applied to cards behind the top card.
 *                    Values less than 1.0 create a 3D effect. Default is 0.95f.
 * @param offsetXFactor The factor used to calculate the horizontal or vertical offset between cards,
 *                      depending on the `stackPosition`. Higher values increase the spacing.
 *                      Default is 0.03f.
 * @param stackPosition The position where the stacked cards will be displayed relative to the top card.
 *                      Default is `LazyCardStackPosition.RIGHT`.
 */
public data class LazyCardStackConfig(
    val visibleCards: Int = 3,
    val scaleFactor: Float = 0.95f,
    val offsetXFactor: Float = 0.03f,
    val stackPosition: LazyCardStackPosition = LazyCardStackPosition.RIGHT,
)