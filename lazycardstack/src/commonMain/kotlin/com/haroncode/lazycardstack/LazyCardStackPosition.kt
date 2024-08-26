package com.haroncode.lazycardstack

/**
 * Represents the possible positions for stacked cards relative to the top card in a `LazyCardStack`.
 *
 * @property TOP  Stacked cards are positioned above the top card.
 * @property LEFT Stacked cards are positioned to the left of the top card.
 * @property RIGHT Stacked cards are positioned to the right of the top card.
 * @property BOTTOM Stacked cards are positioned below the top card.
 */
public enum class LazyCardStackPosition {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM
}