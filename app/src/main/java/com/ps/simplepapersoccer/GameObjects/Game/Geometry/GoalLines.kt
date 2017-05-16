package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction.IntegerLine

class GoalLines(var goalLine: IntegerLine, var startLine: IntegerLine) {
    fun contains(point: Point) : Boolean
    {
        return goalLine.contains(point) || startLine.contains(point)
    }
}
