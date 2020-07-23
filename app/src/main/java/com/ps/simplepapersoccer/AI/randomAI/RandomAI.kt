package com.ps.simplepapersoccer.ai.randomAI

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer

class RandomAI(playerNumber: Int, playerColor: Int, playerName: String = RandomAI::class.java.simpleName): IGameAI, AIPlayer(playerName, playerNumber, playerColor) {
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        return PartialMove(gameHandler.ballNode,
                gameHandler.gameBoard.allLegalMovesFromBallNode.random().newNode,
                gameHandler.gameBoard.currentPlayersTurn)
    }
}