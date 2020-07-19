package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.data.enums.ConnectionTypeEnum
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.ConnectionNode
import com.ps.simplepapersoccer.gameObjects.game.geometry.Goal
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.BaseNode
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.IntegerLine
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import com.ps.simplepapersoccer.gameObjects.move.StoredMove
import com.ps.simplepapersoccer.helpers.MathHelper
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.roundToInt


data class GameBoard(val gridSizeX: Int, val gridSizeY: Int) {
    var allNodesHashSet = HashSet<BaseNode>()
    val nodesHashSet get() = allNodesHashSet.filterIsInstance<Node>()

    val ballNode: Node get() = nodesHashSet.first { it.containsBall }

    var currentPlayersTurn: Int = 1

    lateinit var goal1: Goal
    lateinit var goal2: Goal
    private val goalScalingX = (gridSizeX / 6).toDouble().roundToInt()
    val goalScalingY = (gridSizeX / 6).toDouble().roundToInt()

    var topGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 1)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 1)))
    var bottomGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY - 1)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY - 1)))

    var allPartialMoves = Stack<StoredMove>(); private set

    val isGameOver: Boolean
        get() {
            return getWinner(ballNode)
        }

    init {
        makeNodes(gridSizeX, gridSizeY)
        findNodeByCoords(TwoDimensionalPoint(gridSizeX / 2, gridSizeY / 2))?.containsBall = true
    }

    private fun isEdgeNode(point: TwoDimensionalPoint): Boolean {
        return (point.x == 0 || point.x == gridSizeX || point.y == 1 || point.y == gridSizeY - 1)
    }

    private fun makeNodes(gridSizeX: Int, gridSizeY: Int) {
        for (y in 1 until gridSizeY) {
            (0..gridSizeX)
                    .filter { !topGoalLines.contains(TwoDimensionalPoint(it, y)) && !bottomGoalLines.contains(TwoDimensionalPoint(it, y)) }
                    .forEach {
                        when {
                            isEdgeNode(TwoDimensionalPoint(it, y)) -> {
                                allNodesHashSet.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Wall))
                            }
                            it % 2 == 0 -> {
                                allNodesHashSet.add(ConnectionNode(TwoDimensionalPoint(it, y)))
                            }
                            y % 2 == 0 -> {
                                allNodesHashSet.add(ConnectionNode(TwoDimensionalPoint(it, y)))
                            }
                            else -> {
                                allNodesHashSet.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Empty))
                            }
                        }
                    }
        }

        topGoalLines.allNodes.forEach {
            allNodesHashSet.add(it)
        }

        bottomGoalLines.allNodes.forEach {
            allNodesHashSet.add(it)
        }

        goal1 = bottomGoalLines
        goal2 = topGoalLines

        generateAllNeighbors()
    }

    private fun generateAllNeighbors() {
        for (node in allNodesHashSet) {
            for (otherNode in allNodesHashSet) {
                if (node == otherNode) continue

                if (node is Node && otherNode is ConnectionNode) {
                    val euclideanDistance = MathHelper.euclideanDistance(node.coords, otherNode.coords)
                    if (euclideanDistance.toInt() < 2) {
                        node.addNeighbor(otherNode)
                        otherNode.nonConnectedNodes.add(node)
                    }
                }
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
        return node.connectedNodeNeighbors().sortedBy { it.coords }.map { PossibleMove(node, it) }
    }

    //Not all moves might be legal
    fun allPossibleMovesFromNodeCoords(node: Node): List<PossibleMove> {
        return node.coordNeighbors.sortedBy { it.coords }.map {
            PossibleMove(node, it)
        }
    }

    fun undoLastMove(): PartialMove? {
        return if (allPartialMoves.isNotEmpty()) {
            val storedMove = allPartialMoves.pop()
            getConnectionNodeBetweenNodes(storedMove.partialMove.oldNode, storedMove.partialMove.newNode)
                    ?.connectNodes(storedMove.partialMove.oldNode, storedMove.partialMove.newNode)

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

        getConnectionNodeBetweenNodes(partialMove.oldNode, partialMove.newNode)?.disconnectNode(partialMove.oldNode, partialMove.newNode)

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
        return nodesHashSet.firstOrNull { it.coords == point }
    }

    private fun getConnectionNodeBetweenNodes(node: Node, node2: Node): ConnectionNode? {
        return node.neighbors
                .filter { node1 ->
                    node2.neighbors.any { node1.coords == it.coords }
                }.sortedBy { it.coords }.getOrNull(1)
    }

    override fun toString(): String {
        val sortedNodes = allNodesHashSet.sortedBy { it.coords }

        var returnString = ""
        var currentY = 0

        for (i in sortedNodes.indices) {
            val node = sortedNodes[i]

            if (currentY != node.coords.y) {
                returnString += "\n"
                ++currentY
            }

            if (node is Node) {
                val nodeTypeSymbol = when (node.nodeType) {
                    NodeTypeEnum.ContainsBall -> "O"
                    NodeTypeEnum.Goal -> "G"
                    NodeTypeEnum.Empty -> "X"
                    NodeTypeEnum.Wall -> "|"
                    NodeTypeEnum.BounceAble -> "B"
                    else -> "?"
                }

                returnString = "$returnString $nodeTypeSymbol"
            } else if (node is ConnectionNode) {
                val nodeTypeSymbol = when (node.connectionTypeEnum) {
                    ConnectionTypeEnum.Open -> "-"
                    ConnectionTypeEnum.Blocked -> "!"
                    ConnectionTypeEnum.LineTiltedLeft -> "\\"
                    ConnectionTypeEnum.LineTiltedRight -> "/"
                    else -> "?"
                }

                returnString = "$returnString $nodeTypeSymbol"
            }

            if (node.coords.x + node.coords.y != i) returnString += " "
        }

        returnString += "\n"

        return returnString
    }
}
