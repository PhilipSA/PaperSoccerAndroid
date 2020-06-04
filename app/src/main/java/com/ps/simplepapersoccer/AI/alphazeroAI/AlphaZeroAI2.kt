package com.ps.simplepapersoccer.ai.alphazeroAI

import MonteCarloTreeSearch
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove

class AlphaZeroAI2 : IGameAI {
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove? {

        val tree = MonteCarloTreeSearch()
        val board = tree.findNextMove(gameHandler.gameBoard, gameHandler.currentPlayersTurn.playerNumber)

        val bestMove = board.allPartialMoves.firstElement()
        return bestMove.partialMove
    }
}