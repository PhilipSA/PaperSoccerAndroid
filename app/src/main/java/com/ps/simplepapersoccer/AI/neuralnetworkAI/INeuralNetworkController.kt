package com.ps.simplepapersoccer.ai.neuralnetworkAI

interface INeuralNetworkController<T> {
    val inputs: List<Int>
    val outputs: Int
    fun fitnessEvaluation(): Double
    fun networkGuessOutput(output: List<Double>): T?
}