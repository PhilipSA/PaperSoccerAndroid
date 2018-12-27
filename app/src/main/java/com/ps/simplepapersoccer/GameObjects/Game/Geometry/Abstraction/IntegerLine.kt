package com.ps.simplepapersoccer.gameObjects.game.geometry.Abstraction

import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.helpers.MathHelper.euclideanDistance

class IntegerLine(var fromPoint: TwoDimensionalPoint, var toPoint: TwoDimensionalPoint) {
    var allPoints: MutableList<TwoDimensionalPoint> = mutableListOf()
    val length: Int get() = euclideanDistance(fromPoint, toPoint).toInt()

    init {
        var x = fromPoint.x
        var y = fromPoint.y
        val w = toPoint.x - fromPoint.x
        val h = toPoint.y - fromPoint.y
        var dx1 = 0
        var dy1 = 0
        var dx2 = 0
        var dy2 = 0
        if (w < 0) dx1 = -1 else if (w > 0) dx1 = 1
        if (h < 0) dy1 = -1 else if (h > 0) dy1 = 1
        if (w < 0) dx2 = -1 else if (w > 0) dx2 = 1
        var longest = Math.abs(w)
        var shortest = Math.abs(h)
        if (longest <= shortest) {
            longest = Math.abs(h)
            shortest = Math.abs(w)
            if (h < 0) dy2 = -1 else if (h > 0) dy2 = 1
            dx2 = 0
        }
        var numerator = longest shr 1
        for (i in 0..longest) {
            allPoints.add(TwoDimensionalPoint(x, y))
            numerator += shortest
            if (numerator >= longest) {
                numerator -= longest
                x += dx1
                y += dy1
            } else {
                x += dx2
                y += dy2
            }
        }
    }

    fun contains(point: TwoDimensionalPoint) : Boolean
    {
        return allPoints.find { x -> x.x == point.x && x.y == point.y } != null
    }
}