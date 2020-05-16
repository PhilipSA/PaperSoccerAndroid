package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.ai.abstraction.IGameAiHandlerListener
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.Exception

class GameAIHandler(private val aiHandlerListener: IGameAiHandlerListener) {
    var aiTimeOut = false

    fun makeAIMove(aiPlayer : AIPlayer, gameHandler: GameHandler) {
        aiTimeOut = false

        CoroutineScope(Dispatchers.Main).launch {
            val job = launch {
                delay(2000)
                aiTimeOut = true
            }

            makeAIMoveAsync(aiPlayer, gameHandler)
            job.cancel()
        }
    }

    private suspend fun makeAIMoveAsync(aiPlayer : AIPlayer, gameHandler: GameHandler) {
        val gameHandlerHashCode = gameHandler.hashCode()
        val aiMove = aiPlayer.gameAI.makeMove(gameHandler)

        if (gameHandler.hashCode() != gameHandlerHashCode) {
            throw Exception("The ai modified the GameHandler without reverting all changes")
        }

        aiHandlerListener.aiMove(aiMove, aiTimeOut)
    }
}
