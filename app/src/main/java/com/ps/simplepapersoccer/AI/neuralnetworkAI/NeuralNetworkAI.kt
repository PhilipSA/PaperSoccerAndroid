package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer

class NeuralNetworkAI(private val context: Context?, private val aiPlayer: AIPlayer) : IGameAI {

    private var neuralNetwork: NeuralNetwork? = null
    private var gameHandler: GameHandler? = null

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove? {

        val neuralNetworkController = object: INeuralNetworkController {
            override val inputs: List<Int> get() {
                val nodes = gameHandler.gameBoard.nodeHashSet.toList().sortedBy { it.coords }.map { it.identifierHashCode() }
                return if (gameHandler.getPlayerPosition(aiPlayer) != 0) nodes.reversed() else nodes
            }
            override val outputs: Int = 8

            override fun fitnessEvaluation(): Double {
                return if (gameHandler.winner?.winner == aiPlayer) 1.0
                else -1.0
            }

            override fun networkGuessOutput(output: List<Double>): List<Int> {
                return listOf(output.indexOf(output.max()))
            }
        }

        if (neuralNetwork == null) {
            neuralNetwork = NeuralNetwork(context, neuralNetworkController, true)
        }

        if (this.gameHandler == null) this.gameHandler = gameHandler

        //New game
        if (this.gameHandler != gameHandler) {
            neuralNetwork?.cutOff()
            this.gameHandler = gameHandler
        }

        val highestValue = neuralNetwork!!.nextStep()

        val nextMove = gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode).getOrNull(neuralNetwork!!.nextStep().indexOf(highestValue.max()))

        return nextMove?.let { PartialMove(it, aiPlayer.playerNumber) }
    }
}
