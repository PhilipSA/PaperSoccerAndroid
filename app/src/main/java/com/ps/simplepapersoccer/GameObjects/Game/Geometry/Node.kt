package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode

class Node(coords: TwoDimensionalPoint,
           var nodeType: NodeTypeEnum): BaseNode(coords) {
    val neighbors: HashSet<ConnectionNode> = hashSetOf()
    val coordNeighbors: HashSet<Node> = hashSetOf()
    var cameFrom: Node? = null
    var nodeValue: Double = 0.0
    var costSoFar: Double = 0.0
    var containsBall: Boolean = false

    override val getVisibleCoords: TwoDimensionalPoint
        get() = TwoDimensionalPoint(coords.x / 2, coords.y / 2)

    override fun identifierHashCode(): Int {
        return nodeType.ordinal + if (containsBall) 10 else 0
    }

    fun pairMatchesType(other: Node, firstNodeType: NodeTypeEnum, otherNodeType: NodeTypeEnum): Boolean {
        return (nodeType == firstNodeType && other.nodeType == otherNodeType) || (nodeType == otherNodeType && other.nodeType == firstNodeType)
    }

    fun isDiagonalNeighbor(other: BaseNode): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun addNeighbor(other: ConnectionNode) {
        neighbors.add(other)
    }

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }

    fun openConnectionNodeNeighbors(): HashSet<Node> {
        return neighbors.flatMap { connectionNode ->
            connectionNode.connectedNodes.filter { (it.node1 == this || it.node2 == this) && it.openConnection }.map { if (it.node1 == this) it.node2 else it.node2 }
        }.filter { it != this }.toHashSet()
    }
}
