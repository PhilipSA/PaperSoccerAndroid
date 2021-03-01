package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode
import kotlin.math.atan2

//Keeps track of how other nodes are connected
class ConnectionNode(coords: TwoDimensionalPoint) : BaseNode(coords) {

    data class NodeConnection(val node1: Node, val node2: Node, var openConnection: Boolean)

    val neighbors: HashSet<Node> = hashSetOf()
    val connectedNodes = HashMap<Int, NodeConnection>()

    override val getVisibleCoords: TwoDimensionalPoint = coords

    val connectionTypeEnum: ConnectionTypeEnum
        get() {
            val openConnections = connectedNodes.filter { it.value.openConnection }
            return when (openConnections.size) {
                2 -> ConnectionTypeEnum.Open
                0 -> ConnectionTypeEnum.Blocked
                1 -> {
                    val openConnection = openConnections.values
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

    override fun normalizedIdentifierHashCode(): Int {
        return connectionTypeEnum.normalizedIdentiferValue
    }

    fun connectNodes(node: Node, node2: Node) {
        connectedNodes[getNodeConnection(node, node2)]?.openConnection = true

        node.openConnectionNodes.add(node2)
        node2.openConnectionNodes.add(node)
    }

    fun disconnectNode(node: Node, node2: Node) {
        connectedNodes[getNodeConnection(node, node2)]?.openConnection = false

        node.openConnectionNodes.remove(node2)
        node2.openConnectionNodes.remove(node)
    }

    fun getNodeConnection(node1: Node, node2: Node): Int {
        return node1.hashCode().and(node2.hashCode())
    }

    private fun initNodeConnection(firstNode: Node, secondNode: Node, openConnection: Boolean) {
        connectedNodes[getNodeConnection(firstNode, secondNode)] = NodeConnection(firstNode, secondNode, openConnection)
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