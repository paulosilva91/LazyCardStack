package com.haroncode.lazycardstack

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Represents a measured item within a `LazyCardStack`.
 *
 * This class encapsulates the information needed to place and arrange individual cards
 * within the stack, including their position, scale, rotation, and other layout-related properties.
 *
 * @param key A unique identifier for the item.
 * @param relativeIndex The item's position relative to other visible items in the stack.
 *                      0 indicates the topmost card.
 * @param dragOffset The offset applied to the top card due to user dragging.
 * @param scale The scaling factor applied to the item. 1.0 represents the original size.
 * @param rotation The rotation angle (in degrees) applied to the item.
 * @param placeables A list of `Placeable` objects representing the measured layout of the item's content.
 * @param offsetXFactor The factor used to calculate the horizontal or vertical offset between cards.
 * @param stackPosition The position of the stacked cards relative to the top card.
 */
internal class LazyCardMeasuredItem(
    val key: Any,
    private val relativeIndex: Int,
    private val dragOffset: IntOffset,
    private val scale: Float,
    private val rotation: Float,
    private val placeables: List<Placeable>,
    private val offsetXFactor: Float,
    private val stackPosition: LazyCardStackPosition
) {
    fun place(scope: Placeable.PlacementScope) = with(scope) {
        placeables.forEach { placeable ->
            if (relativeIndex == 0) {
                val isDragEnabled = (placeable.parentData as? DragableEnabledParentData)?.isEnabled ?: true
                val offset = if (isDragEnabled) dragOffset else IntOffset.Zero
                val rotation = if (isDragEnabled) rotation else 0.0f
                placeable.placeRelativeWithLayer(offset, zIndex = 1.0f) { rotationZ = rotation }
            } else {
                val (offsetX, offsetY) = calculateOffset(placeable, relativeIndex, offsetXFactor, stackPosition)

                placeable.placeRelativeWithLayer(
                    IntOffset(offsetX, offsetY),
                    zIndex = -relativeIndex.toFloat()
                ) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = -placeable.width / 2f * (1 - scale)
                        translationY = -placeable.height / 2f * (1 - scale)
                    }
                }
            }
        }
    }

    /**
     * Calculates the horizontal and vertical offset for a card based on its position in the stack,
     * the desired stack position, and the offset factor.
     *
     * @param placeable The `Placeable` object representing the measured layout of the card.
     * @param relativeIndex The card's position relative to other visible items in the stack.
     *                      0 indicates the topmost card.
     * @param offsetXFactor The factor used to calculate the offset. Higher values increase the spacing between cards.
     * @param stackPosition The position where the stacked cards will be displayed relative to the top card.
     *
     * @return A `Pair` representing the calculated horizontal and vertical offsets (`offsetX`, `offsetY`).
     */
    private fun calculateOffset(
        placeable: Placeable,
        relativeIndex: Int,
        offsetXFactor: Float,
        stackPosition: LazyCardStackPosition
    ): Pair<Int, Int> {
        return when (stackPosition) {
            LazyCardStackPosition.LEFT -> {
                val offsetX = -((placeable.width * relativeIndex) * offsetXFactor).roundToInt()
                offsetX to 0
            }
            LazyCardStackPosition.TOP -> {
                val offsetY = -((placeable.height * relativeIndex) * offsetXFactor * 0.8f).roundToInt()
                0 to offsetY
            }
            LazyCardStackPosition.RIGHT -> {
                val offsetX = ((placeable.width * relativeIndex) * offsetXFactor).roundToInt()
                offsetX to 0
            }
            LazyCardStackPosition.BOTTOM -> {
                val offsetY = ((placeable.height * relativeIndex) * offsetXFactor * 0.8f).roundToInt()
                0 to offsetY
            }
        }
    }
}

internal class LazyCardStackMeasureResult(
    val currentItem: LazyCardMeasuredItem?,
    val itemCount: Int,
)
