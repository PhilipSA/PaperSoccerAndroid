package com.ps.simplepapersoccer.helpers

import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint

object MathHelper {
    fun euclideanDistance(point: TwoDimensionalPoint, otherPoint: TwoDimensionalPoint): Double {
        val ycoord = Math.abs(point.y - otherPoint.y)
        val xcoord = Math.abs(point.x - otherPoint.x)
        val distance = Math.sqrt((ycoord * ycoord + xcoord * xcoord).toDouble())
        return distance
    }
}
