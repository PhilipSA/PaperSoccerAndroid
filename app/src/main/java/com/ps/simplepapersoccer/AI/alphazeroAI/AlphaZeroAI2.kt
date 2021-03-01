package com.ps.simplepapersoccer.ai.alphazeroAI

import MonteCarloTreeSearch
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer

class AlphaZeroAI2(playerNumber: Int, playerColor: Int, playerName: String = AlphaZeroAI2::class.java.simpleName) : IGameAI, AIPlayer(playerName, playerNumber, playerColor) {
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        val tree = MonteCarloTreeSearch()
        return tree.findNextMove(gameHandler.gameBoard)
    }
}