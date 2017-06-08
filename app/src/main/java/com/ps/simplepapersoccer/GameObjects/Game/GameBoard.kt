package com.ps.simplepapersoccer.GameObjects.Game

import android.graphics.Point
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction.IntegerLine
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Goal
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.GameObjects.Move.StoredMove
import com.ps.simplepapersoccer.Helpers.MathHelper

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.UUID

class GameBoard(private val gridSizeX: Int, private val gridSizeY: Int) {
    var nodeHashMap = HashMap<UUID, Node>()

    var ballNode: Node

    lateinit var goalNode1: Node
    lateinit var goalNode2: Node

    var topGoalLines: Goal = Goal(IntegerLine(Point(gridSizeX / 2 - 1, 0), Point(gridSizeX / 2 + 1, 0)),
            IntegerLine(Point(gridSizeX / 2 - 1, 0), Point(gridSizeX / 2 - 1, 1)),
            IntegerLine(Point(gridSizeX / 2 + 1, 0), Point(gridSizeX / 2 + 1, 1)))
    var bottomGoalLines: Goal = Goal(IntegerLine(Point(gridSizeX / 2 - 1, gridSizeY), Point(gridSizeX / 2 + 1, gridSizeY)),
            IntegerLine(Point(gridSizeX / 2 - 1, gridSizeY), Point(gridSizeX / 2 - 1, gridSizeY-1)),
            IntegerLine(Point(gridSizeX / 2 + 1, gridSizeY), Point(gridSizeX / 2 + 1, gridSizeY-1)))

    private val allPartialMoves = ArrayList<StoredMove>()

    override fun hashCode(): Int {
        return allPartialMoves.hashCode() xor nodeHashMap.hashCode() xor ballNode.hashCode()
    }

    init {
        makeNodes(gridSizeX, gridSizeY)
        ballNode = findNodeByCoords(Point(gridSizeX / 2, gridSizeY / 2)) as Node
    }

    private fun isEdgeNode(point: Point) : Boolean {
        return (point.x == 0 || point.x == gridSizeX || point.y == 1 || point.y == gridSizeY - 1)
    }

    private fun makeNodes(gridSizeX: Int, gridSizeY: Int) {
        for (y in 1..gridSizeY - 1) {
            (0..gridSizeX)
                    .filter { !topGoalLines.contains(Point(it, y)) && !bottomGoalLines.contains(Point(it, y)) }
                    .forEach {
                        if (isEdgeNode(Point(it, y))) addNodeToNodeMap(Node(Point(it, y), NodeTypeEnum.Wall))
                        else {
                            addNodeToNodeMap(Node(Point(it, y), NodeTypeEnum.Empty))
                        }
                    }
        }

        for (node in topGoalLines.allNodes) {
            addNodeToNodeMap(node)
        }

        for (node in bottomGoalLines.allNodes) {
            addNodeToNodeMap(node)
        }

        goalNode1 = bottomGoalLines.allNodes.first { x -> x.nodeType == NodeTypeEnum.Goal }
        goalNode2 = topGoalLines.allNodes .first { x -> x.nodeType == NodeTypeEnum.Goal }

        GenerateAllNeighbors()
    }

    fun GenerateAllNeighbors() {
        for (node in nodeHashMap.values) {
            for (otherNode in nodeHashMap.values) {
                if (node.id === otherNode.id) continue

                val euclideanDistance = MathHelper.euclideanDistance(node.coords, otherNode.coords)
                if (euclideanDistance.toInt() == 1) node.AddCoordNeighbor(otherNode)

                if (node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Wall) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Post, NodeTypeEnum.Goal)) {
                    if (node.isDiagonalNeighbor(otherNode) && euclideanDistance < 2) {
                        node.AddNeighbor(otherNode)
                    }
                    else continue
                }

                if (node.pairMatchesType(otherNode, NodeTypeEnum.Post, NodeTypeEnum.Wall) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Post) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Goal)) {
                    continue
                }

                if (euclideanDistance < 2) node.AddNeighbor(otherNode)
            }
        }
    }

    fun allPossibleMovesFromNode(node: Node): HashSet<PossibleMove> {
        val possibleMoves = node.neighbors
                .mapTo(HashSet<PossibleMove>()) { PossibleMove(node, it!!) }

        return possibleMoves
    }

    fun UndoLastMove(): PartialMove {
        val storedMove = allPartialMoves.last()
        storedMove.partialMove.newNode.AddNeighbor(storedMove.partialMove.oldNode)
        storedMove.partialMove.oldNode.AddNeighbor(storedMove.partialMove.newNode)
        storedMove.partialMove.newNode.nodeType = storedMove.newNodeTypeEnum
        ballNode = storedMove.partialMove.oldNode
        allPartialMoves.remove(storedMove)
        return storedMove.partialMove
    }

    fun MakePartialMove(partialMove: PartialMove) {
        allPartialMoves.add(StoredMove(partialMove, partialMove.oldNode.nodeType, partialMove.newNode.nodeType))
        partialMove.newNode.RemoveNeighborPair(partialMove.oldNode)

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble

        ballNode = partialMove.newNode
    }

    //Returns the node with the XY coordinates
    fun findNodeByCoords(point: Point): Node? {
        return nodeHashMap.values.firstOrNull { it.coords == point }
    }

    fun addNodeToNodeMap(node: Node) {
        nodeHashMap.put(node.id, node)
    }
}
