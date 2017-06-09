package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction.IntegerLine

class Goal(var goalLine: IntegerLine, var leftPost: IntegerLine, var rightPost: IntegerLine) {
    fun contains(point: Point) : Boolean
    {
        return goalLine.contains(point) || leftPost.contains(point) || rightPost.contains(point) || outerGoalLine.contains(point)
    }
    val height : Int get() = leftPost.length
    val width : Int get() = goalLine.length
    val outerGoalLine: IntegerLine get() = IntegerLine(leftPost.toPoint, rightPost.toPoint)
    val allNodes: MutableList<Node>
    init {
        var nodes: MutableList<Node> = mutableListOf<Node>()
        goalLine.allPoints.forEach {
            nodes.add(Node(it, NodeTypeEnum.Goal))
        }
        leftPost.allPoints.forEach {
            if (!goalLine.contains(it)) nodes.add(Node(it, NodeTypeEnum.Post))
        }
        rightPost.allPoints.forEach {
            if (!goalLine.contains(it)) nodes.add(Node(it, NodeTypeEnum.Post))
        }
        outerGoalLine.allPoints.forEach {
            if (!leftPost.contains(it) && !rightPost.contains(it)) nodes.add(Node(it, NodeTypeEnum.Empty))
        }
        allNodes = nodes
    }
}
