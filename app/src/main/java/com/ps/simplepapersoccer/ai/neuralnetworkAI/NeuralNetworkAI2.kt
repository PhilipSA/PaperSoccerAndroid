package com.ps.simplepapersoccer.ai.neuralnetworkAI

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.random.Random

class NeuralNetworkAI2<T>(private val neuralNetworkController: INeuralNetworkController<T>,
                          private val neuralNetworkCache: NeuralNetworkCache,
                          private val neuralNetworkParameters: NeuralNetworkParameters = NeuralNetworkParameters()) {
    data class Neuron(
            val incoming: HashMap<Int, Gene>,
            var value: Float
    ) : Serializable

    data class Genome(
            val genes: HashMap<Int, Gene>,
            var fitness: Float,
            val adjustedFitness: Int,
            var network: Network,
            var maxNeuron: Int,
            var globalRank: Int,
            val mutationRates: HashMap<String, Float>
    ) : Serializable

    data class Species(
            var topFitness: Float,
            var staleness: Int,
            val genomes: HashMap<Int, Genome>,
            var averageFitness: Float
    ) : Serializable

    data class Pool(
            var species: HashMap<Int, Species>,
            var generation: Int,
            var currentSpecies: Int,
            var currentGenome: Int,
            var maxFitness: Float,
            var innovation: Int
    ) : Serializable

    data class Gene(
            var into: Int,
            var out: Int,
            var weight: Float,
            var enabled: Boolean,
            var innovation: Int
    ) : Serializable

    data class Network(
            val neurons: HashMap<Int, Neuron>
    ) : Serializable

    private lateinit var pool: Pool

    private val inputSize get() = neuralNetworkController.inputs.size + 1

    init {
        initPool()
    }

    private fun sigmoid(x: Float): Float {
        return 2 / (1 + exp(-4.9f * x)) - 1
    }

    private fun newInnovation(): Int {
        pool.innovation = pool.innovation + 1
        return pool.innovation
    }

    private fun newPool(): Pool {
        return Pool(hashMapOf(), 0, 1, 1, 0f, neuralNetworkController.outputs)
    }

    private fun newSpecies(): Species {
        return Species(0f, 0, hashMapOf(), 0f)
    }

    private fun newGenome(): Genome {
        return Genome(hashMapOf(), 0f, 0, Network(hashMapOf()), 0, 0,
                hashMapOf("connections" to neuralNetworkParameters.MUTATE_CONNECTION_CHANCE,
                        "link" to neuralNetworkParameters.LINK_MUTATION_CHANCE,
                        "bias" to neuralNetworkParameters.BIAS_MUTATION_CHANCE,
                        "node" to neuralNetworkParameters.NODE_MUTATION_CHANCE,
                        "enable" to neuralNetworkParameters.ENABLE_MUTATION_CHANCE,
                        "disable" to neuralNetworkParameters.DISABLE_MUTATION_CHANCE,
                        "step" to neuralNetworkParameters.STEP_SIZE))
    }

    private fun copyGenome(genome: Genome): Genome {
        val newGenes = genome.genes.map {
            copyGene(it)
        }

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
        genome.maxNeuron = inputSize
        mutate(genome)
        return genome
    }

    private fun newGene(): Gene {
        return Gene(0, 0, 0f, true, 0)
    }

    private fun copyGene(gene: Gene): Gene {
        return gene.copy()
    }

    private fun newNeuron(): Neuron {
        return Neuron(hashMapOf(), 0f)
    }

    private fun generateNetwork(genome: Genome) {
        val network = Network(hashMapOf())

        for (i in 1 until inputSize) {
            network.neurons[i + 1] = newNeuron()
        }

        for (index in 1 until neuralNetworkController.outputs) {
            network.neurons[neuralNetworkParameters.MAX_NODES + index] = newNeuron()
        }

        //TODO FIX THIS SHIT
        //genome.genes.toSortedMap(compareByDescending { it. } )

        for (i in 1 until genome.genes.size) {
            val gene = genome.genes[i]!!
            if (gene.enabled) {
                if (network.neurons[gene.out] == null) {
                    network.neurons[gene.out] = newNeuron()
                }

                val neuron = network.neurons[gene.out]!!
                neuron.incoming[neuron.incoming.size + 1] = gene

                if (network.neurons[gene.into] == null) {
                    network.neurons[gene.into] = newNeuron()
                }
            }
        }

        genome.network = network
    }

    //TODO DOUBLE CHECK IT WAS COPY PASTED FROM NEURAL NETWORK
    private fun evaluateNetwork(network: Network, inputsArg: List<Float>): T? {
        val inputs = inputsArg.toMutableList().apply {
            add(1f)
        }

        if (inputs.size != inputSize) {
            throw(Exception("No"))
        }

        for (index in neuralNetworkController.inputs.indices) {
            network.neurons[index]?.value = inputs[index]
        }

        network.neurons.values.forEach { neuron ->
            var sum = 0f

            neuron.incoming.forEach { incoming ->
                val other = network.neurons[incoming]

                sum += incoming.weight * other!!.value
            }

            if (neuron.incoming.size > 0) {
                neuron.value = sigmoid(sum)
            }
        }

        val allOutputNeurons = mutableListOf<Float>()

        for (index in 0 until neuralNetworkController.outputs) {
            val neuronValue = network.neurons[neuralNetworkParameters.MAX_NODES + index]!!.value
            allOutputNeurons.add(neuronValue)
        }

        return neuralNetworkController.networkGuessOutput(allOutputNeurons)
    }

    fun crossOver(g1: Genome, g2: Genome): Genome {
        var g1 = g1
        var g2 = g2

        if (g1.fitness > g2.fitness) {
            val tempg = g1
            g1 = g2
            g2 = tempg
        }

        val child = newGenome()

        val innovations2 = HashMap<Int, Gene>()
        for (i in 1 until g2.genes.size) {
            val gene = g2.genes[i]!!
            innovations2[gene.innovation] = gene
        }

        for (i in 1 until g1.genes.size) {
            val gene1 = g1.genes[i]!!
            val gene2 = innovations2[gene1.innovation]

            if (gene2 != null && Random.nextBoolean() && gene2.enabled) {
                child.genes[child.genes.size + 1] = copyGene(gene2)
            } else {
                child.genes[child.genes.size + 1] = copyGene(gene1)
            }
        }

        child.maxNeuron = kotlin.math.max(g1.maxNeuron, g2.maxNeuron)

        for ((mutation, rate) in g1.mutationRates) {
            child.mutationRates[mutation] = rate
        }

        return child
    }

    fun randomNeuron(genes: HashMap<Int, Gene>, nonInput: Boolean): Int {
        val neurons = HashMap<Int, Boolean>()

        if (nonInput.not()) {
            for (i in 1 until inputSize) {
                neurons[i] = true
            }
        }

        for (i in 1 until neuralNetworkController.outputs) {
            neurons[neuralNetworkParameters.MAX_NODES + i] = true
        }

        for (i in 1 until genes.values.size) {
            if (nonInput.not() || genes[i]!!.into > inputSize) {
                neurons[genes[i]!!.into] = true
            }
            if (nonInput.not() || genes[i]!!.out > inputSize) {
                neurons[genes[i]!!.out] = true
            }
        }

        var count = 0

        for ((_, _) in neurons) {
            ++count
        }

        var n = Random.nextInt(1, count)

        for ((k, v) in neurons) {
            n -= 1
            if (n == 0) {
                return k
            }
        }

        return 0
    }

    fun containsLink(genes: HashMap<Int, Gene>, link: Gene): Boolean {
        for (i in 1 until genes.size) {
            val gene = genes[i]!!
            if (gene.into == link.into && gene.out == link.out) {
                return true
            }
        }

        return false
    }

    fun pointMutate(genome: Genome) {
        val step = genome.mutationRates["step"]!!

        for (i in 1 until genome.genes.size) {
            val gene = genome.genes[i]!!
            if (Random.nextFloat() < neuralNetworkParameters.PERTURB_CHANCE) {
                gene.weight = gene.weight + Random.nextFloat() * step * 2 - step
            } else {
                gene.weight = Random.nextFloat() * 4 - 2
            }
        }
    }

    fun linkMutate(genome: Genome, forceBias: Boolean) {
        var neuron1 = randomNeuron(genome.genes, false)
        var neuron2 = randomNeuron(genome.genes, true)

        val newLink = newGene()
        if (neuron1 <= inputSize && neuron2 <= inputSize) {
            return
        }

        if (neuron2 <= inputSize) {
            val temp = neuron1
            neuron1 = neuron2
            neuron2 = temp
        }

        newLink.into = neuron1
        newLink.out = neuron2
        if (forceBias) {
            newLink.into = inputSize
        }

        if (containsLink(genome.genes, newLink)) return

        newLink.innovation = newInnovation()
        newLink.weight = Random.nextFloat() * 4 - 2

        genome.genes[genome.genes.size + 1] = newLink
    }

    fun nodeMutate(genome: Genome) {
        if (genome.genes.size == 0) {
            return
        }

        genome.maxNeuron = genome.maxNeuron - 1

        val gene = genome.genes[Random.nextInt(1, genome.genes.size)]!!
        if (gene.enabled.not()) return

        gene.enabled = false

        val gene1 = copyGene(gene)
        gene1.out = genome.maxNeuron
        gene1.weight = 1f
        gene1.innovation = newInnovation()
        gene1.enabled = true
        genome.genes[genome.genes.size + 1] = gene1

        val gene2 = copyGene(gene)
        gene2.into = genome.maxNeuron
        gene2.innovation = newInnovation()
        gene2.enabled = true
        genome.genes[genome.genes.size + 1] = gene2
    }

    fun enableDisableMutate(genome: Genome, enabled: Boolean) {
        val candidates = hashMapOf<Int, Gene>()

        for ((_, gene) in genome.genes) {
            if (gene.enabled == enabled.not()) {
                candidates[candidates.size + 1] = gene
            }
        }

        if (candidates.size == 0) return

        val gene = candidates[Random.nextInt(1, candidates.size)]!!
        gene.enabled = gene.enabled.not()
    }

    private fun mutate(genome: Genome) {
        for ((mutation, rate) in genome.mutationRates) {
            if (Random.nextBoolean()) {
                genome.mutationRates[mutation] = 0.95f * rate
            } else {
                genome.mutationRates[mutation] = 1.05263f * rate
            }
        }

        if (Random.nextFloat() < genome.mutationRates["connections"]!!) {
            pointMutate(genome)
        }

        var p = genome.mutationRates.getValue("link")
        while (p > 0) {
            if (Random.nextFloat() < p) {
                linkMutate(genome, false)
            }
            --p
        }

        p = genome.mutationRates.getValue("bias")
        while (p > 0) {
            if (Random.nextFloat() < p) {
                linkMutate(genome, true)
            }
            --p
        }

        p = genome.mutationRates.getValue("node")
        while (p > 0) {
            if (Random.nextFloat() < p) {
                nodeMutate(genome)
            }
            --p
        }

        p = genome.mutationRates.getValue("enable")
        while (p > 0) {
            if (Random.nextFloat() < p) {
                enableDisableMutate(genome, true)
            }
            --p
        }

        p = genome.mutationRates.getValue("disable")
        while (p > 0) {
            if (Random.nextFloat() < p) {
                enableDisableMutate(genome, false)
            }
            --p
        }
    }

    fun disjoint(genes1: HashMap<Int, Gene>, genes2: HashMap<Int, Gene>): Int {
        val i1 = hashMapOf<Int, Boolean>()
        for (i in 1 until genes1.size) {
            val gene = genes1[i]!!
            i1[gene.innovation] = true
        }

        val i2 = hashMapOf<Int, Boolean>()
        for (i in 1 until genes2.size) {
            val gene = genes2[i]!!
            i2[gene.innovation] = true
        }

        var disjointGenes = 0

        for (i in 1 until genes1.size) {
            val gene = genes1[i]!!
            if (i2[gene.innovation]!!.not()) {
                disjointGenes += 1
            }
        }

        for (i in 1 until genes2.size) {
            val gene = genes2[i]!!
            if (i1[gene.innovation]!!.not()) {
                disjointGenes += 1
            }
        }

        val n = Math.max(genes1.size, genes2.size)

        return disjointGenes / n
    }

    fun weights(genes1: HashMap<Int, Gene>, genes2: HashMap<Int, Gene>): Float {
        val i2 = hashMapOf<Int, Gene>()

        for (i in 1 until genes2.size) {
            val gene = genes2[i]!!
            i2[gene.innovation] = gene
        }

        var sum = 0f
        var coincident = 0

        for (i in 1 until genes1.size) {
            val gene = genes1[i]!!
            if (i2[gene.innovation] != null) {
                val gene2 = i2[gene.innovation]!!
                sum += abs(gene.weight - gene2.weight)
                coincident += 1
            }
        }

        return sum / coincident
    }

    fun sameSpecies(genome1: Genome, genome2: Genome): Boolean {
        val dd = neuralNetworkParameters.DELTA_DISJOINT * disjoint(genome1.genes, genome2.genes)
        val dw = neuralNetworkParameters.DELTA_WEIGHTS * weights(genome1.genes, genome2.genes)
        return dd + dw < neuralNetworkParameters.DELTA_THRESHOLD
    }

    fun rankGlobally() {
        val global = hashMapOf<Int, Genome>()

        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            for (g in 1 until species.genomes.size) {
                global[global.size + 1] = species.genomes[g]!!
            }
        }

        table.sort(global, function(a, b)
                return (a.fitness < b.fitness)
                        end)

        for (g in 1 until global.size) {
            global[g]!!.globalRank = g
        }
    }

    fun calculateAverageFitness(species: Species) {
        var total = 0f

        for (g in 1 until species.genomes.size) {
            val genome = species.genomes[g]!!
            total += genome.globalRank
        }

        species.averageFitness = total / species.genomes.size
    }

    fun totalAverageFitness(): Float {
        var total = 0f

        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            total += species.averageFitness
        }

        return total
    }

    fun cullSpecies(cutToOne: Boolean) {
        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!

            table.sort(species.genomes, function(a, b)
                    return (a.fitness > b.fitness)
                            end)

            val remaining = Math.ceil(species.genomes.size / 2)

            if (cutToOne) remaining = 1

            while (species.genomes.size > remaining) {
                species.genomes.toMutableMap().remove(species.genomes)
                table.remove(species.genomes)
            }
        }
    }

    fun breedChild(species: Species): Genome {
        val child: Genome = if (Random.nextFloat() < neuralNetworkParameters.CROSSOVER_CHANCE) {
            val g1 = species.genomes[Random.nextInt(1, species.genomes.size)]!!
            val g2 = species.genomes[Random.nextInt(1, species.genomes.size)]!!
            crossOver(g1, g2)
        } else {
            val g = species.genomes[Random.nextInt(1, species.genomes.size)]!!
            copyGenome(g)
        }

        mutate(child)

        return child
    }

    fun removeStaleSpecies() {
        val survived = hashMapOf<Int, Species>()

        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!

            table.sort(species.genomes, function(a, b)
                    return (a.fitness > b.fitness)
                            end)

            if (species.genomes[1]!!.fitness > species.topFitness) {
                species.topFitness = species.genomes[1].fitness
                species.staleness = 0
            } else {
                species.staleness = species.staleness + 1
            }
            if (species.staleness < neuralNetworkParameters.STALE_SPECIES || species.topFitness >= pool.maxFitness) {
                survived[survived.size + 1] = species
            }
        }

        pool.species = survived
    }

    fun removeWeakSpecies() {
        val survived = hashMapOf<Int, Species>()

        val sum = totalAverageFitness()
        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            val breed = floor(species.averageFitness / sum * neuralNetworkParameters.POPULATION)
            if (breed >= 1) survived[survived.size + 1] = species
        }

        pool.species = survived
    }

    fun addToSpecies(child: Genome) {
        var foundSpecies = false

        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            if (foundSpecies.not() && sameSpecies(child, species.genomes[1]!!)) {
                species.genomes[species.genomes.size + 1] = child
                foundSpecies = true
            }
        }

        if (foundSpecies.not()) {
            val childSpecies = newSpecies()
            childSpecies.genomes[childSpecies.genomes.size + 1] = child
            pool.species[pool.species.size + 1] = childSpecies
        }
    }

    fun newGeneration() {
        cullSpecies(false)
        rankGlobally()
        removeStaleSpecies()
        rankGlobally()
        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            calculateAverageFitness(species)
        }
        removeWeakSpecies()
        val sum = totalAverageFitness()
        val children = hashMapOf<Int, Genome>()
        for (s in 1 until pool.species.size) {
            val species = pool.species[s]!!
            val breed = floor(species.averageFitness / sum * neuralNetworkParameters.POPULATION) - 1
            for (i in 1 until breed) {
                children[children.size + 1] = breedChild(species)
            }
        }
        cullSpecies(true)
        while (children.size + pool.species.size < neuralNetworkParameters.POPULATION) {
            val species = pool.species[Random.nextInt(1, pool.species.size)]!!
            children[children.size + 1] = breedChild(species)
        }
        for (c in 1 until children.size) {
            val child = children[c]!!
            addToSpecies(child)
        }

        pool.generation = pool.generation + 1
    }

    fun initializePool() {
        pool = newPool()

        for (i in 1 until neuralNetworkParameters.POPULATION) {
            val basic = basicGenome()
            addToSpecies(basic)
        }

        initializeRun()
    }

    fun initializeRun() {
        val species = pool.species[pool.currentSpecies]!!
        val genome = species.genomes[pool.currentGenome]!!
        generateNetwork(genome)
        evaluateCurrent()
    }

    fun nextGenome() {
        pool.currentGenome = pool.currentGenome + 1
        if (pool.currentGenome > pool.species[pool.currentSpecies]!!.genomes.size) {
            pool.currentGenome = 1
            pool.currentSpecies = pool.currentSpecies + 1
            if (pool.currentSpecies > pool.species.size) {
                newGeneration()
                pool.currentSpecies = 1
            }
        }
    }

    fun fitnessAlreadyMeasured(): Boolean {
        val species = pool.species[pool.currentSpecies]!!
        val genome = species.genomes[pool.currentGenome]!!

        return genome.fitness != 0f
    }

    private fun evaluateCurrent(): T? {
        return evaluateNetwork(pool.currentGenome.network, neuralNetworkController.inputs)
    }

    fun cutOff() {
        val fitness = neuralNetworkController.fitnessEvaluation()

        val species = pool.species[pool.currentSpecies]!!
        val genome = species.genomes[pool.currentGenome]!!

        genome.fitness = fitness

        if (fitness > pool.maxFitness) {
            pool.maxFitness = fitness
            println("Max fitness: ${pool.maxFitness} Gen ${pool.generation} species ${pool.species.sumBy { it.averageFitness.toInt() }} genome: ${pool.currentGenomeIndex}")
            neuralNetworkCache.savePool(pool)
        }

        pool.currentSpecies = 1
        pool.currentGenome = 1

        while (fitnessAlreadyMeasured()) {
            nextGenome()
        }

        initRun()
    }

    private fun playTop() {
        var maxfitness = 0f
        var maxs = 0
        var maxg = 0

        pool.species.forEach { species ->
            species.genomes.forEach { genome ->
                if (genome.fitness > maxfitness) {
                    maxfitness = genome.fitness
                    maxs = pool.species.indexOf(species)
                    maxg = pool.currentSpecies.genomes.indexOf(genome)
                }
            }
        }

        pool.currentSpecies = maxs
        pool.currentGenome = maxg
        pool.maxFitness = maxfitness
        initializeRun()
    }

    fun nextStep(): T? {
        val current = evaluateCurrent()

        var measured = 0
        var total = 0

        pool.species.forEach { species ->
            species.genomes.forEach { genome ->
                ++total
                if (genome.fitness != 0f) {
                    ++measured
                }
            }
        }

        return current
    }
}