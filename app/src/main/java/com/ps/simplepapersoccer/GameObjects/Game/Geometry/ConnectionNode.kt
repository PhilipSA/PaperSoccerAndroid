package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode

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
                    val node1 = connectedNodes.first().node1
                    val node2 = connectedNodes.first().node2
                    when {
                        node1.coords.y == node2.coords.y -> ConnectionTypeEnum.Open
                        node1.coords.x == node2.coords.x -> ConnectionTypeEnum.Open
                        node1.coords.x < node2.coords.x -> ConnectionTypeEnum.LineTiltedLeft
                        node1.coords.x > node2.coords.x -> ConnectionTypeEnum.LineTiltedRight
                        else -> ConnectionTypeEnum.Blocked
                    }
                }
                else -> ConnectionTypeEnum.Blocked
            }
        }

    override fun identifierHashCode(): Int {
        return connectionTypeEnum.identiferValue
    }

    fun connectNodes(node: Node, node2: Node) {
        connectedNodes.firstOrNull { it.node1 == node && it.node2 == node2 }?.openConnection = true
    }

    fun disconnectNode(node: Node, node2: Node) {
        connectedNodes.firstOrNull { it.node1 == node && it.node2 == node2 }?.openConnection = false
    }

    private fun hasNodeConnection(node1: Node, node2: Node): Boolean {
        return connectedNodes.any {
            (it.node1 == node1 && it.node2 == node2) || (it.node1 == node2 && it.node2 == node1)
        }
    }

    fun createNodeConnections() {
        for (firstNode in neighbors) {
            for (secondNode in neighbors) {
                if (firstNode == secondNode) continue
                if (hasNodeConnection(firstNode, secondNode)) continue
                if (firstNode.isDiagonalNeighbor(this) && secondNode.isDiagonalNeighbor(this) && firstNode.isDiagonalNeighbor(secondNode).not()) continue

                if (firstNode.pairMatchesType(secondNode, NodeTypeEnum.Wall, NodeTypeEnum.Wall)) {
                  connectedNodes.add(NodeConnection(firstNode, secondNode, firstNode.isDiagonalNeighbor(secondNode)))
                } else if (firstNode.pairMatchesType(secondNode, NodeTypeEnum.Post, NodeTypeEnum.Wall) ||
                        firstNode.pairMatchesType(secondNode, NodeTypeEnum.Wall, NodeTypeEnum.Post) ||
                        firstNode.pairMatchesType(secondNode, NodeTypeEnum.Wall, NodeTypeEnum.Goal)) {
                    connectedNodes.add(NodeConnection(firstNode, secondNode, false))
                } else {
                    connectedNodes.add(NodeConnection(firstNode, secondNode, true))
                }
            }
        }
    }
}