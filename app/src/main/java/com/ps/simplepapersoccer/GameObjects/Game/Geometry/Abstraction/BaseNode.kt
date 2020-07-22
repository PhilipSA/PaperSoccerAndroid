package com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction

import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint

abstract class BaseNode(val coords: TwoDimensionalPoint) {
    abstract fun normalizedIdentifierHashCode(): Double

    abstract val getVisibleCoords: TwoDimensionalPoint
}