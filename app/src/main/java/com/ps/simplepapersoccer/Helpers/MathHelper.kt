package com.ps.simplepapersoccer.Helpers

object MathHelper {
    fun euclideanDistance(x1: Int, x2: Int, y1: Int, y2: Int): Double {
        val ycoord = Math.abs(y1 - y2)
        val xcoord = Math.abs(x1 - x2)
        val distance = Math.sqrt((ycoord * ycoord + xcoord * xcoord).toDouble())
        return distance
    }

    fun distance(x1: Int, x2: Int, y1: Int, y2: Int): Double {
        val dx = Math.abs(x2 - x1)
        val dy = Math.abs(y2 - y1)

        val min = Math.min(dx, dy)
        val max = Math.max(dx, dy)

        val diagonalSteps = min
        val straightSteps = max - min

        return Math.sqrt(2.0) * diagonalSteps + straightSteps
    }
}
