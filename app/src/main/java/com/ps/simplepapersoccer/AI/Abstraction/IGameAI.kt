package com.ps.simplepapersoccer.AI.Abstraction

import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove

interface IGameAI {
    fun MakeMove(gameHandler: GameHandler): PartialMove
}
