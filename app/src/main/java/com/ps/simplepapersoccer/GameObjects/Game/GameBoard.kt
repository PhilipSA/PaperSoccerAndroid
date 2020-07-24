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
    val allBaseNodes = mutableListOf<BaseNode>()
    val allNodes: HashSet<Node>

    private var currentBallNode: Node? = null
        set(value) {
            field = value
            allLegalMovesFromBallNode = ballNode.connectedNodes.sortedBy { it.coords }.map { PossibleMove(ballNode, it) }
        }

    val ballNode: Node get() = currentBallNode!!

    var allLegalMovesFromBallNode: List<PossibleMove> = emptyList(); private set

    var currentPlayersTurn: Int = 1

    lateinit var goal1: Goal
    lateinit var goal2: Goal
    private val goalScalingX = (gridSizeX / 6).toDouble().roundToInt()

    var topGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0),
            TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 2)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 0), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 2)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, 1), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, 1)))
    var bottomGoalLines: Goal = Goal(IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY + 2),
            TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY + 2)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY + 2), TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY + 2), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY)),
            IntegerLine(TwoDimensionalPoint(gridSizeX / 2 - goalScalingX, gridSizeY + 1), TwoDimensionalPoint(gridSizeX / 2 + goalScalingX, gridSizeY + 1)))

    var allPartialMoves = Stack<StoredMove>(); private set

    val isGameOver: Boolean
        get() {
            return getWinner(ballNode)
        }

    init {
        makeNodes(gridSizeX, gridSizeY)
        allNodes = allBaseNodes.filterIsInstance<Node>().toHashSet()

        val startingBallNode = allNodes.toList().sortedBy { it.coords }[allNodes.size / 2]
        startingBallNode.containsBall = true
        currentBallNode = startingBallNode
    }

    private fun isEdgeNode(point: TwoDimensionalPoint): Boolean {
        return (point.x == 0 || point.x == gridSizeX || point.y == 2 || point.y == gridSizeY)
    }

    private fun makeNodes(gridSizeX: Int, gridSizeY: Int) {
        for (y in 2..gridSizeY) {
            (0..gridSizeX)
                    .filter { !topGoalLines.contains(TwoDimensionalPoint(it, y)) && !bottomGoalLines.contains(TwoDimensionalPoint(it, y)) }
                    .forEach {
                        when {
                            it % 2 == 1 -> {
                                allBaseNodes.add(ConnectionNode(TwoDimensionalPoint(it, y)))
                            }
                            y % 2 == 1 -> {
                                allBaseNodes.add(ConnectionNode(TwoDimensionalPoint(it, y)))
                            }
                            it % 2 != 1 && y % 2 != 1 -> {
                                if (isEdgeNode(TwoDimensionalPoint(it, y))) {
                                    allBaseNodes.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Wall))
                                } else allBaseNodes.add(Node(TwoDimensionalPoint(it, y), NodeTypeEnum.Empty))
                            }
                        }
                    }
        }

        topGoalLines.allNodes.forEach {
            allBaseNodes.add(it)
        }

        bottomGoalLines.allNodes.forEach {
            allBaseNodes.add(it)
        }

        goal1 = bottomGoalLines
        goal2 = topGoalLines

        generateAllNeighbors()
        allBaseNodes.sortBy { it.coords }
    }

    private fun generateAllNeighbors() {
        for (node in allBaseNodes) {
            for (otherNode in allBaseNodes) {
                if (node == otherNode) continue

                if (node is Node && otherNode is ConnectionNode) {
                    val euclideanDistance = MathHelper.euclideanDistance(node.coords, otherNode.coords)
                    if (euclideanDistance.toInt() < 2) {
                        node.addNeighbor(otherNode)
                        otherNode.neighbors.add(node)
                    }
                }
            }
        }

        for (node in allBaseNodes) {
            if (node is ConnectionNode) {
                node.createNodeConnections()
            }
        }
    }

    private fun getWinner(node: Node): Boolean {
        return when {
            node.nodeType == NodeTypeEnum.Goal -> true
            node.connectedNodes.size == 0 -> {
                true
            }
            else -> {
                false
            }
        }
    }

    private fun allPossibleMovesFromNode(node: Node): List<PossibleMove> {
        return node.connectedNodes.sortedBy { it.coords }.map { PossibleMove(node, it) }
    }

    fun allPossibleMovesFromNodeCoords(node: Node): List<Pair<PossibleMove, Boolean>> {
        val legalMoves = allPossibleMovesFromNode(node)

        return node.coordNeighbors.sortedBy { it.coords }.map { neighborNode ->
            Pair(PossibleMove(node, neighborNode), legalMoves.any { it.newNode == neighborNode })
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
            currentBallNode = storedMove.partialMove.oldNode

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
        currentBallNode = partialMove.newNode

        if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
            changeTurn()
        }
    }

    private fun changeTurn() {
        currentPlayersTurn = 3 - currentPlayersTurn
    }

    fun findNodeByCoords(point: TwoDimensionalPoint): Node? {
        return allNodes.firstOrNull { it.coords == point }
    }

    private fun getConnectionNodeBetweenNodes(node: Node, node2: Node): ConnectionNode? {
        return node.neighbors.first { it.getNodeConnection(node, node2) != null }
    }

    override fun toString(): String {
        var returnString = ""
        var currentY = 0

        for (i in allBaseNodes.indices) {
            val node = allBaseNodes[i]

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
