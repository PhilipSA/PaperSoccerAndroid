package com.ps.simplepapersoccer.ai.neuralnetworkAI

import java.io.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.*
import kotlin.random.Random

class NeuralNetwork<T>(private val neuralNetworkController: INeuralNetworkController<T>,
                       private val neuralNetworkCache: NeuralNetworkCache,
                       private val neuralNetworkParameters: NeuralNetworkParameters = NeuralNetworkParameters()) {

    private lateinit var pool: Pool

    private val inputSize get() = neuralNetworkController.inputs.size + 1

    init {
        initPool()
    }

    private fun sigmoid(x: Float): Float {
        return 2 / (1 + exp(-4.9f * x)) - 1
    }

    private fun newInnovation(): Int {
        pool.innovation += 1
        return pool.innovation
    }

    private fun copyGenome(genome: Genome): Genome {
        val newGenes = genome.genes.map {
            it.copy()
        }.toMutableList()

        return Genome(genes = newGenes,
                fitness = genome.fitness,
                adjustedFitness = genome.adjustedFitness,
                network = genome.network,
                maxNeuron = genome.maxNeuron,
                globalRank = genome.globalRank,
                mutationRates = genome.mutationRates)
    }

    private fun basicGenome(network: Network): Genome {
        val genome = Genome(network, neuralNetworkParameters)
        genome.maxNeuron = NeuronIndex(network.inputNeurons.size - 1, NeuronType.Input)
        mutate(genome)
        return genome
    }

    private fun newNetwork(): Network {
        val network = Network(mutableListOf(), mutableListOf(), mutableListOf())

        for (i in 0 until inputSize) {
            network.inputNeurons.add(Neuron.getDefault(NeuronType.Input))
        }

        for (index in 0 until neuralNetworkController.outputs) {
            network.outputNeurons.add(Neuron.getDefault(NeuronType.Output))
        }

        return network
    }

    private fun generateNetwork(genome: Genome) {
        genome.genes.sortBy { it.outNeuronIndex?.index }

        genome.genes.forEach { gene ->
            if (gene.enabled) {
                if (genome.getNeuronByIndex(gene.outNeuronIndex) == null) {
                    val newNeuron = Neuron.getDefault(NeuronType.HiddenLayer)
                    genome.network.hiddenLayerNeurons.add(newNeuron)
                    gene.outNeuronIndex = NeuronIndex(genome.network.hiddenLayerNeurons.size - 1, newNeuron.neuronType)
                }
                val neuron = gene.outNeuronIndex
                genome.getNeuronByIndex(neuron)!!.incoming.add(gene)
                if (genome.getNeuronByIndex(gene.intoNeuronIndex) == null) {
                    val newNeuron = Neuron.getDefault(NeuronType.HiddenLayer)
                    genome.network.hiddenLayerNeurons.add(newNeuron)
                    gene.intoNeuronIndex = NeuronIndex(genome.network.hiddenLayerNeurons.size - 1, newNeuron.neuronType)
                }
            }
        }
    }

    private fun evaluateNetwork(network: Network, inputsArg: List<Float>): T? {
        val inputs = inputsArg.toMutableList().apply {
            add(1f)
        }

        if (inputs.size != inputSize) {
            throw(Exception("No"))
        }

        for (index in inputs.indices) {
            network.inputNeurons[index].value = inputs[index]
        }

        network.allNeurons.forEach { neuron ->
            var sum = 0f

            neuron.incoming.forEach { incoming ->
                val other = network.getNeuronByIndex(incoming.intoNeuronIndex)

                sum += incoming.weight * other!!.value
            }

            if (neuron.incoming.size > 0) {
                neuron.value = sigmoid(sum)
            }
        }

        val allOutputNeurons = mutableListOf<Float>()

        network.outputNeurons.forEach {
            val neuronValue = it.value
            allOutputNeurons.add(neuronValue)
        }

        return neuralNetworkController.networkGuessOutput(allOutputNeurons)
    }

    private fun crossover(genome1: Genome, genome2: Genome): Genome {
        var genome1 = genome1
        var genome2 = genome2

        if (genome2.fitness > genome1.fitness) {
            val tempg = genome1
            genome1 = genome2
            genome2 = tempg
        }

        val child = Genome(newNetwork(), neuralNetworkParameters)

        val innovations2 = HashMap<Int, Gene>()

        genome2.genes.forEach { gene ->
            innovations2[gene.innovation] = gene
        }

        genome1.genes.forEach { gene1 ->
            val gene2 = innovations2[gene1.innovation]

            if (gene2 != null && Random.nextBoolean() && gene2.enabled) {
                child.genes.add(gene2.copy())
            } else {
                child.genes.add(gene1.copy())
            }
        }

        child.maxNeuron = if (genome1.maxNeuron?.index ?: 0 > genome2.maxNeuron?.index ?: 0) genome1.maxNeuron else genome2.maxNeuron

        for ((mutation, rate) in genome1.mutationRates) {
            child.mutationRates[mutation] = rate
        }

        return child
    }

    private fun randomNeuron(genome: Genome, genes: MutableList<Gene>, nonInput: Boolean): NeuronIndex? {
        val neurons = mutableListOf<NeuronIndex>()

        if (nonInput.not()) {
            neurons.addAll(genome.network.inputNeurons.mapIndexed { index, neuron -> NeuronIndex(index, neuron.neuronType) })
        }

        neurons.addAll(genome.network.outputNeurons.mapIndexed { index, neuron -> NeuronIndex(index, neuron.neuronType) })

        genes.forEach { gene ->
            val intoNeuron = genome.getNeuronByIndex(gene.intoNeuronIndex)
            val outNeuron = genome.getNeuronByIndex(gene.outNeuronIndex)

            if (nonInput.not() || intoNeuron?.isInputNeuron?.not() == true) {
                gene.intoNeuronIndex?.let { neurons.add(it) }
            }

            if (nonInput.not() || outNeuron?.isInputNeuron?.not() == true) {
                gene.outNeuronIndex?.let { neurons.add(it) }
            }
        }

        return if (neurons.isEmpty()) null else neurons.random()
    }

    private fun containsLink(genes: MutableList<Gene>, link: Gene): Boolean {
        genes.forEach { gene ->
            if (gene.intoNeuronIndex == link.intoNeuronIndex && gene.outNeuronIndex == link.outNeuronIndex) {
                return true
            }
        }
        return false
    }

    private fun pointMutate(genome: Genome) {
        val step = genome.mutationRates.getValue("step")

        genome.genes.forEach { gene ->
            if (Random.nextFloat() < neuralNetworkParameters.PERTURB_CHANCE) {
                gene.weight = gene.weight + Random.nextFloat() * step * 2 - step
            } else {
                gene.weight = Random.nextFloat() * 4 - 2
            }
        }
    }

    private fun linkMutate(genome: Genome, forceBias: Boolean) {
        var neuronIndex1 = randomNeuron(genome, genome.genes, false)
        var neuronIndex2 = randomNeuron(genome, genome.genes, true)

        val neuron1 = genome.getNeuronByIndex(neuronIndex1)
        val neuron2 = genome.getNeuronByIndex(neuronIndex2)

        if (neuron1 == null || neuron2 == null) return

        val newLink = Gene.getDefault()

        if (neuron1.isInputNeuron && neuron2.isInputNeuron) {
            return
        }

        if (neuron2.isInputNeuron) {
            val temp = neuronIndex1
            neuronIndex1 = neuronIndex2
            neuronIndex2 = temp
        }

        newLink.intoNeuronIndex = neuronIndex1
        newLink.outNeuronIndex = neuronIndex2

        if (forceBias) newLink.intoNeuronIndex = NeuronIndex(genome.network.inputNeurons.size - 1, NeuronType.Input)

        if (containsLink(genome.genes, newLink)) return

        newLink.innovation = newInnovation()
        newLink.weight = Random.nextFloat() * 4 - 2

        genome.genes.add(newLink)
    }

    private fun nodeMutate(genome: Genome) {
        if (genome.genes.size == 0) return

        genome.nextMaxNeuron()

        val gene = genome.genes.random()

        if (gene.enabled.not()) return
        gene.enabled = false

        val gene1 = gene.copy()
        gene1.outNeuronIndex = genome.maxNeuron
        gene1.weight = 1f
        gene1.innovation = newInnovation()
        gene1.enabled = true
        genome.genes.add(gene1)

        val gene2 = gene.copy()
        gene2.intoNeuronIndex = genome.maxNeuron
        gene2.innovation = newInnovation()
        gene2.enabled = true
        genome.genes.add(gene2)
    }

    fun enableDisableMutate(genome: Genome, enable: Boolean) {
        val candidates = genome.genes.filter { it.enabled == enable.not() }

        if (candidates.isEmpty()) return

        val gene = candidates.random()
        gene.enabled = gene.enabled.not()
    }

    private fun mutateHelper(rate: Float): Float {
        return if (Random.nextBoolean()) {
            0.95f * rate
        } else {
            1.05263f * rate
        }
    }

    private fun mutate(genome: Genome) {
        for ((mutation, rate) in genome.mutationRates) {
            val value = mutateHelper(rate)
            genome.mutationRates[mutation] = value
        }

        if (Random.nextFloat() < genome.mutationRates.getValue("connections")) {
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

    private fun disjoint(genes1: List<Gene>, genes2: List<Gene>): Int {
        val i1 = HashMap<Int, Boolean>()
        val i2 = HashMap<Int, Boolean>()

        var disjointGenes = 0

        genes1.forEach {
            i1[it.innovation] = true

            if (i2[it.innovation]?.not() == true) {
                ++disjointGenes
            }
        }

        genes2.forEach {
            i2[it.innovation] = true

            if (i1[it.innovation]?.not() == true) {
                ++disjointGenes
            }
        }

        val n = max(genes1.size, genes2.size)

        return if (n == 0) Int.MAX_VALUE else disjointGenes / n
    }

    private fun weights(genes1: List<Gene>, genes2: List<Gene>): Float {
        val i2 = HashMap<Int, Gene>()

        genes2.forEach {
            i2[it.innovation] = it
        }

        var sum = 0f
        var coincident = 0f

        genes1.forEach { gene ->
            if (i2[gene.innovation] != null) {
                val gene2 = i2[gene.innovation]
                sum += abs(gene.weight - gene2!!.weight)
                ++coincident
            }
        }

        return sum / coincident
    }

    private fun sameSpecies(genome1: Genome, genome2: Genome): Boolean {
        val dd = neuralNetworkParameters.DELTA_DISJOINT * disjoint(genome1.genes, genome2.genes)
        val dw = neuralNetworkParameters.DELTA_WEIGHTS * weights(genome1.genes, genome2.genes)
        return dd + dw < neuralNetworkParameters.DELTA_THRESHOLD
    }

    private fun rankGlobally() {
        val global = mutableListOf<Genome>()

        pool.species.forEach { species ->
            species.genomes.forEach {
                global.add(it)
            }
        }

        global.sortBy { it.fitness }

        for ((g, genome) in global.withIndex()) {
            genome.globalRank = g + 1
        }
    }

    fun calculateAverageFitness(species: Species) {
        var total = 0f

        species.genomes.forEach {
            total += it.globalRank
        }

        species.averageFitness = total / species.genomes.size
    }

    fun totalAverageFitness(): Float {
        var sum = 0f
        for (element in pool.species) {
            sum += element.averageFitness
        }
        return sum
    }

    private fun cullSpecies(cutToOne: Boolean) {
        pool.species.forEach { species ->
            species.genomes.sortByDescending { it.fitness }

            var remaining = ceil(species.genomes.size / 2f)
            if (cutToOne) remaining = 1f

            species.genomes.subList(remaining.toInt(), species.genomes.size).clear()
        }
    }

    private fun breedChild(species: Species): Genome {
        val child = if (Random.nextFloat() < neuralNetworkParameters.CROSSOVER_CHANCE) {
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

            if (species.genomes[0].fitness > species.topFitness) {
                species.topFitness = species.genomes[0].fitness
                species.staleness = 0
            } else {
                ++species.staleness
            }

            if (species.staleness < neuralNetworkParameters.STALE_SPECIES || species.topFitness >= pool.maxFitness) {
                survived.add(species)
            }
        }

        pool.species = survived
    }

    private fun removeWeakSpecies() {
        val survived = mutableListOf<Species>()

        val sum = totalAverageFitness()

        pool.species.forEach { species ->
            val breed = floor(species.averageFitness / sum * neuralNetworkParameters.POPULATION)
            if (breed >= 1) survived.add(species)
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
            val childSpecies = Species.getDefault()
            childSpecies.genomes.add(child)
            pool.species.add(childSpecies)
        }
    }

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
            val breed = floor((species.averageFitness / sum * neuralNetworkParameters.POPULATION)).toInt() - 1
            for (i in 0 until breed) {
                children.add(breedChild(species))
            }
        }
        cullSpecies(true)

        while (children.size + pool.species.size < neuralNetworkParameters.POPULATION) {
            val species = pool.species.random()
            children.add(breedChild(species))
        }

        children.forEach {
            addToSpecies(it)
        }

        pool.generation = pool.generation + 1

        neuralNetworkCache.savePool(pool)
    }

    private fun initPool() {
        val loadedPool = neuralNetworkCache.loadPool()

        if (loadedPool != null) {
            pool = loadedPool

            while (fitnessAlreadyMeasured()) {
                nextGenome(pool)
            }
            initRun()
        } else {
            pool = Pool.getDefault(innovation = neuralNetworkController.outputs)

            for (x in 0 until neuralNetworkParameters.POPULATION) {
                addToSpecies(basicGenome(newNetwork()))
            }

            initRun()
        }
    }

    private fun initRun() {
        generateNetwork(pool.currentGenome!!)
    }

    private fun evaluateCurrent(): T? {
        return evaluateNetwork(pool.currentGenome!!.network, neuralNetworkController.inputs)
    }

    fun nextGenome(pool: Pool) {
        pool.currentGenomeIndex +=  1
        if (pool.currentGenomeIndex >= pool.currentSpecies!!.genomes.size) {
            pool.currentGenomeIndex = 0
            pool.currentSpeciesIndex += 1

            if (pool.currentSpeciesIndex >= pool.species.size) {
                newGeneration()
                pool.currentSpeciesIndex = 0
            }
        }
    }

    private fun fitnessAlreadyMeasured(): Boolean {
        return pool.currentGenome!!.fitness != 0f
    }

    fun cutOff() {
        var fitness = neuralNetworkController.fitnessEvaluation()

        if (fitness == 0f) {
            fitness = -1f
        }

        pool.currentGenome!!.fitness = fitness

        if (fitness > pool.maxFitness) {
            pool.maxFitness = fitness
            println("Max fitness: ${pool.maxFitness} Gen ${pool.generation} species ${pool.species.sumBy { it.averageFitness.toInt() }}")
            neuralNetworkCache.savePool(pool)
        }

        pool.currentSpeciesIndex = 0
        pool.currentGenomeIndex = 0

        while (fitnessAlreadyMeasured()) {
            nextGenome(pool)
        }

        initRun()
    }

    private fun playTop() {
        var maxfitness = 0f
        var maxs = 0
        var maxg = 0

        pool.species.forEachIndexed { i, species ->
            species.genomes.forEachIndexed { j, genome ->
                if (genome.fitness > maxfitness) {
                    maxfitness = genome.fitness
                    maxs = i
                    maxg = j
                }
            }
        }

        pool.currentSpeciesIndex = maxs
        pool.currentGenomeIndex = maxg
        pool.maxFitness = maxfitness
        initRun()
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