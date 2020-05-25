package com.ps.simplepapersoccer.ai.abstraction

import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import java.io.Serializable

interface IGameAI: Serializable {
    suspend fun makeMove(gameHandler: GameHandler): PartialMove?
}
