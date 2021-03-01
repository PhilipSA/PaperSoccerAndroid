package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.constants.StringConstants.NEURAL_NETWORK_FILE_NAME
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
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

        val neuralNetworkController = object : INeuralNetworkController<PossibleMove> {
            override lateinit var inputs: List<Int>

            override val outputs = 8

            override fun fitnessEvaluation(): Float {
                val winner = this@NeuralNetworkAI.gameHandler!!.winner

                return if (winner?.winner == this@NeuralNetworkAI &&
                        (winner.victoryConditionEnum == VictoryConditionEnum.Goal || winner.victoryConditionEnum == VictoryConditionEnum.OpponentOutOfMoves)) 1f
                else this@NeuralNetworkAI.gameHandler!!.numberOfTurns.toFloat() / 200
            }

            override fun networkGuessOutput(output: List<Float>): PossibleMove? {
                val possibleOutputs = this@NeuralNetworkAI.gameHandler!!.gameBoard.allPossibleMovesFromNodeCoords(this@NeuralNetworkAI.gameHandler!!.ballNode)
                val alignedOutputs = if (this@NeuralNetworkAI.gameHandler!!.getPlayerPosition(this@NeuralNetworkAI) != 0) possibleOutputs.reversed() else possibleOutputs

                val outputs = mutableListOf<Pair<PossibleMove, Float>>()
                var maxScore = Float.NEGATIVE_INFINITY

                for (i in output.indices) {
                    val currentOutput = output[i]
                    val possibleOutput = alignedOutputs.getOrNull(i)
                    if (currentOutput > maxScore && possibleOutput?.second == true) {
                        maxScore = currentOutput
                        outputs.add(Pair(possibleOutput.first, currentOutput))
                    }
                }

                return outputs.maxBy { it.second }?.first
            }

            override fun updateInputs() {
                val nodes = this@NeuralNetworkAI.gameHandler!!.gameBoard.allBaseNodes.map { it.normalizedIdentifierHashCode() }
                val sortedNodes = if (this@NeuralNetworkAI.gameHandler!!.getPlayerPosition(this@NeuralNetworkAI) != 0) nodes.reversed() else nodes
                inputs = sortedNodes
            }
        }

        neuralNetworkController.updateInputs()

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
