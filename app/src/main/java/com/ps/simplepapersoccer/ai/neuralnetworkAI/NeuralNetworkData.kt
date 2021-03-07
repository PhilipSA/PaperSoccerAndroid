package com.ps.simplepapersoccer.ai.neuralnetworkAI

import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

enum class NeuronType {
    Input,
    Bias,
    Output,
    HiddenLayer
}

data class Neuron(
        val incoming: HashSet<Gene>,
        var value: Float,
        var neuronType: NeuronType
) : Serializable {
    val isInputNeuron get() = neuronType == NeuronType.Input
    val isOutputNeuron get() = neuronType == NeuronType.Output

    companion object {
        fun getDefault(neuronType: NeuronType): Neuron {
            return Neuron(hashSetOf(), 0f, neuronType)
        }
    }
}


class Genome(
        val genes: MutableList<Gene>,
        var fitness: Float,
        val adjustedFitness: Int,
        var network: Network,
        var maxNeuron: NeuronIndex?,
        var globalRank: Int,
        val mutationRates: HashMap<String, Float>
) : Serializable {
    fun nextMaxNeuron() {
        maxNeuron = maxNeuron?.let { NeuronIndex(it.index + 1, it.neuronType) }
    }

    fun getNeuronByIndex(neuronIndex: NeuronIndex?): Neuron? {
        return network.getNeuronByIndex(neuronIndex)
    }

    constructor(network: Network, neuralNetworkParameters: NeuralNetworkParameters) : this(mutableListOf(), 0f, 0, network, null, 0, hashMapOf("connections" to neuralNetworkParameters.MUTATE_CONNECTION_CHANCE,
            "link" to neuralNetworkParameters.LINK_MUTATION_CHANCE,
            "bias" to neuralNetworkParameters.BIAS_MUTATION_CHANCE,
            "node" to neuralNetworkParameters.NODE_MUTATION_CHANCE,
            "enable" to neuralNetworkParameters.ENABLE_MUTATION_CHANCE,
            "disable" to neuralNetworkParameters.DISABLE_MUTATION_CHANCE,
            "step" to neuralNetworkParameters.STEP_SIZE))
}

data class Species(
        var topFitness: Float,
        var staleness: Int,
        val genomes: MutableList<Genome>,
        var averageFitness: Float
) : Serializable {
    companion object {
        fun getDefault(topFitness: Float = 0f,
                       staleness: Int = 0,
                       genomes: MutableList<Genome> = mutableListOf(),
                       averageFitness: Float = 0f): Species {
            return Species(topFitness, staleness, genomes, averageFitness)
        }
    }
}

data class Pool(
        var species: MutableList<Species>,
        var generation: Int,
        var currentSpeciesIndex: Int,
        var currentGenomeIndex: Int,
        var maxFitness: Float,
        var innovation: Int
) : Serializable {
    val currentSpecies get() = species.getOrNull(currentSpeciesIndex)
    val currentGenome get() = currentSpecies?.genomes?.getOrNull(currentGenomeIndex)

    companion object {
        fun getDefault(species: MutableList<Species> = mutableListOf(),
                       generation: Int = 0,
                       currentSpeciesIndex: Int = 0,
                       currentGenomeIndex: Int = 0,
                       maxFitness: Float = 0f,
                       innovation: Int): Pool {
            return Pool(species, generation, currentSpeciesIndex, currentGenomeIndex, maxFitness, innovation)
        }
    }
}

data class NeuronIndex(val index: Int, val neuronType: NeuronType) : Serializable

data class Gene(
        var intoNeuronIndex: NeuronIndex?,
        var outNeuronIndex: NeuronIndex?,
        var weight: Float,
        var enabled: Boolean,
        var innovation: Int
) : Serializable {
    companion object {
        fun getDefault(): Gene {
            return Gene(null, null, 0f, true, 0)
        }
    }
}

data class Network(
        val inputNeurons: MutableList<Neuron>,
        val hiddenLayerNeurons: MutableList<Neuron>,
        val outputNeurons: MutableList<Neuron>
) : Serializable {
    val allNeurons get() = inputNeurons.plus(outputNeurons).plus(hiddenLayerNeurons)

    fun getNeuronByIndex(neuronIndex: NeuronIndex?): Neuron? {
        return when (neuronIndex?.neuronType) {
            NeuronType.Input -> inputNeurons.getOrNull(neuronIndex.index)
            NeuronType.Bias -> inputNeurons.getOrNull(neuronIndex.index)
            NeuronType.HiddenLayer -> hiddenLayerNeurons.getOrNull(neuronIndex.index)
            NeuronType.Output -> outputNeurons.getOrNull(neuronIndex.index)
            else -> null
        }
    }
}

data class PoolDto(
        val uncompressedSize: Int,
        val pool: ByteArray
) : Serializable