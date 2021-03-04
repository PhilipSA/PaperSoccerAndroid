package com.ps.simplepapersoccer.gameobjects.game.geometry.Abstraction

import com.ps.simplepapersoccer.gameobjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.helpers.MathHelper.euclideanDistance
import kotlin.math.abs

class IntegerLine(val fromPoint: TwoDimensionalPoint, val toPoint: TwoDimensionalPoint) {
    var allPoints: HashSet<TwoDimensionalPoint> = hashSetOf()
    val length = euclideanDistance(fromPoint, toPoint).toInt()

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
        var longest = abs(w)
        var shortest = abs(h)
        if (longest <= shortest) {
            longest = abs(h)
            shortest = abs(w)
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