package com.ps.simplepapersoccer.helpers

import android.graphics.Point


object MathHelper {
    fun euclideanDistance(point: Point, otherPoint: Point): Double {
        val ycoord = Math.abs(point.y - otherPoint.y)
        val xcoord = Math.abs(point.x - otherPoint.x)
        val distance = Math.sqrt((ycoord * ycoord + xcoord * xcoord).toDouble())
        return distance
    }

    fun distance(point: Point, otherPoint: Point): Double {
        val dx = Math.abs(otherPoint.x - point.x)
        val dy = Math.abs(otherPoint.y - point.y)

        val min = Math.min(dx, dy)
        val max = Math.max(dx, dy)

        val diagonalSteps = min
        val straightSteps = max - min

        return Math.sqrt(2.0) * diagonalSteps + straightSteps
    }
}
