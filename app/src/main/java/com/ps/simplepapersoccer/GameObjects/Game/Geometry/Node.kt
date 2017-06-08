package com.ps.simplepapersoccer.GameObjects.Game.Geometry

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum

import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

class Node(val coords: Point, var nodeType: NodeTypeEnum) {
    val neighbors: MutableList<Node> = mutableListOf()
    val coordNeighbors: MutableList<Node> = mutableListOf()

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
}
