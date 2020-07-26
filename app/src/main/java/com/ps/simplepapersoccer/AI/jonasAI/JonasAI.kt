package com.ps.simplepapersoccer.ai.jonasAI

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.helpers.PathFindingHelper
import java.util.*
import kotlin.collections.ArrayList

class JonasAI(playerNumber: Int, playerColor: Int, playerName: String = JonasAI::class.java.simpleName) : IGameAI, AIPlayer(playerName, playerNumber, playerColor) {
    var goalFound: Boolean = false

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        goalFound = false

        val newList = ArrayList<MoveSequence>()
        val allTheMoves = followAllPossibleMoves(newList, gameHandler, UUID.randomUUID())

        val bestMoveSequence = evaluateNodes(allTheMoves!!, gameHandler)

        return bestMoveSequence.moveList.pollLast()!!
    }

    private fun followAllPossibleMoves(moveSequences: ArrayList<MoveSequence>, gameHandler: GameHandler, calledBy: UUID): ArrayList<MoveSequence>? {
        val possibleMoves = gameHandler.gameBoard.allLegalMovesFromBallNode

        for (possibleMove in possibleMoves) {

            if (goalFound) {
                break
            }

            val isGoal = gameHandler.getOpponent(gameHandler.currentPlayersTurn).goal?.isGoalNode(possibleMove.newNode)
                    ?: false

            if (possibleMove.newNode.nodeType == NodeTypeEnum.Empty
                    || isGoal) {
                goalFound = isGoal

                val moveList = LinkedList<PartialMove>()
                moveList.add(PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.gameBoard.currentPlayersTurn))

                val newMoveSequence = MoveSequence(
                        moveList,
                        possibleMove.newNode,
                        calledBy,
                        isGoal)

                moveSequences.add(newMoveSequence)
            } else {
                val thisIdentifier = UUID.randomUUID()

                val partialMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.gameBoard.currentPlayersTurn)
                partialMove.madeTheMove = gameHandler.gameBoard.currentPlayersTurn
                gameHandler.gameBoard.makePartialMove(partialMove)

                val moves = followAllPossibleMoves(moveSequences, gameHandler, thisIdentifier)

                for (sequences in moves!!) {
                    if (sequences.originIdentifier == thisIdentifier) {
                        sequences.moveList.add(partialMove)
                        sequences.originIdentifier = calledBy
                    }
                }

                gameHandler.gameBoard.undoLastMove()
            }
        }

        return moveSequences
    }

    private fun evaluateNodes(sequences: ArrayList<MoveSequence>, gameHandler: GameHandler): MoveSequence {
        var manhattanDistance = Integer.MAX_VALUE.toDouble()
        var tempManhattan: Double
        var manhattanSequence: MoveSequence? = null

        if (goalFound) {
            val goalMaker = sequences.first { it.goal }
            return goalMaker
        }

        for (sequence in sequences) {
            tempManhattan = PathFindingHelper.findPathGreedyBestFirstSearchBiDirectional(sequence.endNode, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goal!!.goalNode()).size.toDouble()
            if (tempManhattan < manhattanDistance) {
                manhattanDistance = tempManhattan
                manhattanSequence = sequence
            }
        }

        return manhattanSequence!!
    }
}