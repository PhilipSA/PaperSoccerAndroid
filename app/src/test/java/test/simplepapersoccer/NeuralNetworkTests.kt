package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.neuralnetworkAI.INeuralNetworkController
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetwork
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkParameters
import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import junit.framework.TestCase
import org.junit.Test
import kotlin.random.Random

class NeuralNetworkTests {

    @Test
    fun neuralNetworkLearnsAndOperation() {

        var scoreCounter = 0
        var lastGuess = -1
        val numberOfRuns = 5000
        var inputs = listOf(Random.nextInt(0, 2), Random.nextInt(0, 2))

        val controller = object : INeuralNetworkController<Int> {
            override var inputs: List<Double> = inputs.map { it.toDouble() }

            override val outputs = 1

            override fun fitnessEvaluation(): Double {
                return if (lastGuess == inputs[0].and(inputs[1])) {
                    ++scoreCounter
                    1.0
                } else -1.0
            }

            override fun networkGuessOutput(output: List<Double>): Int {
                return output.map {
                    if (it >= 0) 1 else 0
                }.first()
            }

            override fun updateInputs() {
                this.inputs = listOf(Random.nextInt(0, 2), Random.nextInt(0, 2)).map { it.toDouble() }
            }
        }

        val neuralNetwork = NeuralNetwork(null, controller, false)

        for (i in 0 until numberOfRuns) {
            val nextStep = neuralNetwork.nextStep()
            lastGuess = nextStep ?: -1
            neuralNetwork.cutOff()
            controller.updateInputs()
        }

        TestCase.assertEquals(numberOfRuns * 0.9, scoreCounter)
    }
}