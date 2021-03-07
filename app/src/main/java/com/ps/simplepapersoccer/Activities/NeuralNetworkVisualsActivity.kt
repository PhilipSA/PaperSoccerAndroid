package com.ps.simplepapersoccer.activities

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.ai.neuralnetworkAI.INeuralNetworkController
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetwork
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkCache
import kotlin.random.Random


class NeuralNetworkVisualsActivity: Activity() {

    lateinit var neuralNetwork: NeuralNetwork<Int>
    private var drawNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(NeuralNetworkDrawingView(this))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val numberOfRuns = 500
        var scoreCounter = 0
        var lastGuess = 1

        val controller = object : INeuralNetworkController<Int> {
            override var inputs = getRandomInputs()

            override val outputs = 1

            override fun fitnessEvaluation(): Float {
                return if (lastGuess == 1) {
                    ++scoreCounter
                    1f
                } else -1f
            }

            override fun networkGuessOutput(output: List<Float>): Int {
                return output.map {
                    if (it > 0) 1 else 0
                }.first()
            }

            override fun updateInputs() {
                this.inputs = getRandomInputs()
            }

            fun getRandomInputs(): List<Float> {
                return listOf(Random.nextInt(0, 2), Random.nextInt(0, 2)).map { it.toFloat() }
            }
        }

        neuralNetwork = NeuralNetwork(controller, NeuralNetworkCache(null, false))

        for (i in 0 until numberOfRuns) {
            val nextStep = neuralNetwork.nextStep()
            lastGuess = nextStep!!
            neuralNetwork.cutOff()
            controller.updateInputs()
        }

        drawNetwork = true
        //window.decorView.findViewById<View>(R.id.content).invalidate()
    }

    inner class NeuralNetworkDrawingView(context: Context): View(context)
    {
        init {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            if (drawNetwork) neuralNetwork.displayGenome(neuralNetwork.pool.currentGenome!!, canvas)
        }
    }
}