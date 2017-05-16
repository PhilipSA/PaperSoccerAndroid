package com.ps.simplepapersoccer.GameObjects.Player

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.AI.EuclideanAI
import com.ps.simplepapersoccer.AI.MinimaxAI.MinimaxAI
import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer

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