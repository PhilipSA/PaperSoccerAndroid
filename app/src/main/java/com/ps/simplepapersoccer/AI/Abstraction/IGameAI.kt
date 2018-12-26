package com.ps.simplepapersoccer.ai.Abstraction

import com.ps.simplepapersoccer.gameObjects.Game.GameHandler
import com.ps.simplepapersoccer.gameObjects.Move.PartialMove

interface IGameAI {
    fun MakeMove(gameHandler: GameHandler): PartialMove
}
