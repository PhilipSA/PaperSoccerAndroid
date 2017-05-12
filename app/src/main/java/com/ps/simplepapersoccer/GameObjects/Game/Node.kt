package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.Enums.NodeTypeEnum

import java.util.HashSet
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

class Node {
    var id: UUID
    var xCord: Int = 0
    var yCord: Int = 0
    var nodeType: NodeTypeEnum
    var neighbors = CopyOnWriteArraySet<UUID>()

    override fun hashCode(): Int {
        return id.hashCode() xor nodeType.hashCode() xor neighbors.hashCode()
    }

    fun RemoveNeighborPair(other: Node) {
        neighbors.remove(other.id)
        other.neighbors.remove(this.id)
    }

    fun AddNeighborPair(other: Node) {
        neighbors.add(other.id)
        other.neighbors.add(this.id)
    }

    internal constructor(x: Int, y: Int, nodeType: NodeTypeEnum) {
        id = UUID.randomUUID()
        this.xCord = x
        this.yCord = y
        this.nodeType = nodeType
    }

    internal constructor(node: Node) {
        this.id = node.id
        this.xCord = node.xCord
        this.yCord = node.yCord
        this.nodeType = node.nodeType
        this.neighbors = node.neighbors
    }
}
