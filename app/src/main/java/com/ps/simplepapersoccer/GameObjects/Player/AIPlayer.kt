package com.ps.simplepapersoccer.gameObjects.player

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.ai.GameAIHandler.Companion.AI_TIMEOUT_MS
import com.ps.simplepapersoccer.ai.alphazeroAI.AlphaZeroAI
import com.ps.simplepapersoccer.ai.jonasAI.JonasAI
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import java.io.Serializable

class AIPlayer(playerName: String, playerNumber: Int, playerColor: Int, isAi: Boolean) :
        IPlayer(playerName, playerNumber, playerColor, isAi), Serializable {

    val gameAI: IGameAI = when (playerName) {
        EuclideanAI::class.java.simpleName -> EuclideanAI()
        MinimaxAI::class.java.simpleName -> MinimaxAI(AI_TIMEOUT_MS)
        JonasAI::class.java.simpleName -> JonasAI()
        AlphaZeroAI::class.java.simpleName -> AlphaZeroAI(this)
        else -> {
            EuclideanAI()
        }
    }

    companion object {
        val allAi = listOf(EuclideanAI::class.java.simpleName, MinimaxAI::class.java.simpleName, JonasAI::class.java.simpleName, AlphaZeroAI::class.java.simpleName)
    }
}