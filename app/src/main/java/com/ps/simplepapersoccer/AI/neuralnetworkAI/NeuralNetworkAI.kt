package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.constants.StringConstants.NEURAL_NETWORK_FILE_NAME
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer

class NeuralNetworkAI(private val context: Context?,
                      playerNumber: Int,
                      playerColor: Int,
                      playerName: String = NeuralNetworkAI::class.java.simpleName,
                      private val backupFileName: String = NEURAL_NETWORK_FILE_NAME) : IGameAI, AIPlayer(playerName, playerNumber, playerColor) {

    private var neuralNetwork: NeuralNetwork<PossibleMove>? = null
    private var gameHandler: GameHandler? = null

    @ExperimentalStdlibApi
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove? {

        if (this.gameHandler == null) this.gameHandler = gameHandler

        val neuralNetworkController = object: INeuralNetworkController<PossibleMove> {
            override val inputs: List<Int> get() {
                val nodes = this@NeuralNetworkAI.gameHandler!!.gameBoard.allNodesHashSet.toList().sortedBy { it.coords }.map { it.identifierHashCode() }
                return if (this@NeuralNetworkAI.gameHandler!!.getPlayerPosition(this@NeuralNetworkAI) != 0) nodes.reversed() else nodes
            }

            override val outputs = 8

            override fun fitnessEvaluation(): Double {
                return if (this@NeuralNetworkAI.gameHandler!!.winner?.winner == this@NeuralNetworkAI) 1.0
                else this@NeuralNetworkAI.gameHandler!!.numberOfTurns.toDouble() / 100
            }

            override fun networkGuessOutput(output: List<Double>): PossibleMove? {
                val possibleOutputs = this@NeuralNetworkAI.gameHandler!!.gameBoard.allPossibleMovesFromNodeCoords(this@NeuralNetworkAI.gameHandler!!.ballNode)
                val alignedOutputs = if (this@NeuralNetworkAI.gameHandler!!.getPlayerPosition(this@NeuralNetworkAI) != 0) possibleOutputs.reversed() else possibleOutputs

                val outputs = mutableListOf<PossibleMove>()

                for (i in output.indices) {
                    val currentOutput = output[i]
                    val possibleOutput = alignedOutputs.getOrNull(i)
                    if (currentOutput == output.max() && possibleOutput != null) {
                        outputs.add(possibleOutput)
                    }
                }

                return outputs.randomOrNull()
            }
        }

        if (neuralNetwork == null) {
            neuralNetwork = NeuralNetwork(context, neuralNetworkController, true, NeuralNetworkParameters(FILE_NAME = backupFileName))
        }

        //New game
        if (this.gameHandler != gameHandler) {
            neuralNetwork?.cutOff()
            this.gameHandler = gameHandler
        }

        val guessedPossibleMoves = neuralNetwork!!.nextStep()

        return guessedPossibleMoves?.let { PartialMove(it, this.playerNumber) }
    }
}
