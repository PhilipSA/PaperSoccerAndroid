package com.ps.simplepapersoccer.ai.abstraction

import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove

interface IGameAI {
    suspend fun makeMove(gameHandler: GameHandler): PartialMove
}
