package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode
import kotlin.math.atan2

//Keeps track of how other nodes are connected
class ConnectionNode(coords: TwoDimensionalPoint) : BaseNode(coords) {

    data class NodeConnection(val node1: Node, val node2: Node, var openConnection: Boolean)

    val neighbors: HashSet<Node> = hashSetOf()
    val connectedNodes = hashSetOf<NodeConnection>()

    override val getVisibleCoords: TwoDimensionalPoint
        get() = coords

    val connectionTypeEnum: ConnectionTypeEnum
        get() {
            return when (connectedNodes.filter { it.openConnection }.size) {
                2 -> ConnectionTypeEnum.Open
                0 -> ConnectionTypeEnum.Blocked
                1 -> {
                    val openConnection = connectedNodes.filter { it.openConnection }
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
        getNodeConnection(node, node2)?.openConnection = true
        node.connectedNodes.add(node2)
        node2.connectedNodes.add(node)
    }

    fun disconnectNode(node: Node, node2: Node) {
        getNodeConnection(node, node2)?.openConnection = false
        node.connectedNodes.remove(node2)
        node2.connectedNodes.remove(node)
    }

    fun getNodeConnection(node1: Node, node2: Node): NodeConnection? {
        return connectedNodes.firstOrNull {
            (it.node1 == node1 && it.node2 == node2) || (it.node1 == node2 && it.node2 == node1)
        }
    }

    private fun initNodeConnection(firstNode: Node, secondNode: Node, openConnection: Boolean) {
        connectedNodes.add(NodeConnection(firstNode, secondNode, openConnection))
        firstNode.createCoordNeighborPair(secondNode)

        if (openConnection) {
            firstNode.createConnectedNodePair(secondNode)
        }
    }

    fun createNodeConnections() {
        for (firstNode in neighbors) {
            for (secondNode in neighbors) {
                if (firstNode == secondNode) continue
                if (getNodeConnection(firstNode, secondNode) != null) continue
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