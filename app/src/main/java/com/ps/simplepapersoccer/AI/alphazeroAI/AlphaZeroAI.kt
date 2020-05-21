package com.ps.simplepapersoccer.ai.alphazeroAI

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.move.PossibleMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.helpers.MathHelper
import com.ps.simplepapersoccer.helpers.PathFindingHelper
import java.io.File
import kotlin.math.*
import kotlin.random.Random

class AlphaZeroAI(private val context: Context, private val aiPlayer: AIPlayer) : IGameAI {

    private var neuralNetwork: NeuralNetwork? = null

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        if (neuralNetwork == null) {
            neuralNetwork = NeuralNetwork(context, gameHandler, aiPlayer)
        }

        //New game
        if (neuralNetwork?.gameHandler != gameHandler) {
            neuralNetwork?.cutOff()
            neuralNetwork?.gameHandler = gameHandler
        }

        return neuralNetwork!!.nextMove()
    }

    private class NeuralNetwork(private val context: Context, var gameHandler: GameHandler, private val aiPlayer: AIPlayer) {
        companion object {
            private const val POPULATION = 300
            private const val DELTA_DISJOINT = 2.0
            private const val DELTA_WEIGHTS = 0.4
            private const val DELTA_THRESHOLD = 1.0

            private const val STALE_SPECIES = 15

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

            private const val MAX_NODES = 1000000

            private const val FILE_NAME = "temp.pool"
        }

        private data class Neuron(
                val incoming: MutableList<Gene>,
                var value: Double
        )

        private data class MutationRates (
                var mutation: Double = 0.0,
                var connections: Double = MUTATE_CONNECTION_CHANCE,
                var link: Double = LINK_MUTATION_CHANCE,
                var bias: Double = BIAS_MUTATION_CHANCE,
                var node: Double = NODE_MUTATION_CHANCE,
                var enable: Double = ENABLE_MUTATION_CHANCE,
                var disable: Double = DISABLE_MUTATION_CHANCE,
                var step: Double = STEP_SIZE
        ) {
            fun getValues() = listOf(connections, link, bias, node, enable, disable, step)
        }

        private data class Genome(
                val genes: MutableList<Gene>,
                var fitness: Double,
                val adjustedFitness: Int,
                var network: Network,
                var maxNeuron: Int,
                var globalRank: Int,
                val mutationRates: MutationRates
        )

        private data class Species(
                var topFitness: Double,
                var staleness: Int,
                val genomes: MutableList<Genome>,
                var averageFitness: Int
        )

        private data class Pool(
                var species: MutableList<Species>,
                var generation: Int,
                var currentSpecies: Int,
                var currentGenome: Int,
                var maxFitness: Double,
                var innovation: Int
        )

        private data class Gene(
                var into: Int,
                var out: Int,
                var weight: Double,
                var enabled: Boolean,
                var innovation: Int
        )

        private data class Network(
                val neurons: HashMap<Int, Neuron>
        )

        lateinit var pool: Pool
        private val outputs get() = gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode)
        private val inputs get() = gameHandler.gameBoard.nodeHashSet.size + 1

        init {
            initPool()
        }

        private fun sigmoid(x: Double): Double {
            return 2 / (1 + exp(-4.9 * x)) - 1
        }

        private fun newInnovation(): Int {
            pool.innovation = pool.innovation + 1
            return pool.innovation
        }

        private fun newPool(): Pool {
            return Pool(mutableListOf(), 0, 0, 0, 0.0, outputs.size)
        }

        private fun newSpecies(): Species {
            return Species(0.0, 0, mutableListOf(), 0)
        }

        private fun newGenome(): Genome {
            return Genome(arrayListOf(), 0.0, 0, Network(hashMapOf()), 0, 0, MutationRates())
        }

        private fun copyGenome(genome: Genome): Genome {
            val newGenes = genome.genes.map {
                copyGene(it)
            }.toMutableList()

            return genome.copy(genes = newGenes,
                    fitness = genome.fitness,
                    adjustedFitness = genome.adjustedFitness,
                    network = genome.network,
                    maxNeuron = genome.maxNeuron,
                    globalRank = genome.globalRank,
                    mutationRates = genome.mutationRates)
        }

        private fun basicGenome(): Genome {
            val genome = newGenome()
            genome.maxNeuron = inputs
            mutate(genome)
            return genome
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
            return Neuron(mutableListOf(), 0.0)
        }

        private fun generateNetwork(genome: Genome) {
            val network = Network(hashMapOf())

            for (i in 0 until inputs) {
                network.neurons[i] = newNeuron()
            }

            for ((index, _) in outputs.withIndex()) {
                network.neurons[MAX_NODES + index] = newNeuron()
            }

            genome.genes.sortBy { it.out }

            genome.genes.forEach { gene ->
                if (gene.enabled) {
                    if (network.neurons[gene.out] == null) {
                        network.neurons[gene.out] = newNeuron()
                    }
                    val neuron = network.neurons[gene.out]
                    neuron!!.incoming.add(gene)
                    if (network.neurons[gene.into] == null) {
                        network.neurons[gene.into] = newNeuron()
                    }
                }
            }

            genome.network = network
        }

        private fun evaluateNetwork(network: Network, inputsArg: MutableList<Double>): MutableList<PossibleMove> {
            if (inputsArg.size != inputs) {
                return mutableListOf()
            }

            for (index in 0 until inputs) {
                network.neurons[index]?.value = inputsArg[index]
            }

            network.neurons.values.forEach { neuron ->
                var sum = 0.0

                for ((j, x) in neuron.incoming.withIndex()) {
                    val incoming = neuron.incoming[j]
                    val other = network.neurons[incoming.into]

                    sum += incoming.weight * other!!.value
                }

                if (neuron.incoming.size > 0) {
                    neuron.value = sigmoid(sum)
                }
            }

            val validOutputs = mutableListOf<PossibleMove>()

            for ((index, _) in outputs.withIndex()) {
                val move = outputs.toList()[index]

                if (network.neurons[MAX_NODES + index]?.value ?: 0.0 > 0) {
                    validOutputs.add(move)
                }
            }

            return if (validOutputs.isNotEmpty()) validOutputs else outputs.toMutableList()
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

            genome2.genes.forEach { gene ->
                innovations2[gene.innovation] = gene
            }

            for ((i, _) in genome1.genes.withIndex()) {
                val gene1 = genome1.genes[i]
                val gene2 = innovations2[gene1.innovation]

                if (gene2 != null && Random.nextInt(2) == 1 && gene2.enabled) {
                    child.genes.add(copyGene(gene2))
                } else {
                    child.genes.add(copyGene(gene1))
                }
            }

            child.maxNeuron = max(genome1.maxNeuron, genome2.maxNeuron)

            for ((mutation, rate) in genome1.mutationRates.getValues().withIndex()) {
                if (mutation == 0) child.mutationRates.connections = rate
                if (mutation == 1) child.mutationRates.link = rate
                if (mutation == 2) child.mutationRates.bias = rate
                if (mutation == 3) child.mutationRates.node = rate
                if (mutation == 4) child.mutationRates.enable = rate
                if (mutation == 5) child.mutationRates.disable = rate
                if (mutation == 6) child.mutationRates.step = rate
            }

            return child
        }

        private fun randomNeuron(genes: MutableList<Gene>, nonInput: Boolean): Int {
            val neurons = hashMapOf<Int, Boolean>()

            if (nonInput.not()) {
                for (i in 0 until inputs) {
                    neurons[i] = true
                }
            }

            for ((i, _) in outputs.withIndex()) {
                neurons[MAX_NODES + i] = true
            }

            for (i in 0 until genes.size) {
                if (nonInput.not() || genes[i].into > inputs) {
                    neurons[genes[i].into] = true
                }

                if (nonInput.not() || genes[i].out > inputs) {
                    neurons[genes[i].out] = true
                }
            }

            val count = neurons.size
            var n = Random.nextInt(1, count)

            for ((k, _) in neurons) {
                n -= 1
                if (n == 0) return k
            }

            return 0
        }

        private fun containsLink(genes: MutableList<Gene>, link: Gene): Boolean {
            genes.forEach { gene ->
                if (gene.into == link.into && gene.out == link.out) {
                    return true
                }
            }
            return false
        }

        private fun pointMutate(genome: Genome) {
            val step = genome.mutationRates.step

            genome.genes.forEach { gene ->
                if (Random.nextDouble(0.0, 1.0) < PERTURB_CHANCE) {
                    gene.weight = gene.weight + Random.nextDouble(0.0, 1.0) * step * 2 - step
                } else {
                    gene.weight = Random.nextDouble(0.0, 1.0) * 4 - 2
                }
            }
        }

        private fun linkMutate(genome: Genome, forceBias: Boolean) {
            var neuron1 = randomNeuron(genome.genes, false)
            var neuron2 = randomNeuron(genome.genes, true)

            val newLink = newGene()

            if (neuron1 <= inputs && neuron2 <= inputs) {
                return
            }

            if (neuron2 <= inputs) {
                val temp = neuron1
                neuron1 = neuron2
                neuron2 = temp
            }

            newLink.into = neuron1
            newLink.out = neuron2

            if (forceBias) newLink.into = inputs

            if (containsLink(genome.genes, newLink)) return

            newLink.innovation = newInnovation()
            newLink.weight = Random.nextDouble(0.0, 1.0) * 4 - 2

            genome.genes.add(newLink)
        }

        private fun nodeMutate(genome: Genome) {
            if (genome.genes.size == 0) return

            ++genome.maxNeuron

            val gene = genome.genes[Random.nextInt(0, genome.genes.size)]

            if (gene.enabled.not()) return
            gene.enabled = false

            val gene1 = copyGene(gene)
            gene1.out = genome.maxNeuron
            gene1.weight = 1.0
            gene1.innovation = newInnovation()
            gene1.enabled = true
            genome.genes.add(gene1)

            val gene2 = copyGene(gene)
            gene2.into = genome.maxNeuron
            gene2.innovation = newInnovation()
            gene2.enabled = true
            genome.genes.add(gene2)
        }

        private fun enableDisableMutate(genome: Genome, enable: Boolean) {
            val candidates = mutableListOf<Gene>()

            genome.genes.forEach { gene ->
                if (gene.enabled == enable.not()) {
                    candidates.add(gene)
                }
            }

            if (candidates.size == 0) return

            val gene = candidates[Random.nextInt(0, candidates.size)]
            gene.enabled = gene.enabled.not()
        }

        private fun mutateHelper(rate: Double): Double {
            return if (Random.nextInt(1, 2) == 1) {
                0.95 * rate
            } else {
                1.05263 * rate
            }
        }

        private fun mutate(genome: Genome) {
            for ((mutation, rate) in genome.mutationRates.getValues().withIndex()) {
                val value = mutateHelper(rate)

                if (mutation == 0) genome.mutationRates.connections = value
                if (mutation == 1) genome.mutationRates.link = value
                if (mutation == 2) genome.mutationRates.bias = value
                if (mutation == 3) genome.mutationRates.node = value
                if (mutation == 4) genome.mutationRates.enable = value
                if (mutation == 5) genome.mutationRates.disable = value
                if (mutation == 6) genome.mutationRates.step = value
            }

            if (Random.nextDouble(0.0, 1.0) < genome.mutationRates.connections) {
                pointMutate(genome)
            }

            var p = genome.mutationRates.link

            while (p > 0) {
                if (Random.nextDouble(0.0, 1.0) < p) {
                    linkMutate(genome, false)
                }
                --p
            }

            p = genome.mutationRates.bias
            while (p > 0) {
                if (Random.nextDouble(0.0, 1.0) < p) {
                    linkMutate(genome, true)
                }
                --p
            }

            p = genome.mutationRates.node
            while (p > 0) {
                if (Random.nextDouble(0.0, 1.0) < p) {
                    nodeMutate(genome)
                }
                --p
            }


            p = genome.mutationRates.enable
            while (p > 0) {
                if (Random.nextDouble(0.0, 1.0) < p) {
                    enableDisableMutate(genome, true)
                }
                --p
            }

            p = genome.mutationRates.disable
            while (p > 0) {
                if (Random.nextDouble(0.0, 1.0) < p) {
                    enableDisableMutate(genome, false)
                }
                --p
            }
        }

        private fun disjoint(genes1: List<Gene>, genes2: List<Gene>): Int {
            val i1 = HashMap<Int, Boolean>()

            genes1.forEach {
                i1[it.innovation] = true
            }

            val i2 = HashMap<Int, Boolean>()

            genes2.forEach {
                i1[it.innovation] = true
            }

            var disjointGenes = 0

            genes1.forEach {
                if (i2[it.innovation]?.not() == true) {
                    ++disjointGenes
                }
            }

            genes2.forEach {
                if (i1[it.innovation]?.not() == true) {
                    ++disjointGenes
                }
            }

            val n = max(genes1.size, genes2.size)

            return if (n == 0) 0 else disjointGenes / n
        }

        private fun weights(genes1: List<Gene>, genes2: List<Gene>): Double {
            val i2 = HashMap<Int, Gene>()

            genes2.forEach {
                i2[it.innovation] = it
            }

            var sum = 0.0
            var coincident = 0.0

            genes1.forEach { gene ->
                if (i2[gene.innovation] != null) {
                    val gene2 = i2[gene.innovation]
                    sum += abs(gene.weight - gene2!!.weight)
                    ++coincident
                }
            }

            return if (coincident == 0.0) 0.0 else sum / coincident
        }

        private fun sameSpecies(genome1: Genome, genome2: Genome): Boolean {
            val dd = DELTA_DISJOINT * disjoint(genome1.genes, genome2.genes)
            val dw = DELTA_WEIGHTS * weights(genome1.genes, genome2.genes)
            return dd + dw < DELTA_THRESHOLD
        }

        private fun rankGlobally() {
            val global = mutableListOf<Genome>()

            pool.species.forEach { species ->
                species.genomes.forEach {
                    global.add(it)
                }
            }

            global.sortBy { it.fitness }

            for ((g, _) in global.withIndex()) {
                global[g].globalRank = g
            }
        }

        private fun calculateAverageFitness(species: Species) {
            var total = 0

            species.genomes.forEach {
                total += it.globalRank
            }

            species.averageFitness = if (species.genomes.size == 0) 0 else total / species.genomes.size
        }

        private fun totalAverageFitness(): Int {
            var total = 0

            pool.species.forEach { species ->
                total += species.averageFitness
            }

            return total
        }

        private fun cullSpecies(cutToOne: Boolean) {
            pool.species.forEach { species ->
                species.genomes.sortByDescending { it.fitness }

                var remaining = ceil((species.genomes.size / 2).toDouble())
                if (cutToOne) remaining = 1.0

                while (species.genomes.size > remaining) {
                    species.genomes.removeAt(species.genomes.lastIndex)
                }
            }
        }

        private fun breedChild(species: Species): Genome {
            val child = if (Random.nextDouble(0.0, 1.0) < CROSSOVER_CHANCE) {
                val g1 = species.genomes.random()
                val g2 = species.genomes.random()
                crossover(g1, g2)
            } else {
                val g = species.genomes.random()
                copyGenome(g)
            }

            mutate(child)
            return child
        }

        private fun removeStaleSpecies() {
            val survived = mutableListOf<Species>()

            pool.species.forEach { species ->
                species.genomes.sortByDescending { it.fitness }

                if (species.genomes.getOrNull(0)?.fitness ?: 0.0 > species.topFitness) {
                    species.topFitness = species.genomes[0].fitness
                    species.staleness = 0
                } else {
                    ++species.staleness
                }

                if (species.staleness < STALE_SPECIES || species.topFitness >= pool.maxFitness) {
                    survived.add(species)
                }
            }

            pool.species = survived
        }

        private fun removeWeakSpecies() {
            val survived = mutableListOf<Species>()

            val sum = totalAverageFitness()

            pool.species.forEach { species ->
                val breed = floor((species.averageFitness / sum * POPULATION).toDouble())
                if (breed > 1) survived.add(species)
            }

            pool.species = survived
        }

        private fun addToSpecies(child: Genome) {
            var foundSpecies = false
            pool.species.forEach { species ->
                if (foundSpecies.not() && sameSpecies(child, species.genomes.first())) {
                    species.genomes.add(child)
                    foundSpecies = true
                }
            }

            if (foundSpecies.not()) {
                val childSpecies = newSpecies()
                childSpecies.genomes.add(child)
                pool.species.add(childSpecies)
            }
        }

        //TODO: write to file
        private fun newGeneration() {
            cullSpecies(false)
            rankGlobally()
            removeStaleSpecies()
            rankGlobally()
            pool.species.forEach { species ->
                calculateAverageFitness(species)
            }
            removeWeakSpecies()
            val sum = totalAverageFitness()
            val children = mutableListOf<Genome>()

            pool.species.forEach { species ->
                val breed = floor((species.averageFitness / sum * POPULATION).toDouble()).toInt() - 1
                for (i in 0..breed) {
                    children.add(breedChild(species))
                }
            }
            cullSpecies(true)

            while (children.size + pool.species.size < POPULATION) {
                if (pool.species.isNotEmpty()) {
                    val species = pool.species.random()
                    children.add(breedChild(species))
                }
            }

            children.forEach {
                addToSpecies(it)
            }

            pool.generation = pool.generation + 1

            writeFile()
        }

        private fun initPool() {
            pool = newPool()

            //loadFile()

            for (x in 0..POPULATION) {
                addToSpecies(basicGenome())
            }

            initRun()
        }

        private fun initRun() {
            val species = pool.species[pool.currentSpecies]
            val genome = species.genomes[pool.currentGenome]
            generateNetwork(genome)
            evaluateCurrent()
        }

        private fun evaluateCurrent(): MutableList<PossibleMove> {
            val species = pool.species[pool.currentSpecies]
            val genome = species.genomes[pool.currentGenome]

            val inputs = gameHandler.gameBoard.nodeHashSet.map { it.hashCode().toDouble() }.toMutableList()
            inputs.add(aiPlayer.playerColor.hashCode().toDouble())

            return evaluateNetwork(genome.network, inputs)
        }

        private fun nextGenome() {
            pool.currentGenome = pool.currentGenome + 1

            if (pool.currentGenome > pool.species.getOrNull(pool.currentSpecies)?.genomes?.size ?: 0) {
                pool.currentGenome = 0
                pool.currentSpecies = pool.currentSpecies + 1

                if (pool.currentSpecies > pool.species.size) {
                    newGeneration()
                    pool.currentSpecies = 0
                }
            }
        }

        private fun fitnessAlreadyMeasured(): Boolean {
            val species = pool.species.getOrNull(pool.currentSpecies)
            val genome = species?.genomes?.getOrNull(pool.currentGenome)

            return genome?.fitness != 0.0
        }

        fun nextMove(): PartialMove {
            val current = evaluateCurrent().random()
            val partialMove = PartialMove(current.oldNode, current.newNode, aiPlayer)

            gameHandler.makePartialMove(partialMove)

            if (gameHandler.getWinner(gameHandler.ballNode)?.winner != null) {
                cutOff()
            }

            var measured = 0
            var total = 0

            pool.species.forEach { species ->
                species.genomes.forEach { genome ->
                    ++total
                    if (genome.fitness != 0.0) {
                        ++measured
                    }
                }
            }

            gameHandler.undoLastMove()

            return PartialMove(current.oldNode, current.newNode, aiPlayer)
        }

        fun cutOff() {
            val species = pool.species.getOrNull(pool.currentSpecies)
            val genome = species?.genomes?.getOrNull(pool.currentGenome)

            var fitness = fitnessEvaluation(gameHandler)

            if (fitness == 0.0) fitness = -1.0

            genome?.fitness = fitness

            if (fitness > pool.maxFitness) {
                println("Max fitness: ${pool.maxFitness} Gen ${pool.generation} species ${pool.species.sumBy { it.averageFitness }} genome: ${pool.currentGenome}")
                pool.maxFitness = fitness
                writeFile()
            }

            pool.currentSpecies = 0
            pool.currentGenome = 0

            while (fitnessAlreadyMeasured()) {
                nextGenome()
            }

            initRun()
        }

        private fun fitnessEvaluation(state: GameHandler): Double {
            var score = 0.0

            if (state.getWinner(state.ballNode)?.winner == aiPlayer) score = 1000.0

            score += state.numberOfTurns

            val opponentsGoal = state.getOpponent(aiPlayer).goal!!
            val closestToOpponentsGoal = state.gameBoard.nodeHashSet.filter { it.nodeType == NodeTypeEnum.BounceAble }.minBy { MathHelper.euclideanDistance(opponentsGoal.goalNode().coords, it.coords) }!!

            score -= MathHelper.euclideanDistance(opponentsGoal.goalNode().coords, closestToOpponentsGoal.coords)

            return score
        }

        private fun writeFile() {
/*            val file = File(context.cacheDir, FILE_NAME)
            file.createNewFile()
            file.writeText(Gson().toJson(pool))*/
        }

        private fun loadFile() {
            val file = File(context.cacheDir, FILE_NAME)

            if (file.exists()) {
                pool = Gson().fromJson(file.readText(), Pool::class.java)

                while (fitnessAlreadyMeasured()) {
                    nextGenome()
                }
                initRun()
            }
        }
    }
}
