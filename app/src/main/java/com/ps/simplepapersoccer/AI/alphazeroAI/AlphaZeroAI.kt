package com.ps.simplepapersoccer.AI.alphazeroAI

import com.ps.simplepapersoccer.ai.GameAIHandler
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

class AlphaZeroAI(private val timeLimitMilliSeconds: Long = GameAIHandler.AI_TIMEOUT_MS) : IGameAI {
    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {

    }

    private class DoBeLearnin(private val gameHandler: GameHandler) {
        companion object {
            private val population = 300
            private val deltaDisjoint = 2.0
            private val deltaWeights = 0.4
            private val deltaThreshold = 1.0

            private val staleSpecies = 15

            private const val MUTATE_CONNECTION_CHANCE = 0.25
            private const val PERTURB_CHANCE = 0.90
            private const val CROSSOVER_CHANCE = 0.75
            private const val LINK_MUTATION_CHANCE = 2.0
            private const val NODE_MUTATION_CHANCE = 0.50
            private const val BIAS_MUTATION_CHANCE = 0.40
            private const val STEP_SIZE = 0.1
            private const val DISABLE_MUTATION_CHANCE = 0.4
            private const val ENABLE_MUTATION_CHANCE = 0.2

            private val timeoutConstant = 20

            private val maxNodes = 1000000
        }

        data class Neuron(
                val incoming: MutableList<Gene>,
                var value: Double
        )

        data class MutationRates(
                val connections: Double = MUTATE_CONNECTION_CHANCE,
                val link: Double = LINK_MUTATION_CHANCE,
                val bias: Double = BIAS_MUTATION_CHANCE,
                val node: Double = NODE_MUTATION_CHANCE,
                val enable: Double = ENABLE_MUTATION_CHANCE,
                val disable: Double = DISABLE_MUTATION_CHANCE,
                val step: Double = STEP_SIZE
        )

        data class Genome(
                val genes: MutableList<Gene>,
                val fitness: Int,
                val adjustedFitness: Int,
                var network: Network?,
                var maxNeuron: Int,
                val globalRank: Int,
                val mutationRates: MutationRates
        )

        data class Species(
                val topFitness: Int,
                val staleness: Int,
                val genomes: HashSet<Genome>,
                val averageFitness: Int
        )

        data class Pool(
                val species: HashSet<Species>,
                val generation: Int,
                val currentSpecies: Int,
                val currentGenome: Int,
                val maxFitness: Int,
                var innovation = HashSet<>
        )

        data class Gene(
                val into: Int,
                val out: Int,
                var weight: Double,
                val enabled: Boolean,
                val innovation: Int
        )

        data class Network(
                val neurons: HashMap<Int, Neuron>
        )

        private val outputs get() = gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode)
        private val inputs = hashSetOf(gameHandler.gameBoard.hashCode())

        private fun sigmoid(x: Double): Double {
            return 2 / (1 + exp(-4.9 * x)) - 1
        }

        val pool = Pool()

        private fun newInnovation() {
            pool.innovation = pool.innovation + 1
            return pool.innovation
        }

        private fun newGenome(): Genome {
            return Genome(arrayListOf(), 0, 0, null, 0, 0, MutationRates())
        }

        private fun copyGenome(genome: Genome): Genome {
            return genome.copy(genes = genome.genes,
                    fitness = genome.fitness,
                    adjustedFitness = genome.adjustedFitness,
                    network = genome.network,
                    maxNeuron = genome.maxNeuron,
                    globalRank = genome.globalRank,
                    mutationRates = genome.mutationRates)
        }

        private fun basicGenome(): Genome {
            return Genome(maxNeuron = inputs)
        }

        private fun newGene(): Gene {
            return Gene(0, 0, 0.0, true, 0)
        }

        private fun copyGene(gene: Gene): Gene {
            return gene.copy(into = gene.into,
                    out = gene.out,
                    weight = gene.weight,
                    enabled = gene.enabled,
                    innovation = gene.innovation)
        }

        private fun newNeuron(): Neuron {
            return Neuron(hashSetOf(), 0.0)
        }

