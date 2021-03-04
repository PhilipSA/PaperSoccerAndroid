package com.ps.simplepapersoccer.ai.neuralnetworkAI

interface INeuralNetworkController<T> {
    val inputs: List<Float>
    val outputs: Int
    fun updateInputs()
    fun fitnessEvaluation(): Float
    fun networkGuessOutput(output: List<Float>): T?
}