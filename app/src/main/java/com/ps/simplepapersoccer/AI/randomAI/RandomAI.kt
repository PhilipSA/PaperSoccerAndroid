package com.ps.simplepapersoccer.ai.randomAI

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove

class RandomAI(): IGameAI {
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        return PartialMove(gameHandler.ballNode, gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode).random().newNode, gameHandler.currentPlayersTurn)
    }
}