        private fun generateNetwork(genome: Genome) {
            val network = Network(hashMapOf())

            network.neurons[0] = newNeuron()

            for ((index, x) in outputs.withIndex()) {
                network.neurons[maxNodes + index] = newNeuron()
            }

            val sortedGenomes = genome.genes.sortedBy { it.out }

            for ((index, x) in sortedGenomes.withIndex()) {
                val gene = genome.genes[index]
                if (gene.enabled) {
                    if (network.neurons[gene.out] == null) {
                        network.neurons[gene.out] = newNeuron()
                    }
                    val neuron = network.neurons[gene.out]
                    neuron?.incoming?.add(gene)
                    if (network.neurons[gene.into] == null) {
                        network.neurons[gene.into] = newNeuron()
                    }
                }
            }

            genome.network = network
        }

        private fun evaluateNetwork(network: Network, input: GameBoard) {
            inputs.add(input.hashCode())

            for ((index, x) in inputs.withIndex()) {
                network.neurons[index]?.value = x
            }

            for ((i, neuron) in network.neurons.entries.withIndex()) {
                var sum = 0.0

                for ((j, x) in neuron.value.incoming) {
                    val incoming = neuron.value.incoming[j]
                    val other = network.neurons[incoming.into]!!

                    sum += incoming.weight * other.value
                }

                if (neuron.value.incoming > 0) {
                    neuron.value.value = sigmoid(sum)
                }
            }
        }

        private fun crossover(genome1: Genome, genome2: Genome): Genome {
            var genome1 = genome1
            var genome2 = genome2

            if (genome2.fitness > genome1.fitness) {
                val tempg = genome1
                genome1 = genome2
                genome2 = tempg
            }

            val child = newGenome()

            val innovations2 = HashMap<Int, Gene>()

            for ((i, _) in genome2.genes) {
                val gene = genome2.genes[i]
                innovations2[gene.innovation] = gene
            }

            for ((i, _) in genome1.genes) {
                val gene1 = genome1.genes[i]
                val gene2 = innovations2[gene1.innovation]

                if (gene2 != null && Random.nextInt(2) == 1 && gene2.enabled) {
                    child.genes.add(copyGene(gene2))
                } else {
                    child.genes.add(copyGene(gene1))
                }
            }

            child.maxNeuron = max(genome1.maxNeuron, genome2.maxNeuron)

            for ((mutation, rate) in genome1.mutationRates) {
                child.mutationRates[mutation] = rate
            }

            return child
        }

        private fun randomNeuron(genes: MutableList<Gene>, nonInput: Boolean): Int {
            val neurons = mutableListOf<Boolean>()

            if (nonInput.not()) {
                for ((i, x) in inputs.withIndex()) {
                    neurons[i] = true
                }
            }

            for ((i, x) in outputs.withIndex()) {
                neurons[maxNodes+i] = true
            }

            for ((i, x) in genes) {
                if (nonInput.not() || genes[i].into > inputs.size) {
                    neurons[genes[i].into] = true
                }

                if (nonInput.not() || genes[i].out > inputs.size) {
                    neurons[genes[i].out] = true
                }
            }

            val count = neurons.size
            var n = Random.nextInt(1, count)

            for ((i, x) in outputs.withIndex()) {
                n -= 1
                if (n == 0) return i
            }

            return 0
        }

        private fun containsLink(genes: MutableList<Gene>, link: Gene): Boolean {
            for ((i, x) in genes.withIndex()) {
                val gene = genes[i]
                if (gene.into == link.into && gene.out == link.out) {
                    return true
                }
            }
            return false
        }

        //TODO: Mimic LUA math.random
        private fun pointMutate(genome: Genome) {
            val step = genome.mutationRates.step

            for ((i, x) in genome.genes.withIndex()) {
                val gene = genome.genes[i]

                if (Random.nextDouble(0.0, 1.0) < PERTURB_CHANCE) {
                    gene.weight = gene.weight + Random.nextDouble(0.0, 1.0) * step*2 - step
                } else {
                    gene.weight = Random.nextDouble(0.0, 1.0)*4 - 2
                }
            }
        }
    }
}
