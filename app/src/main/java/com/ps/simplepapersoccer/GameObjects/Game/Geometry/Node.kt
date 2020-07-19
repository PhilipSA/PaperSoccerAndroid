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

    override fun identifierHashCode(): Int {
        return nodeType.ordinal + if (containsBall) 10 else 0
    }

    fun pairMatchesType(other: Node, firstNodeType: NodeTypeEnum, otherNodeType: NodeTypeEnum): Boolean {
        return (nodeType == firstNodeType && other.nodeType == otherNodeType) || (nodeType == otherNodeType && other.nodeType == firstNodeType)
    }

    fun isDiagonalNeighbor(other: Node): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun addNeighbor(other: ConnectionNode) {
        neighbors.add(other)
    }

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }

    fun connectedNodeNeighbors(): HashSet<Node> {
        return neighbors.flatMap { connectionNode ->
            connectionNode.nonConnectedNodes.filter {
                (connectionNode.coords.x == coords.x || connectionNode.coords.y == coords.y) ||
                        (connectionNode.coords.x != coords.x && connectionNode.coords.y != coords.y && it.coords.x != coords.x && it.coords.y != coords.y)
            }
        }.filter { it != this }.toHashSet()
    }
}
