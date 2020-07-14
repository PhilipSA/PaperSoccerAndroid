package com.ps.simplepapersoccer.ai.neuralnetworkAI

import com.ps.simplepapersoccer.gameObjects.game.GameHandler

interface INeuralNetworkController {
    val inputs: List<Int>
    val outputs: Int
    fun fitnessEvaluation(): Double
    fun networkGuessOutput(output: List<Double>): List<Int>
}