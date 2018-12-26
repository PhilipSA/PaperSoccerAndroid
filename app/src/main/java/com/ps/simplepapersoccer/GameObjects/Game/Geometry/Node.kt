package com.ps.simplepapersoccer.gameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.enums.NodeTypeEnum

class Node(val coords: Point, var nodeType: NodeTypeEnum) {
    val neighbors: MutableList<Node> = mutableListOf()
    val coordNeighbors: MutableList<Node> = mutableListOf()
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

    override fun toString(): String {
        return coords.toString() + nodeType.toString()
    }
}
