package com.ps.simplepapersoccer.gameObjects.Player

import com.ps.simplepapersoccer.ai.Abstraction.IGameAI
import com.ps.simplepapersoccer.ai.EuclideanAI
import com.ps.simplepapersoccer.ai.MinimaxAI.MinimaxAI
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer

class AIPlayer(val difficulty: DifficultyEnum, playerName: String, playerNumber: Int, playerColor: Int, isAi: Boolean) : IPlayer(playerName, playerNumber, playerColor, isAi) {
    var gameAI: IGameAI? = null

    init {
        if (difficulty == DifficultyEnum.Easy) {
            gameAI = EuclideanAI()
        } else if (difficulty == DifficultyEnum.Medium) {
            gameAI = MinimaxAI(1100)
        } else if (difficulty == DifficultyEnum.Hard) {
            gameAI = MinimaxAI(1500)
        } else if (difficulty == DifficultyEnum.VeryHard) {
            gameAI = MinimaxAI(2000)
        }
    }
}