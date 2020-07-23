package com.ps.simplepapersoccer.ai.neuralnetworkAI

interface INeuralNetworkController<T> {
    val inputs: List<Double>
    val outputs: Int
    fun updateInputs()
    fun fitnessEvaluation(): Double
    fun networkGuessOutput(output: List<Double>): T?
}