package com.ps.simplepapersoccer.ai.minimaxAI

import com.ps.simplepapersoccer.ai.GameAIHandler.Companion.AI_TIMEOUT_MS
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.data.enums.SortOrderEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import com.ps.simplepapersoccer.helpers.PathFindingHelper
import java.util.*
import kotlin.math.max
import kotlin.math.min

//Philips AI
class MinimaxAI(private val timeLimitMilliSeconds: Long = AI_TIMEOUT_MS, playerName: String, playerNumber: Int, playerColor: Int) :
        IGameAI, AIPlayer(playerName, playerNumber, playerColor) {

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        val bestMove = chooseMove(gameHandler)
        return bestMove?.returnMove as PartialMove
    }

    private fun chooseMove(state: GameHandler): MoveData? {
        val startTime = System.currentTimeMillis()
        var maxScore = Integer.MIN_VALUE.toDouble()
        var bestMove: MoveData? = null
        val maximPlayer = state.currentPlayersTurn

        val moves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, state.currentPlayersTurn)

        for (move in moves) {

            state.gameBoard.makePartialMove(move.returnMove as PartialMove)
            val searchTimeLimit = ((timeLimitMilliSeconds - 1000) / moves.size)
            val score = iterativeDeepeningSearch(state, searchTimeLimit, maximPlayer)
            //val score = alphaBetaPruningNoTimeLimit(state, 0, maximPlayer, Integer.MIN_VALUE.toDouble(), Integer.MAX_VALUE.toDouble())
            state.gameBoard.undoLastMove()

            if (score >= winCutoff) {
                return move
            }

            if (score > maxScore) {
                maxScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun iterativeDeepeningSearch(state: GameHandler, timeLimit: Long, maximPlayer: IPlayer): Double {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeLimit
        var depth = 1
        var score = 0.0
        searchCutoff = false

        while (true) {
            val currentTime = System.currentTimeMillis()

            if (currentTime >= endTime) {
                break
            }

            val searchResult = alphaBetaPruning(state, depth, maximPlayer, Integer.MIN_VALUE.toDouble(), Integer.MAX_VALUE.toDouble(), currentTime, endTime - currentTime)
            if (searchResult >= winCutoff) {
                return searchResult
            }

            if (!searchCutoff) {
                score = searchResult
            }

            depth++
        }

        return score
    }

    private fun alphaBetaPruning(state: GameHandler, currentDepth: Int, maximizingPlayer: IPlayer, paramAlpha: Double, paramBeta: Double, startTime: Long, timeLimit: Long): Double {
        var alpha = paramAlpha
        var beta = paramBeta
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime

        if (elapsedTime >= timeLimit) {
            searchCutoff = true
        }

        if (searchCutoff || currentDepth == 0 || state.getWinner(state.ballNode) != null) {
            return minmaxEvaluation(state, maximizingPlayer)
        }

        val bestScore: Double

        if (maximizingPlayer == state.currentPlayersTurn) {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.gameBoard.makePartialMove(possibleMove.returnMove!!)
                alpha = max(alpha, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit))
                state.gameBoard.undoLastMove()

                if (beta <= alpha) {
                    break // pruning
                }
            }
            bestScore = alpha
        } else {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.gameBoard.makePartialMove(possibleMove.returnMove!!)
                beta = min(beta, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit))
                state.gameBoard.undoLastMove()

                if (beta <= alpha) {
                    break // pruning
                }
            }

            bestScore = beta
        }

        return bestScore
    }

    private fun sortPossibleMovesByScore(sortOrder: SortOrderEnum, state: GameHandler, maximzingPlayer: IPlayer): ArrayList<MoveData> {
        val newPossibleMoves = ArrayList<MoveData>()
        for (possibleMove in state.gameBoard.allPossibleMovesFromNode(state.ballNode)) {
            val partialMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, state.gameBoard.currentPlayersTurn)
            partialMove.madeTheMove = state.gameBoard.currentPlayersTurn
            state.gameBoard.makePartialMove(partialMove)

            val moveData = MoveData(minmaxEvaluation(state, maximzingPlayer))
            moveData.returnMove = partialMove

            newPossibleMoves.add(moveData)

            state.gameBoard.undoLastMove()
        }

        if (sortOrder == SortOrderEnum.Ascending) {
            newPossibleMoves.sort()
        } else {
            Collections.sort(newPossibleMoves, Collections.reverseOrder<Any>())
        }
        return newPossibleMoves
    }

    private fun minmaxEvaluation(state: GameHandler, maximizingPlayer: IPlayer): Double {
        var score = 0.0

        if (state.getWinner(state.ballNode)?.winner == maximizingPlayer) score = 1000.0
        if (state.getWinner(state.ballNode)?.winner == state.getOpponent(maximizingPlayer)) score = -1000.0

        score += (-state.numberOfTurns).toDouble()

        val opponentsGoal = state.getOpponent(maximizingPlayer).goal!!.goalNode()
        score += -PathFindingHelper.findPath(state.ballNode, opponentsGoal).size * 2

        //Neighbors are bounceable
        state.gameBoard.allPossibleMovesFromNode(state.ballNode).forEach{
            if (it.newNode.nodeType == NodeTypeEnum.BounceAble && state.currentPlayersTurn === maximizingPlayer) ++score
            else if (it.newNode.nodeType == NodeTypeEnum.BounceAble) {
                --score
            }
        }
        return score
    }

    companion object {
        private val EVALS_PER_SECOND = 100
        private const val winCutoff = 900
        private var searchCutoff = false
    }
}
