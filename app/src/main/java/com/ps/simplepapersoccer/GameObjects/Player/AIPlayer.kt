package com.ps.simplepapersoccer.gameObjects.player

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.ai.EuclideanAI
import com.ps.simplepapersoccer.ai.GameAIHandler.Companion.AI_TIMEOUT_MS
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import java.io.Serializable

class AIPlayer(difficulty: DifficultyEnum, playerName: String, playerNumber: Int, playerColor: Int, isAi: Boolean) :
        IPlayer(playerName, playerNumber, playerColor, isAi), Serializable {

    var gameAI: IGameAI = when (difficulty) {
        DifficultyEnum.Easy -> EuclideanAI()
        DifficultyEnum.VeryHard -> MinimaxAI(AI_TIMEOUT_MS)
    }
}