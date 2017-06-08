package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum

import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

class Node {
    var coords: Point
    var nodeType: NodeTypeEnum

    var id: UUID
    var neighbors: MutableList<Node>
    var coordNeighbors: MutableList<Node>

    override fun hashCode(): Int {
        return id.hashCode() xor nodeType.hashCode() xor neighbors.hashCode()
    }

    fun pairMatchesType(other: Node, firstNodeType: NodeTypeEnum, otherNodeType: NodeTypeEnum): Boolean {
        return (nodeType == firstNodeType && other.nodeType == otherNodeType) || (nodeType == otherNodeType && other.nodeType == firstNodeType)
    }

    //test
    fun isDiagonalNeighbor(other: Node): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun RemoveNeighborPair(other: Node) {
        neighbors.remove(other)
        other.neighbors.remove(this)
    }

    fun AddNeighbor(other: Node) {
        neighbors.add(other)
    }

    fun AddCoordNeighbor(other: Node) {
        coordNeighbors.add(other)
    }

    constructor(coords: Point, nodeType: NodeTypeEnum) {
        id = UUID.randomUUID()
        this.coords = coords
        this.nodeType = nodeType
        this.neighbors = mutableListOf()
        this.coordNeighbors = mutableListOf()
    }

    constructor(node: Node) {
        this.id = node.id
        this.coords = node.coords
        this.nodeType = node.nodeType
        this.neighbors = node.neighbors
        this.coordNeighbors = node.coordNeighbors
    }
}
