package com.ps.simplepapersoccer.ai.neuralnetworkAI

interface INeuralNetworkController<T> {
    val inputs: List<Int>
    val outputs: Int
    fun updateInputs()
    fun fitnessEvaluation(): Float
    fun networkGuessOutput(output: List<Float>): T?
}