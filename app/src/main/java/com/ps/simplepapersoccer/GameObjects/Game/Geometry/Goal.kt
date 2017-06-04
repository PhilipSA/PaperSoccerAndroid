package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction.IntegerLine

class Goal(var goalLine: IntegerLine, var leftPost: IntegerLine, var rightPost: IntegerLine) {
    fun contains(point: Point) : Boolean
    {
        return goalLine.contains(point) || leftPost.contains(point) || rightPost.contains(point)
    }
    val height : Int get() = leftPost.length
    val width : Int get() = goalLine.length
    val allNodes: MutableList<Node> get() {
        var nodes: MutableList<Node> = mutableListOf<Node>()
        goalLine.allPoints.forEach {
            nodes.add(Node(it, NodeTypeEnum.Goal))
        }
        leftPost.allPoints.forEach {
            nodes.add(Node(it, NodeTypeEnum.Post))
        }
        rightPost.allPoints.forEach {
            nodes.add(Node(it, NodeTypeEnum.Post))
        }
        return nodes
    }
}
