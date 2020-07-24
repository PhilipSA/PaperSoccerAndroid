package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode

class Node(coords: TwoDimensionalPoint,
           var nodeType: NodeTypeEnum) : BaseNode(coords) {
    val neighbors: HashSet<ConnectionNode> = hashSetOf()
    var cameFrom: Node? = null
    var nodeValue: Double = 0.0
    var costSoFar: Double = 0.0
    var containsBall: Boolean = false

    val coordNeighbors: HashSet<Node> = hashSetOf()
    val connectedNodesMap: HashMap<Node, ConnectionNode> = hashMapOf()
    val openConnectionNodes get() = connectedNodesMap.filter { it.value.connectedNodes[ConnectionNode.NodeConnection(this, it.key)] == true }.keys

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

    fun createConnectedNodePair(node: Node, connectionNode: ConnectionNode) {
        this.connectedNodesMap[node] = connectionNode
        node.connectedNodesMap[this] = connectionNode
    }

    fun createCoordNeighborPair(node: Node) {
        this.coordNeighbors.add(node)
        node.coordNeighbors.add(this)
    }

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }
}
