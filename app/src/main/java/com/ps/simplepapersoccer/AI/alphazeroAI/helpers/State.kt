package com.ps.simplepapersoccer.ai.alphazeroAI.helpers

import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove

class State {
    lateinit var board: GameBoard
    var playerNo = 0
    var visitCount = 0
    var winScore = 0.0

    constructor(state: State) {
        board = state.board
        playerNo = state.playerNo
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
            val availablePositions: HashSet<PossibleMove> = board.allPossibleMovesFromNode(board.ballNode)
            availablePositions.forEach { p ->
                val newState = State(board.copy(board))
                newState.board.makePartialMove(PartialMove(p, board.currentPlayersTurn))
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
        val availablePositions: HashSet<PossibleMove> = board.allPossibleMovesFromNode(board.ballNode)
        board.makePartialMove(PartialMove(availablePositions.random(), board.currentPlayersTurn))
    }

    fun togglePlayer() {
        playerNo = 3 - playerNo
    }
}