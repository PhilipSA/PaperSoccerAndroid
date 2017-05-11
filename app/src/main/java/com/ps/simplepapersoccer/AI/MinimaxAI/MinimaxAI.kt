package com.ps.simplepapersoccer.AI.MinimaxAI

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.Enums.SortOrderEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.GameObjects.Game.Node
import com.ps.simplepapersoccer.GameObjects.Player
import com.ps.simplepapersoccer.Helpers.MathHelper
import java.util.*

class MinimaxAI(timeLimitMilliSeconds: Int) : IGameAI {

    private val transpositionsMap = HashMap<Int, TranspositionData>()

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

            //
            // Compute how long to spend looking at each move
            //
            val searchTimeLimit = ((TIME_LIMIT_MILLIS - 1000) / moves.size).toLong()

            val score = iterativeDeepeningSearch(state, searchTimeLimit, maximPlayer)

            state.UndoLastMove()

            //
            // If the search finds a winning move
            //
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

    //
    // Run an iterative deepening search on a game state, taking no longer than the given time limit
    //
    private fun iterativeDeepeningSearch(state: GameHandler, timeLimit: Long, maximPlayer: Player): Double {
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

            //
            // If the search finds a winning move, stop searching
            //
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

    private fun alphaBetaPruning(state: GameHandler, currentDepth: Int, maximizingPlayer: Player, alpha: Double, beta: Double, startTime: Long, timeLimit: Long): Double {
        var alpha = alpha
        var beta = beta
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime

        if (elapsedTime >= timeLimit) {
            searchCutoff = true
        }

        val storedAlpha = alpha
        val storedBeta = beta

        val transpositionData = transpositionsMap[state.hashCode()]
        if (transpositionData != null && transpositionData.depth >= currentDepth) {
            if (transpositionData.scoreTypeEnum == ScoreTypeEnum.EXACT) {
                return transpositionData.score
            } else if (transpositionData.scoreTypeEnum == ScoreTypeEnum.UPPER) {
                beta = Math.min(beta, transpositionData.score)
            } else if (transpositionData.scoreTypeEnum == ScoreTypeEnum.LOWER) {
                alpha = Math.max(alpha, transpositionData.score)
            }
            if (alpha >= beta) {
                return transpositionData.score
            }
        }

        if (searchCutoff || currentDepth == 0 || state.getWinner(state.ballNode()) != null) {
            val value = minmaxEvaluation(state, maximizingPlayer)
            if (value <= alpha) {
                transpositionsMap.put(state.hashCode(), TranspositionData(currentDepth, value, ScoreTypeEnum.LOWER))
            } else if (value >= beta) {
                transpositionsMap.put(state.hashCode(), TranspositionData(currentDepth, value, ScoreTypeEnum.UPPER))
            } else {
                transpositionsMap.put(state.hashCode(), TranspositionData(currentDepth, value, ScoreTypeEnum.EXACT))
            }
            return value
        }

        var bestScore = 0.0

        if (maximizingPlayer === state.currentPlayersTurn) {
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

        val next = TranspositionData()
        next.score = bestScore
        next.depth = currentDepth
        if (bestScore <= storedAlpha) {
            next.scoreTypeEnum = ScoreTypeEnum.UPPER
        } else if (bestScore >= storedBeta) {
            next.scoreTypeEnum = ScoreTypeEnum.LOWER
        } else {
            next.scoreTypeEnum = ScoreTypeEnum.EXACT
        }
        transpositionsMap.put(state.hashCode(), next)

        return bestScore
    }

    private fun sortPossibleMovesByScore(sortOrder: SortOrderEnum, state: GameHandler, maximzingPlayer: Player): ArrayList<MoveData> {
        val newPossibleMoves = ArrayList<MoveData>()
        for (possibleMove in state.allPossibleMovesFromNode(state.ballNode())) {
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

    private fun minmaxEvaluation(state: GameHandler, maximizingPlayer: Player): Double {
        var score = 0.0

        if (state.isGameOver && state.getWinner(state.ballNode())?.winner === maximizingPlayer) score = 1000.0
        if (state.isGameOver && state.getWinner(state.ballNode())?.winner !== maximizingPlayer) score = -1000.0

        score += (-state.numberOfTurns).toDouble()

        val opponentsGoal = state.getOpponent(maximizingPlayer).goalNode
        score += -MathHelper.distance(opponentsGoal!!.xCord, state.ballNode().xCord, opponentsGoal.yCord, state.ballNode().yCord)*2

        val myGoal = maximizingPlayer.goalNode

        //Only one move from the goal
        if (MathHelper.distance(opponentsGoal.xCord, state.ballNode().xCord, opponentsGoal.yCord, state.ballNode().yCord) < 2.0 && state.currentPlayersTurn === maximizingPlayer)
            score = 1000.0

        if (MathHelper.distance(myGoal!!.xCord, state.ballNode().xCord, myGoal.yCord, state.ballNode().yCord) < 2.0 && state.currentPlayersTurn !== maximizingPlayer)
            score = -1000.0

        //Neighbors are bounceable
        state.allPossibleMovesFromNode(state.ballNode()).forEach{
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
