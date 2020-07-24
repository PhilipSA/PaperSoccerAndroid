package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode
import kotlin.math.atan2

//Keeps track of how other nodes are connected
class ConnectionNode(coords: TwoDimensionalPoint) : BaseNode(coords) {

    data class NodeConnection(val node1: Node, val node2: Node) {
        override fun equals(other: Any?): Boolean {
            val otherNodeConnection = other as NodeConnection
            return (this.node1 == otherNodeConnection.node1 && otherNodeConnection.node2 == node2)
                    || (otherNodeConnection.node1 == node2 && otherNodeConnection.node2 == node1)
        }

        override fun hashCode(): Int {
            return node1.hashCode().and(node2.hashCode())
        }
    }

    val neighbors: HashSet<Node> = hashSetOf()
    val connectedNodes = HashMap<NodeConnection, Boolean>()

    override val getVisibleCoords: TwoDimensionalPoint
        get() = coords

    val connectionTypeEnum: ConnectionTypeEnum
        get() {
            return when (connectedNodes.filter { it.value }.size) {
                2 -> ConnectionTypeEnum.Open
                0 -> ConnectionTypeEnum.Blocked
                1 -> {
                    val openConnection = connectedNodes.filter { it.value }.keys
                    val node1 = openConnection.first().node1
                    val node2 = openConnection.first().node2

                    val deltaY = node1.coords.y - node2.coords.y
                    val deltaX = node2.coords.x - node1.coords.x
                    val result = Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble()));

                    when {
                        node1.coords.y == node2.coords.y -> ConnectionTypeEnum.Open
                        node1.coords.x == node2.coords.x -> ConnectionTypeEnum.Open
                        result == 45.0 || result == -135.0 -> ConnectionTypeEnum.LineTiltedLeft
                        result == -45.0 || result == 135.0 -> ConnectionTypeEnum.LineTiltedRight
                        else -> ConnectionTypeEnum.Blocked
                    }
                }
                else -> ConnectionTypeEnum.Blocked
            }
        }

    override fun normalizedIdentifierHashCode(): Double {
        return connectionTypeEnum.normalizedIdentiferValue
    }

    fun connectNodes(node: Node, node2: Node) {
        connectedNodes[getNodeConnection(node, node2)] = true
    }

    fun disconnectNode(node: Node, node2: Node) {
        connectedNodes[getNodeConnection(node, node2)] = false
    }

    fun getNodeConnection(node1: Node, node2: Node): NodeConnection {
        return NodeConnection(node1, node2)
    }

    private fun initNodeConnection(firstNode: Node, secondNode: Node, openConnection: Boolean) {
        connectedNodes.put(NodeConnection(firstNode, secondNode), openConnection)
        firstNode.createCoordNeighborPair(secondNode)

        if (openConnection) {
            firstNode.createConnectedNodePair(secondNode, this)
        }
    }

    fun createNodeConnections() {
        for (firstNode in neighbors) {
            for (secondNode in neighbors) {
                if (firstNode == secondNode) continue
                if (connectedNodes[getNodeConnection(firstNode, secondNode)] != null) continue
                if (firstNode.isDiagonalNeighbor(this) && secondNode.isDiagonalNeighbor(this) && firstNode.isDiagonalNeighbor(secondNode).not()) continue

                if (firstNode.pairMatchesType(secondNode, NodeTypeEnum.Wall, NodeTypeEnum.Wall)) {
                    initNodeConnection(firstNode, secondNode, firstNode.isDiagonalNeighbor(secondNode))
                } else if (firstNode.pairMatchesType(secondNode, NodeTypeEnum.Post, NodeTypeEnum.Wall) ||
                        firstNode.pairMatchesType(secondNode, NodeTypeEnum.Wall, NodeTypeEnum.Goal) ||
                        firstNode.pairMatchesType(secondNode, NodeTypeEnum.Goal, NodeTypeEnum.Goal) ||
                        (firstNode.pairMatchesType(secondNode, NodeTypeEnum.Post, NodeTypeEnum.Goal) && firstNode.isDiagonalNeighbor(secondNode).not()) ) {
                    initNodeConnection(firstNode, secondNode, false)
                } else {
                    initNodeConnection(firstNode, secondNode, true)
                }
            }
        }
    }
}