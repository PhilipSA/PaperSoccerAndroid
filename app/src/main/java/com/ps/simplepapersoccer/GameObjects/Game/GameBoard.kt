package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.Abstraction.IntegerLine
import com.ps.simplepapersoccer.gameObjects.game.geometry.Goal
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import com.ps.simplepapersoccer.gameObjects.move.StoredMove
import com.ps.simplepapersoccer.helpers.MathHelper
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.roundToInt

class GameBoard(private val gridSizeX: Int, private val gridSizeY: Int) {
    var nodeHashSet = HashSet<Node>()

    var ballNode: Node

    lateinit var goal1: Goal
    lateinit var goal2: Goal
    private val goalScalingX = (gridSizeX / 6).toDouble().roundToInt()
    val goalScalingY = (gridSizeX / 6).toDouble().roundToInt()

    var topGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 1)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 1)))
    var bottomGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY-1)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY-1)))

    private val allPartialMoves = Stack<StoredMove>()

    init {
        makeNodes(gridSizeX, gridSizeY)
        ballNode = findNodeByCoords(TwoDimensionalPoint(gridSizeX / 2, gridSizeY / 2)) as Node
    }

    private fun isEdgeNode(point: TwoDimensionalPoint) : Boolean {
        return (point.x == 0 || point.x == gridSizeX || point.y == 1 || point.y == gridSizeY - 1)
    }

    private fun makeNodes(gridSizeX: Int, gridSizeY: Int) {
        for (y in 1 until gridSizeY) {
            (0..gridSizeX)
                    .filter { !topGoalLines.contains(TwoDimensionalPoint(it, y)) && !bottomGoalLines.contains(TwoDimensionalPoint(it, y)) }
                    .forEach {
                        if (isEdgeNode(TwoDimensionalPoint(it, y))) nodeHashSet.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Wall))
                        else {
                            nodeHashSet.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Empty))
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

        generateAllNeighbors()
    }

    private fun generateAllNeighbors() {
        for (node in nodeHashSet) {
            for (otherNode in nodeHashSet) {
                if (node == otherNode) continue

                val euclideanDistance = MathHelper.euclideanDistance(node.coords, otherNode.coords)
                if (euclideanDistance.toInt() == 1) node.addCoordNeighbor(otherNode)

                if (node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Wall) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Post, NodeTypeEnum.Goal)) {
                    if (node.isDiagonalNeighbor(otherNode) && euclideanDistance < 2) {
                        node.addNeighbor(otherNode)
                    }
                    else continue
                }

                if (node.pairMatchesType(otherNode, NodeTypeEnum.Post, NodeTypeEnum.Wall) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Post) ||
                        node.pairMatchesType(otherNode, NodeTypeEnum.Wall, NodeTypeEnum.Goal)) {
                    continue
                }

                if (euclideanDistance < 2) node.addNeighbor(otherNode)
            }
        }
    }

    fun allPossibleMovesFromNode(node: Node): HashSet<PossibleMove> {
        return node.neighbors.map { PossibleMove(node, it) }.toHashSet()
    }

    fun undoLastMove(): PartialMove {
        val storedMove = allPartialMoves.pop()
        storedMove.partialMove.newNode.addNeighbor(storedMove.partialMove.oldNode)
        storedMove.partialMove.oldNode.addNeighbor(storedMove.partialMove.newNode)
        storedMove.partialMove.newNode.nodeType = storedMove.newNodeTypeEnum
        ballNode = storedMove.partialMove.oldNode
        return storedMove.partialMove
    }

    fun makePartialMove(partialMove: PartialMove) {
        allPartialMoves.add(StoredMove(partialMove, partialMove.oldNode.nodeType, partialMove.newNode.nodeType))
        partialMove.newNode.removeNeighborPair(partialMove.oldNode)
        partialMove.oldNode.removeNeighborPair(partialMove.newNode)

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble

        ballNode = partialMove.newNode
    }

    fun findNodeByCoords(point: TwoDimensionalPoint): Node? {
        return nodeHashSet.firstOrNull { it.coords == point }
    }
}
