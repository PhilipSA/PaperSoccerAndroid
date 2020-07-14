package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.Abstraction.IntegerLine
import com.ps.simplepapersoccer.gameObjects.game.geometry.Goal
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import com.ps.simplepapersoccer.gameObjects.move.StoredMove
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import com.ps.simplepapersoccer.helpers.MathHelper
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.roundToInt

data class GameBoard(val gridSizeX: Int, val gridSizeY: Int) {
    var nodeHashSet = HashSet<Node>()

    val ballNode get() = nodeHashSet.first { it.containsBall }

    var currentPlayersTurn: Int = 1

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

    var allPartialMoves = Stack<StoredMove>(); private set

    val isGameOver: Boolean
        get() {
            return getWinner(ballNode)
        }

    init {
        makeNodes(gridSizeX, gridSizeY)
        findNodeByCoords(TwoDimensionalPoint(gridSizeX / 2, gridSizeY / 2))?.containsBall = true
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

    private fun getWinner(node: Node): Boolean {
        return when {
            node.nodeType == NodeTypeEnum.Goal -> true
            node.neighbors.size == 0 -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun allPossibleMovesFromNode(node: Node): List<PossibleMove> {
        return node.neighbors.sortedBy { it.coords }.map { PossibleMove(node, it) }
    }

    fun undoLastMove(): PartialMove? {
        return if (allPartialMoves.isNotEmpty()) {
            val storedMove = allPartialMoves.pop()
            storedMove.partialMove.newNode.addNeighbor(storedMove.partialMove.oldNode)
            storedMove.partialMove.oldNode.addNeighbor(storedMove.partialMove.newNode)
            storedMove.partialMove.newNode.nodeType = storedMove.newNodeTypeEnum

            storedMove.partialMove.oldNode.containsBall = true
            storedMove.partialMove.newNode.containsBall = false

            storedMove.partialMove

            currentPlayersTurn = storedMove.partialMove.madeTheMove

            storedMove.partialMove
        } else null
    }

    fun makePartialMove(partialMove: PartialMove) {
        allPartialMoves.add(StoredMove(partialMove, partialMove.oldNode.nodeType, partialMove.newNode.nodeType))
        partialMove.newNode.removeNeighborPair(partialMove.oldNode)
        partialMove.oldNode.removeNeighborPair(partialMove.newNode)

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble

        partialMove.newNode.containsBall = true
        partialMove.oldNode.containsBall = false

        if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
            changeTurn()
        }
    }

    private fun changeTurn() {
        currentPlayersTurn = 3 - currentPlayersTurn
    }

    fun findNodeByCoords(point: TwoDimensionalPoint): Node? {
        return nodeHashSet.firstOrNull { it.coords == point }
    }

    override fun toString(): String {
        val sortedNodes = nodeHashSet.sortedBy { it.coords }

        var returnString = ""
        var currentY = 0

        for (i in sortedNodes.indices) {
            val node = sortedNodes[i]

            if (currentY != node.coords.y) {
                returnString += "\n"
                ++currentY
            }

            val nodeTypeSymbol = when (node.nodeType) {
                NodeTypeEnum.ContainsBall -> "O"
                NodeTypeEnum.Goal -> "G"
                NodeTypeEnum.Empty -> "X"
                NodeTypeEnum.Wall -> "|"
                NodeTypeEnum.BounceAble -> "B"
                else -> "?"
            }

            returnString = "$returnString $nodeTypeSymbol"

            if (node.coords.x + node.coords.y != i) returnString += " "
        }

        returnString += "\n"

        return returnString
    }
}
