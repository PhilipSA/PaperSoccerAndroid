package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.neuralnetworkAI.INeuralNetworkController
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetwork
import junit.framework.TestCase
import org.junit.Test
import kotlin.random.Random

class NeuralNetworkTests {

    @Test
    fun neuralNetworkLearnsAndOperation() {

        var scoreCounter = 0
        var lastGuess = -1
        val numberOfRuns = 500
        var inputs = listOf(Random.nextInt(0, 2), Random.nextInt(0, 2))

        val neuralNetwork = NeuralNetwork(null, object : INeuralNetworkController {
            override val inputs: List<Int>
                get() = inputs

            override val outputs: Int = 1

            override fun fitnessEvaluation(): Double {
                return if (lastGuess == inputs[0].and(inputs[1])) {
                    ++scoreCounter
                    1.0
                } else -1.0
            }

            override fun networkGuessOutput(output: List<Double>): List<Int> {
                return output.map {
                    if (it >= 0.5) 1 else 0
                }
            }
        }, false)

        for (i in 0 until numberOfRuns) {
            val nextStep = neuralNetwork.nextStep()
            lastGuess = nextStep.first()
            neuralNetwork.cutOff()
            inputs = listOf(Random.nextInt(0, 2), Random.nextInt(0, 2))
        }

        TestCase.assertEquals(numberOfRuns * 0.9, scoreCounter)
    }
}