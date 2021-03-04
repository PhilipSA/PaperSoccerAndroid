package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.ai.abstraction.IGameAiHandlerListener
import com.ps.simplepapersoccer.gameobjects.game.GameHandler
import com.ps.simplepapersoccer.gameobjects.player.AIPlayer
import kotlinx.coroutines.*
import java.lang.Exception

class GameAIHandler(private val aiHandlerListener: IGameAiHandlerListener,
                    private val dispatcher: CoroutineDispatcher) {
    private var aiTimeOut = false

    fun makeAIMove(aiPlayer: AIPlayer, gameHandler: GameHandler) {
        aiTimeOut = false

        CoroutineScope(dispatcher).launch {
            val job = launch {
                delay(AI_TIMEOUT_MS)
                aiTimeOut = true
            }

            makeAIMoveAsync(aiPlayer, gameHandler, job)
        }
    }

    private suspend fun makeAIMoveAsync(aiPlayer: AIPlayer, gameHandler: GameHandler, job: Job) {
        val gameHandlerHashCode = gameHandler.hashCode()
        val aiMove = aiPlayer.makeMove(gameHandler)
        job.cancel()

        if (gameHandler.hashCode() != gameHandlerHashCode) {
            throw Exception("The AI modified the GameHandler without reverting all changes")
        }

        aiHandlerListener.aiMove(aiMove, aiTimeOut)
    }

    companion object {
        const val AI_TIMEOUT_MS = 500L
    }
}
