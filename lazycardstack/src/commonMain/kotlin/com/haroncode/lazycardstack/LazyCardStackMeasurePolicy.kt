package com.haroncode.lazycardstack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.round

/**
 * Creates and remembers a measure policy for the `LazyCardStack` composable.
 *
 * This measure policy is responsible for measuring and laying out the visible cards in the stack,
 * taking into account the current state, configuration options, and the provided item provider.
 *
 * @param state The `LazyCardStackState` object representing the current state of the card stack.
 * @param itemProviderLambda A lambda function that provides a `LazyLayoutItemProvider` for the card stack.
 * @param visibleImages The number of cards to display in the stack.
 * @param scaleFactor The scaling factor applied to cards behind the top card.
 * @param offsetXFactor The factor used to calculate the offset between cards.
 * @param stackPosition The position of the stacked cards relative to the top card.
 *
 * @return A composable function that takes `Constraints` as input and returns a `MeasureResult`,
 *         defining how the `LazyCardStack` should be measured and laid out within its parent.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
public fun rememberLazyCardStackMeasurePolicy(
    state: LazyCardStackState,
    itemProviderLambda: () -> LazyLayoutItemProvider,
    visibleImages: Int,
    scaleFactor: Float,
    offsetXFactor: Float,
    stackPosition: LazyCardStackPosition
): LazyLayoutMeasureScope.(Constraints) -> MeasureResult = remember(
    state,
    itemProviderLambda,
    visibleImages,
    scaleFactor,
    offsetXFactor,
    stackPosition
) {
    { containerConstraints ->
        val itemProvider = itemProviderLambda()
        val itemsCount = itemProvider.itemCount
        var firstVisibleItemIndex: Int

        Snapshot.withoutReadObservation {
            firstVisibleItemIndex = state.updateScrollPositionIfTheFirstItemWasDeleted(
                itemProvider,
                state.visibleItemIndex
            )
        }

        val childConstraints = Constraints(
            maxWidth = containerConstraints.maxWidth,
            maxHeight = containerConstraints.maxHeight
        )

        if (firstVisibleItemIndex >= itemsCount) {
            // the data set has been updated and now we have less items that we were
            // scrolled to before
            firstVisibleItemIndex = itemsCount - 1
        }

        val indexRange = when {
            firstVisibleItemIndex < itemsCount - (visibleImages - 1) -> firstVisibleItemIndex..firstVisibleItemIndex + (visibleImages - 1)
            firstVisibleItemIndex == itemsCount - (visibleImages - 1) -> firstVisibleItemIndex..< itemsCount
            else -> firstVisibleItemIndex..<itemsCount
        }

        val visibleItems = indexRange.mapIndexed { relativeIndex, itemIndex ->
            val placeables = measure(itemIndex, childConstraints)
            val key = itemProvider.getKey(itemIndex)

            LazyCardMeasuredItem(
                relativeIndex = relativeIndex,
                dragOffset = state.offset.round(),
                rotation = state.rotation,
                key = key,
                placeables = placeables,
                scale = if (relativeIndex == 0) 1f else scaleFactor,
                offsetXFactor = offsetXFactor,
                stackPosition = stackPosition
            )
        }

        val width = containerConstraints.maxWidth
        val height = containerConstraints.maxHeight

        state.premeasureConstraints = containerConstraints

        layout(width, height) {
            visibleItems.forEach { item -> item.place(this) }
            val measureResult = LazyCardStackMeasureResult(
                currentItem = visibleItems.firstOrNull(),
                itemCount = itemsCount
            )
            state.applyMeasureResult(measureResult)
        }
    }
}
