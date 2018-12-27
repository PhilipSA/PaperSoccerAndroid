package com.ps.simplepapersoccer.gameObjects.game.geometry

import com.ps.simplepapersoccer.enums.NodeTypeEnum

class Node(val coords: TwoDimensionalPoint, var nodeType: NodeTypeEnum) {
    val neighbors: HashSet<Node> = hashSetOf()
    val coordNeighbors: HashSet<Node> = hashSetOf()
    var cameFrom: Node? = null
    var nodeValue: Double = 0.0
    var costSoFar: Double = 0.0

    fun pairMatchesType(other: Node, firstNodeType: NodeTypeEnum, otherNodeType: NodeTypeEnum): Boolean {
        return (nodeType == firstNodeType && other.nodeType == otherNodeType) || (nodeType == otherNodeType && other.nodeType == firstNodeType)
    }

    //test
    fun isDiagonalNeighbor(other: Node): Boolean {
        return coords.y != other.coords.y && other.coords.x != this.coords.x
    }

    fun removeNeighborPair(other: Node) {
        neighbors.remove(other)
        other.neighbors.remove(this)
    }

    fun addNeighbor(other: Node) {
        neighbors.add(other)
    }

    fun addCoordNeighbor(other: Node) {
        coordNeighbors.add(other)
    }

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }
}
