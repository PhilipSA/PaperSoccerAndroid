package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode

//Keeps track of how other nodes are connected
class ConnectionNode(coords: TwoDimensionalPoint) : BaseNode(coords) {

    //All nodes that does not have line going through this node
    val nonConnectedNodes = mutableListOf<Node>()

    val connectionTypeEnum: ConnectionTypeEnum
        get() {
            return when (nonConnectedNodes.size) {
                4 -> ConnectionTypeEnum.Open
                0 -> ConnectionTypeEnum.Blocked
                2 -> {
                    val node = nonConnectedNodes[0]
                    val node2 = nonConnectedNodes[1]
                    when {
                        node.coords.y == node2.coords.y -> ConnectionTypeEnum.Open
                        node.coords.x == node2.coords.x -> ConnectionTypeEnum.Open
                        node.coords.x < node2.coords.x -> ConnectionTypeEnum.LineTiltedLeft
                        node.coords.x > node2.coords.x -> ConnectionTypeEnum.LineTiltedRight
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
        nonConnectedNodes.add(node)
        nonConnectedNodes.add(node2)
    }

    fun disconnectNode(node: Node, node2: Node) {
        nonConnectedNodes.remove(node)
        nonConnectedNodes.remove(node2)
    }
}