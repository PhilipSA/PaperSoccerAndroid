package com.ps.simplepapersoccer.gameobjects.game.geometry

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameobjects.game.geometry.abstraction.BaseNode
import com.ps.simplepapersoccer.gameobjects.game.geometry.abstraction.IntegerLine

class Goal(var goalLine: IntegerLine, var leftPost: IntegerLine, var rightPost: IntegerLine, var connectionLine: IntegerLine) {
    fun contains(point: TwoDimensionalPoint) : Boolean
    {
        return goalLine.contains(point) || leftPost.contains(point) || rightPost.contains(point) || outerGoalLine.contains(point)
    }
    private val outerGoalLine: IntegerLine get() = IntegerLine(leftPost.toPoint, rightPost.toPoint)
    val allNodes: HashSet<BaseNode> = hashSetOf()
    val middleGoalNode: Node

    fun isGoalNode(node: Node): Boolean {
        if (node.nodeType == NodeTypeEnum.Goal) return allNodes.contains(node)
        return false
    }

    init {
        for (it in goalLine.allPoints) {
            if (it.x % 2 == 1) allNodes.add(ConnectionNode(it))
            else allNodes.add(Node(it, NodeTypeEnum.Goal))
        }
        for (it in connectionLine.allPoints) {
            allNodes.add(ConnectionNode(it))
        }
        leftPost.allPoints.forEach {
            if (!goalLine.contains(it) && connectionLine.contains(it).not()) allNodes.add(Node(it, NodeTypeEnum.Post))
        }
        rightPost.allPoints.forEach {
            if (!goalLine.contains(it) && connectionLine.contains(it).not()) allNodes.add(Node(it, NodeTypeEnum.Post))
        }
        outerGoalLine.allPoints.forEach {
            if (!leftPost.contains(it) && !rightPost.contains(it)) {
                if (it.x % 2 == 1) allNodes.add(ConnectionNode(it))
                else allNodes.add(Node(it, NodeTypeEnum.Empty))
            }
        }
        middleGoalNode = allNodes.filter { x -> x is Node && x.nodeType == NodeTypeEnum.Goal }[1] as Node
    }
}
