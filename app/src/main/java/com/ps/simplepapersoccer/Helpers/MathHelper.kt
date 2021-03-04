package com.ps.simplepapersoccer.helpers

import com.ps.simplepapersoccer.gameobjects.game.geometry.TwoDimensionalPoint
import kotlin.math.abs
import kotlin.math.sqrt

object MathHelper {
    fun euclideanDistance(point: TwoDimensionalPoint, otherPoint: TwoDimensionalPoint): Float {
        val yCord = abs(point.y - otherPoint.y)
        val xCord = abs(point.x - otherPoint.x)
        return sqrt((yCord * yCord + xCord * xCord).toFloat())
    }
}
