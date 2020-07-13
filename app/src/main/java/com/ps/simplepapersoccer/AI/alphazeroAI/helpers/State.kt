package com.ps.simplepapersoccer.ai.alphazeroAI.helpers

import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class State {
    var board: GameBoard
    val playerNo get() = board.currentPlayersTurn
    var visitCount = 0
    var winScore = 0.0
    lateinit var move: PartialMove

    constructor(state: State) {
        board = state.board
        visitCount = state.visitCount
        winScore = state.winScore
    }

    constructor(gameBoard: GameBoard) {
        board = gameBoard
    }

    val opponent: Int
        get() = 3 - playerNo

    val allPossibleStates: List<State>
        get() {
            val possibleStates: MutableList<State> = ArrayList()
            val availablePositions = board.allPossibleMovesFromNode(board.ballNode)
            availablePositions.forEach { p ->
                val newState = State(board)
                newState.move = PartialMove(p, board.currentPlayersTurn)
                possibleStates.add(newState)
            }
            return possibleStates
        }

    fun incrementVisit() {
        visitCount++
    }

    fun addScore(score: Double) {
        if (winScore != Int.MIN_VALUE.toDouble()) winScore += score
    }

    fun randomPlay() {
        val availablePositions = board.allPossibleMovesFromNode(board.ballNode)
        board.makePartialMove(PartialMove(availablePositions.random(), board.currentPlayersTurn))
    }
}