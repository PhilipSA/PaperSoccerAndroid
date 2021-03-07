package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkCache
import com.ps.simplepapersoccer.ai.neuralnetworkAI.INeuralNetworkController
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetwork
import com.ps.simplepapersoccer.ai.neuralnetworkAI.Pool
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class NeuralNetworkTests {

    var scoreCounter = 0
    var lastGuess = 1f

    private val controller = object : INeuralNetworkController<Float> {
        override var inputs = getRandomInputs()

        override val outputs = 1

        override fun fitnessEvaluation(): Float {
            return if (lastGuess == 1f) {
                ++scoreCounter
                1f
            } else -1f
        }

        override fun networkGuessOutput(output: List<Float>): Float {
            return output.map {
                if (it > 0) 1f else 0f
            }.first()
        }

        override fun updateInputs() {
            this.inputs = getRandomInputs()
        }

        fun getRandomInputs(): List<Float> {
            return listOf(0.5f)
        }
    }

    @Test
    fun neuralNetworkLearnsAndOperation() {

        val numberOfRuns = 500

        val controller = object : INeuralNetworkController<Float> {
            override var inputs = getRandomInputs()

            override val outputs = 1

            override fun fitnessEvaluation(): Float {
                return if (lastGuess == 1f) {
                    ++scoreCounter
                    1f
                } else -1f
            }

            override fun networkGuessOutput(output: List<Float>): Float {
                return output.map {
                    if (it > 0) 1f else 0f
                }.first()
            }

            override fun updateInputs() {
                this.inputs = getRandomInputs()
            }

            fun getRandomInputs(): List<Float> {
                return listOf(0.5f)
            }
        }

        val neuralNetwork = NeuralNetwork(controller, NeuralNetworkCache(null, false))

        for (i in 0 until numberOfRuns) {
            val nextStep = neuralNetwork.nextStep()
            lastGuess = nextStep!!
            neuralNetwork.cutOff()
            controller.updateInputs()
        }

        TestCase.assertEquals(numberOfRuns * 0.9, scoreCounter)
    }

    @Test
    fun canGenerateANetwork() {
        val network = NeuralNetwork(controller, NeuralNetworkCache(null, false))

        Assert.assertEquals(1f, network.nextStep())
    }

    @Test
    fun canCalculateTotalAverageFitness() {

    }

    @Test
    fun canGoToNextGenome() {
        val pool = Pool.getDefault(innovation = 0)
        val network = NeuralNetwork(controller, NeuralNetworkCache(null, false))

        network.nextGenome(pool)

        assert(pool.currentGenomeIndex == 1)
    }
}