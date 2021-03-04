package com.ps.simplepapersoccer.ai.abstraction

import com.ps.simplepapersoccer.gameobjects.game.GameHandler
import com.ps.simplepapersoccer.gameobjects.move.PartialMove
import java.io.Serializable

interface IGameAI: Serializable {
    suspend fun makeMove(gameHandler: GameHandler): PartialMove?
}
