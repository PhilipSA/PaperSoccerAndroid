package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum

import java.util.HashSet
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

class Node {
    var coords: Point
    var nodeType: NodeTypeEnum

    var id: UUID
    var neighbors: CopyOnWriteArraySet<UUID>
    var coordNeighbors: CopyOnWriteArraySet<Node>

    override fun hashCode(): Int {
        return id.hashCode() xor nodeType.hashCode() xor neighbors.hashCode()
    }

    fun isDiagonalNeighbor(other: Node): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun RemoveNeighborPair(other: Node) {
        neighbors.remove(other.id)
        other.neighbors.remove(this.id)
    }

    fun AddNeighborPair(other: Node) {
        neighbors.add(other.id)
        other.neighbors.add(this.id)
    }

    fun AddCoordNeighborPair(other: Node) {
        coordNeighbors.add(other)
        other.coordNeighbors.add(this)
    }

    constructor(coords: Point, nodeType: NodeTypeEnum) {
        id = UUID.randomUUID()
        this.coords = coords
        this.nodeType = nodeType
        this.neighbors = CopyOnWriteArraySet<UUID>()
        this.coordNeighbors = CopyOnWriteArraySet<Node>()
    }

    constructor(node: Node) {
        this.id = node.id
        this.coords = node.coords
        this.nodeType = node.nodeType
        this.neighbors = node.neighbors
        this.coordNeighbors = node.coordNeighbors
    }
}
