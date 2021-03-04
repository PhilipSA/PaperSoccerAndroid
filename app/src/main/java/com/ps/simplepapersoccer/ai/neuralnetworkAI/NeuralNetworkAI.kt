package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.constants.StringConstants
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameobjects.game.GameHandler
import com.ps.simplepapersoccer.gameobjects.move.PartialMove
import com.ps.simplepapersoccer.gameobjects.move.PossibleMove
import com.ps.simplepapersoccer.gameobjects.player.AIPlayer

class NeuralNetworkAI(context: Context?,
                      playerNumber: Int,
                      playerColor: Int,
                      backUpFileName: String = StringConstants.NEURAL_NETWORK_FILE_NAME,
                      playerName: String = NeuralNetworkAI::class.java.simpleName) : IGameAI, AIPlayer(playerName, playerNumber, playerColor) {

    private var neuralNetwork: NeuralNetwork<PossibleMove>? = null
    private var gameHandler: GameHandler? = null

    val neuralNetworkCache = NeuralNetworkCache(context, true, backUpFileName)

    @ExperimentalStdlibApi
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove? {

        if (this.gameHandler == null) this.gameHandler = gameHandler

        val neuralNetworkController = object : INeuralNetworkController<PossibleMove> {
            override lateinit var inputs: List<Float>

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

                return outputs.maxByOrNull { it.second }?.first
            }

            override fun updateInputs() {
                val nodes = this@NeuralNetworkAI.gameHandler!!.gameBoard.allBaseNodes.map { it.normalizedIdentifierHashCode() }
                val sortedNodes = if (this@NeuralNetworkAI.gameHandler!!.getPlayerPosition(this@NeuralNetworkAI) != 0) nodes.reversed() else nodes
                inputs = sortedNodes
            }
        }

        neuralNetworkController.updateInputs()

        if (neuralNetwork == null) {
            neuralNetwork = NeuralNetwork(neuralNetworkController, neuralNetworkCache, NeuralNetworkParameters())
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
