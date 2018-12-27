package com.ps.simplepapersoccer.gameObjects.player

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.ai.EuclideanAI
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer

class AIPlayer(difficulty: DifficultyEnum, playerName: String, playerNumber: Int, playerColor: Int, isAi: Boolean) : IPlayer(playerName, playerNumber, playerColor, isAi) {
    var gameAI: IGameAI = when (difficulty) {
        DifficultyEnum.Easy -> EuclideanAI()
        DifficultyEnum.Medium -> MinimaxAI(1100)
        DifficultyEnum.Hard -> MinimaxAI(1500)
        DifficultyEnum.VeryHard -> MinimaxAI(2000)
    }
}