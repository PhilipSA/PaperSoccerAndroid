package com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction

import android.graphics.Point
import java.lang.Math.abs

class IntegerLine(var fromPoint: Point, var toPoint: Point) {
    fun contains(point: Point) : Boolean
    {
        //C = arg
        var crossproduct = (point.y - fromPoint.y) * (toPoint.x - fromPoint.x) - (point.x - fromPoint.x) * (toPoint.y - fromPoint.y)
        if (abs(crossproduct) != 0) return false   // (or != 0 if using integers)

        var dotproduct = (point.x - fromPoint.x) * (toPoint.x - fromPoint.x) + (point.y - fromPoint.y)*(toPoint.y - fromPoint.y)
        if (dotproduct < 0) return false

        var squaredlengthba = (toPoint.x - fromPoint.x)*(toPoint.x - fromPoint.x) + (toPoint.y - fromPoint.y)*(toPoint.y - fromPoint.y)
        if (dotproduct > squaredlengthba) return false

        return true
    }
}