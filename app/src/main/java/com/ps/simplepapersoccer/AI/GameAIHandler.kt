package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.gameObjects.Game.GameHandler
import com.ps.simplepapersoccer.gameObjects.Player.AIPlayer

class GameAIHandler(private val gameHandler: GameHandler, private val calculateAsync: Boolean) {

    fun MakeAIMove(aiPlayer : AIPlayer) {
        if(calculateAsync) {
            MakeAIMoveAsync(aiPlayer)
            return
        }
        else {
            gameHandler.AIMakeMove(aiPlayer.gameAI?.MakeMove(gameHandler)!!)
        }
    }

    fun MakeAIMoveAsync(aiPlayer : AIPlayer)  {
        val aiMove = aiPlayer.gameAI?.MakeMove(gameHandler)
        gameHandler.AIMakeMove(aiMove!!)
    }
}
