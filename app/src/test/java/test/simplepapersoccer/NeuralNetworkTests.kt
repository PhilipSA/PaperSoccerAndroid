package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.neuralnetworkAI.INeuralNetworkController
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetwork
import org.junit.Test
import kotlin.random.Random

class NeuralNetworkTests {

    @Test
    fun neuralNetworkLearnsAnd() {

        var lastScoreCounterValue = 0
        var scoreCounter = 0

        val neuralNetwork = NeuralNetwork(null, object : INeuralNetworkController {
            override val inputs: List<Int>
                get() = listOf(Random.nextInt(0, 2), Random.nextInt(0, 2))

            override val outputs: Int = 2

            override fun fitnessEvaluation(): Double {
                return if (scoreCounter > lastScoreCounterValue) 1.0
                else 0.0
            }
        }, false)

        for (i in 0 until 1000) {
            val nextStep = neuralNetwork.nextStep()
            if (nextStep == 1) ++scoreCounter
            neuralNetwork.cutOff()
            lastScoreCounterValue = scoreCounter
        }

        assert(scoreCounter > 800)
    }
}