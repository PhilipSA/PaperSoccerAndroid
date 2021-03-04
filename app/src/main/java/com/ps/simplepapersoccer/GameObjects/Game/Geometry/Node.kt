package com.ps.simplepapersoccer.gameobjects.game.geometry

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameobjects.game.geometry.Abstraction.BaseNode

class Node(coords: TwoDimensionalPoint,
           var nodeType: NodeTypeEnum) : BaseNode(coords) {
    var cameFrom: Node? = null
    var nodeValue: Double = 0.0
    var costSoFar: Double = 0.0
    var containsBall: Boolean = false

    val coordNeighbors: HashSet<Node> = hashSetOf()
    val connectedNodesMap: HashMap<Node, ConnectionNode> = hashMapOf()
    val openConnectionNodes = hashSetOf<Node>()

    override val getVisibleCoords: TwoDimensionalPoint
        get() = TwoDimensionalPoint(coords.x / 2, coords.y / 2)

    override fun normalizedIdentifierHashCode(): Float {
        return nodeType.normalizedIdentiferValue + if (containsBall) 0.01f else 0f
    }

    fun pairMatchesType(other: Node, firstNodeType: NodeTypeEnum, otherNodeType: NodeTypeEnum): Boolean {
        return (nodeType == firstNodeType && other.nodeType == otherNodeType) || (nodeType == otherNodeType && other.nodeType == firstNodeType)
    }

    fun isDiagonalNeighbor(other: BaseNode): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun createConnectedNodePair(node: Node, connectionNode: ConnectionNode) {
        this.connectedNodesMap[node] = connectionNode
        node.connectedNodesMap[this] = connectionNode

        node.openConnectionNodes.add(this)
        this.openConnectionNodes.add(node)
    }

    fun createCoordNeighborPair(node: Node) {
        this.coordNeighbors.add(node)
        node.coordNeighbors.add(this)
    }

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }
}
