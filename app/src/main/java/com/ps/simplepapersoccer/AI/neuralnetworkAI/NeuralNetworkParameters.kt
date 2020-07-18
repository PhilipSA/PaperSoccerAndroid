package com.ps.simplepapersoccer.ai.neuralnetworkAI

data class NeuralNetworkParameters(
        val POPULATION: Int = 200,
        val DELTA_DISJOINT: Double = 2.0,
        val DELTA_WEIGHTS: Double = 0.4,
        val DELTA_THRESHOLD: Double = 1.0,
        val STALE_SPECIES: Double = POPULATION * 0.075,
        val MUTATE_CONNECTION_CHANCE: Double = 0.25,
        val PERTURB_CHANCE: Double = 0.90,
        val CROSSOVER_CHANCE: Double = 0.75,
        val LINK_MUTATION_CHANCE: Double = 2.0,
        val NODE_MUTATION_CHANCE: Double = 0.50,
        val BIAS_MUTATION_CHANCE: Double = 0.40,
        val STEP_SIZE: Double = 0.1,
        val DISABLE_MUTATION_CHANCE: Double = 0.4,
        val ENABLE_MUTATION_CHANCE: Double = 0.2,
        val MAX_NODES: Int = 100000,
        val FILE_NAME: String = "temp.pool"
)