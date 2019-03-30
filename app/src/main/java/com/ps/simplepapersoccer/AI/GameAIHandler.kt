package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameAIHandler(private val gameHandler: GameHandler) {
    fun makeAIMove(aiPlayer : AIPlayer) {
        CoroutineScope(Dispatchers.IO).launch {
            makeAIMoveAsync(aiPlayer)
        }
    }

    private suspend fun makeAIMoveAsync(aiPlayer : AIPlayer)  {
        val aiMove = aiPlayer.gameAI.makeMove(gameHandler)
        gameHandler.aiMakeMove(aiMove)
    }
}
