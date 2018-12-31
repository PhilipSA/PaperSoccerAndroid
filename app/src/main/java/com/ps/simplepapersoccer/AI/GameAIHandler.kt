package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.runOnBgThread

class GameAIHandler(private val gameHandler: GameHandler, private val calculateAsync: Boolean) {
    fun makeAIMove(aiPlayer : AIPlayer) {
        if(calculateAsync) {
            makeAIMoveAsync(aiPlayer)
            return
        }
        else {
            gameHandler.aiMakeMove(aiPlayer.gameAI.makeMove(gameHandler))
        }
    }

    fun makeAIMoveAsync(aiPlayer : AIPlayer)  {
        runOnBgThread {
            val aiMove = aiPlayer.gameAI.makeMove(gameHandler)
            gameHandler.aiMakeMove(aiMove)
        }
    }
}
