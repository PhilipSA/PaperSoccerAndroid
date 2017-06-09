package com.ps.simplepapersoccer.AI.MinimaxAI

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.Enums.SortOrderEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer
import com.ps.simplepapersoccer.Helpers.MathHelper
import com.ps.simplepapersoccer.Helpers.PathFindingHelper
import java.util.*

class MinimaxAI(timeLimitMilliSeconds: Int) : IGameAI {
    init {
        TIME_LIMIT_MILLIS = timeLimitMilliSeconds
    }

    override fun MakeMove(gameHandler: GameHandler): PartialMove {
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

            state.MakePartialMove(move.returnMove as PartialMove)
            val searchTimeLimit = ((TIME_LIMIT_MILLIS - 1000) / moves.size).toLong()
            //val score = iterativeDeepeningSearch(state, searchTimeLimit, maximPlayer)
            val score = alphaBetaPruningNoTimeLimit(state, 0, maximPlayer, Integer.MIN_VALUE.toDouble(), Integer.MAX_VALUE.toDouble())
            state.UndoLastMove()

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

        if (searchCutoff || currentDepth == 0 || state.getWinner(state.ballNode()) != null) {
            val value = minmaxEvaluation(state, maximizingPlayer)
            return value
        }

        var bestScore = 0.0

        if (maximizingPlayer == state.currentPlayersTurn) {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.MakePartialMove(possibleMove.returnMove!!)
                alpha = Math.max(alpha, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit))
                state.UndoLastMove()

                if (beta <= alpha) {
                    break // pruning
                }
            }
            bestScore = alpha
        } else {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.MakePartialMove(possibleMove.returnMove!!)
                beta = Math.min(beta, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit))
                state.UndoLastMove()

                if (beta <= alpha) {
                    break // pruning
                }
            }

            bestScore = beta
        }

        return bestScore
    }

    private fun alphaBetaPruningNoTimeLimit(state: GameHandler, currentDepth: Int, maximizingPlayer: IPlayer, paramAlpha: Double, paramBeta: Double): Double {
        var alpha = paramAlpha
        var beta = paramBeta

        if (searchCutoff || currentDepth == 0 || state.getWinner(state.ballNode()) != null) {
            val value = minmaxEvaluation(state, maximizingPlayer)
            return value
        }

        var bestScore = 0.0

        if (maximizingPlayer == state.currentPlayersTurn) {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.MakePartialMove(possibleMove.returnMove!!)
                alpha = Math.max(alpha, alphaBetaPruningNoTimeLimit(state, currentDepth - 1, maximizingPlayer, alpha, beta))
                state.UndoLastMove()

                if (beta <= alpha) {
                    break // pruning
                }
            }
            bestScore = alpha
        } else {
            val possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer)
            for (possibleMove in possibleMoves) {
                state.MakePartialMove(possibleMove.returnMove!!)
                beta = Math.min(beta, alphaBetaPruningNoTimeLimit(state, currentDepth - 1, maximizingPlayer, alpha, beta))
                state.UndoLastMove()

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
        for (possibleMove in state.gameBoard.allPossibleMovesFromNode(state.ballNode())) {
            val partialMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, state.currentPlayersTurn)
            partialMove.madeTheMove = state.currentPlayersTurn
            state.MakePartialMove(partialMove)

            val moveData = MoveData(minmaxEvaluation(state, maximzingPlayer))
            moveData.returnMove = partialMove

            newPossibleMoves.add(moveData)

            state.UndoLastMove()
        }

        if (sortOrder == SortOrderEnum.Ascending) {
            Collections.sort(newPossibleMoves)
        } else {
            Collections.sort(newPossibleMoves, Collections.reverseOrder<Any>())
        }
        return newPossibleMoves
    }

    private fun minmaxEvaluation(state: GameHandler, maximizingPlayer: IPlayer): Double {
        var score = 0.0

        if (state.getWinner(state.ballNode())?.winner == maximizingPlayer) score = 1000.0
        if (state.getWinner(state.ballNode())?.winner == state.getOpponent(maximizingPlayer)) score = -1000.0

        score += (-state.numberOfTurns).toDouble()

        val opponentsGoal = state.getOpponent(maximizingPlayer).goalNode
        score += -PathFindingHelper.findPath(state.ballNode(), opponentsGoal!!).size * 2

        //Neighbors are bounceable
        state.gameBoard.allPossibleMovesFromNode(state.ballNode()).forEach{
            if (it.newNode.nodeType == NodeTypeEnum.BounceAble && state.currentPlayersTurn === maximizingPlayer) ++score
            else if (it.newNode.nodeType == NodeTypeEnum.BounceAble) {
                --score;
            }
        }
        return score
    }

    companion object {
        private var TIME_LIMIT_MILLIS = 2000
        private val EVALS_PER_SECOND = 100
        private val winCutoff = 900
        private var searchCutoff = false
    }
}
