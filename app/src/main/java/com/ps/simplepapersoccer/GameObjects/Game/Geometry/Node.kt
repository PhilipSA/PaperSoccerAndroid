package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode

class Node(coords: TwoDimensionalPoint,
           var nodeType: NodeTypeEnum): BaseNode(coords) {
    val neighbors: HashSet<ConnectionNode> = hashSetOf()
    var cameFrom: Node? = null
    var nodeValue: Double = 0.0
    var costSoFar: Double = 0.0
    var containsBall: Boolean = false

    val coordNeighbors: HashSet<Node> get() = getNodeNeighbors(false)

    override val getVisibleCoords: TwoDimensionalPoint
        get() = TwoDimensionalPoint(coords.x / 2, coords.y / 2)

    override fun normalizedIdentifierHashCode(): Double {
        return nodeType.normalizedIdentiferValue + if (containsBall) 0.05 else 0.0
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

    fun getNodeNeighbors(filterOpenConnection: Boolean = true): HashSet<Node> {
        return neighbors.flatMap { connectionNode ->
            connectionNode.connectedNodes.filter { (it.node1 == this || it.node2 == this) && if (filterOpenConnection) it.openConnection else true }.map { if (it.node1 == this) it.node2 else it.node1 }
        }.toHashSet()
    }
}
