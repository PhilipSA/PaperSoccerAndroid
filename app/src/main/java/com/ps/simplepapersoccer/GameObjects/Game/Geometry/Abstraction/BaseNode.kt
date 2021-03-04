package com.ps.simplepapersoccer.gameobjects.game.geometry.Abstraction

import com.ps.simplepapersoccer.gameobjects.game.geometry.TwoDimensionalPoint

abstract class BaseNode(val coords: TwoDimensionalPoint) {
    abstract fun normalizedIdentifierHashCode(): Float

    abstract val getVisibleCoords: TwoDimensionalPoint
}