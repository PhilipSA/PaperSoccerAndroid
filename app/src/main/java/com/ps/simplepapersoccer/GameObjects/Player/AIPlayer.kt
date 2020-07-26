package com.ps.simplepapersoccer.gameObjects.player

import android.content.Context
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.ai.GameAIHandler.Companion.AI_TIMEOUT_MS
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkAI
import com.ps.simplepapersoccer.ai.alphazeroAI.AlphaZeroAI2
import com.ps.simplepapersoccer.ai.jonasAI.JonasAI
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.ai.randomAI.RandomAI
import com.ps.simplepapersoccer.data.constants.StringConstants.NEURAL_NETWORK_FILE_NAME
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import java.io.Serializable

abstract class AIPlayer(playerName: String, playerNumber: Int, playerColor: Int) :
        IPlayer(playerName, playerNumber, playerColor, true), Serializable, IGameAI {

    companion object {
        val allAi = listOf(EuclideanAI::class.java.simpleName,
                MinimaxAI::class.java.simpleName,
                JonasAI::class.java.simpleName,
                NeuralNetworkAI::class.java.simpleName,
                AlphaZeroAI2::class.java.simpleName,
                RandomAI::class.java.simpleName)

        fun createAi(context: Context?,
                     playerName: String,
                     playerNumber: Int,
                     playerColor: Int): AIPlayer = when (playerName) {
            EuclideanAI::class.java.simpleName -> EuclideanAI(playerNumber, playerColor)
            MinimaxAI::class.java.simpleName -> MinimaxAI(playerNumber, playerColor)
            JonasAI::class.java.simpleName -> JonasAI(playerNumber, playerColor)
            NeuralNetworkAI::class.java.simpleName -> NeuralNetworkAI(context, playerNumber, playerColor, backupFileName =  NEURAL_NETWORK_FILE_NAME)
            AlphaZeroAI2::class.java.simpleName -> AlphaZeroAI2(playerName, playerNumber, playerColor)
            RandomAI::class.java.simpleName -> RandomAI(playerNumber, playerColor, playerName)
            else -> {
                EuclideanAI(playerNumber, playerColor)
            }
        }
    }
}

