package com.ps.simplepapersoccer.GameObjects.Game

import android.graphics.Point
import android.util.Log
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Abstraction.IntegerLine
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Goal
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.GameObjects.Move.StoredMove
import com.ps.simplepapersoccer.Helpers.MathHelper
import java.util.ArrayList

class GameBoard(private val gridSizeX: Int, private val gridSizeY: Int) {
    var nodeHashSet = HashSet<Node>()

    var ballNode: Node

    lateinit var goal1: Goal
    lateinit var goal2: Goal
    val goalScalingX = Math.round((gridSizeX / 6).toDouble()).toInt()
    val goalScalingY = Math.round((gridSizeX / 6).toDouble()).toInt()

    var topGoalLines: Goal = Goal(IntegerLine(Point(gridSizeX / 2 - goalScalingX, 0), Point(gridSizeX / 2 + goalScalingX, 0)),
            IntegerLine(Point(gridSizeX / 2 - goalScalingX, 0), Point(gridSizeX / 2 - goalScalingX, 1)),
            IntegerLine(Point(gridSizeX / 2 + goalScalingX, 0), Point(gridSizeX / 2 + goalScalingX, 1)))
    var bottomGoalLines: Goal = Goal(IntegerLine(Point(gridSizeX / 2 - goalScalingX, gridSizeY), Point(gridSizeX / 2 + goalScalingX, gridSizeY)),
            IntegerLine(Point(gridSizeX / 2 - goalScalingX, gridSizeY), Point(gridSizeX / 2 - goalScalingX, gridSizeY-1)),
            IntegerLine(Point(gridSizeX / 2 + goalScalingX, gridSizeY), Point(gridSizeX / 2 + goalScalingX, gridSizeY-1)))

    private val allPartialMoves = ArrayList<StoredMove>()

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
                        if (isEdgeNode(Point(it, y))) nodeHashSet.add(Node(Point(it, y), NodeTypeEnum.Wall))
                        else {
                            nodeHashSet.add(Node(Point(it, y), NodeTypeEnum.Empty))
                        }
                    }
        }

        topGoalLines.allNodes.forEach {
            nodeHashSet.add(it)
        }

        bottomGoalLines.allNodes.forEach {
            nodeHashSet.add(it)
        }

        goal1 = bottomGoalLines
        goal2 = topGoalLines

        GenerateAllNeighbors()
    }

    fun GenerateAllNeighbors() {
        for (node in nodeHashSet) {
            for (otherNode in nodeHashSet) {
                if (node == otherNode) continue

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
                .mapTo(HashSet<PossibleMove>()) { PossibleMove(node, it) }

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
        partialMove.oldNode.RemoveNeighborPair(partialMove.newNode)

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble

        ballNode = partialMove.newNode
    }

    //Returns the node with the XY coordinates
    fun findNodeByCoords(point: Point): Node? {
        return nodeHashSet.firstOrNull { it.coords == point }
    }
}
