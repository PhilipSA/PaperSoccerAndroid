package com.ps.simplepapersoccer.AI

import co.metalab.asyncawait.async
import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.AI.MinimaxAI.MinimaxAI
import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler

class GameAIHandler(private val gameHandler: GameHandler, private val difficulty: DifficultyEnum) {
    private var gameAI: IGameAI? = null

    init {
        if (difficulty == DifficultyEnum.Easy) {
            gameAI = EuclideanAI()
        } else if (difficulty == DifficultyEnum.Medium) {
            gameAI = MinimaxAI(1200)
        } else if (difficulty == DifficultyEnum.Hard) {
            gameAI = MinimaxAI(1500)
        } else if (difficulty == DifficultyEnum.VeryHard) {
            gameAI = MinimaxAI(3000)
        }
    }

    fun MakeAIMove() = async {
        var aiMove = await { gameAI?.MakeMove(gameHandler) }
        gameHandler.AIMakeMove(aiMove!!)
    }
}
