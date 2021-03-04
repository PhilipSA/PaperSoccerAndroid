package com.ps.simplepapersoccer.ai.neuralnetworkAI

data class NeuralNetworkParameters(
        val POPULATION: Int = 100,
        val DELTA_DISJOINT: Float = 2f,
        val DELTA_WEIGHTS: Float = 0.4f,
        val DELTA_THRESHOLD: Float = 1f,
        val STALE_SPECIES: Float = POPULATION * 0.075f,
        val MUTATE_CONNECTION_CHANCE: Float = 0.25f,
        val PERTURB_CHANCE: Float = 0.90f,
        val CROSSOVER_CHANCE: Float = 0.75f,
        val LINK_MUTATION_CHANCE: Float = 2f,
        val NODE_MUTATION_CHANCE: Float = 0.50f,
        val BIAS_MUTATION_CHANCE: Float = 0.40f,
        val STEP_SIZE: Float = 0.1f,
        val DISABLE_MUTATION_CHANCE: Float = 0.4f,
        val ENABLE_MUTATION_CHANCE: Float = 0.2f,
        val MAX_NODES: Int = 100000,
)