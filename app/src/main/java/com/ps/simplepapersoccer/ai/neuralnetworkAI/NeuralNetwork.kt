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

    enum class NeuronType {
        Input,
        Output,
        HiddenLayer
    }

    data class Neuron(
            val id: String,
            val incoming: HashSet<Gene>,
            var value: Float,
            var neuronType: NeuronType
    ) : Serializable {
        val isInputNeuron get() = neuronType == NeuronType.Input
        val isOutputNeuron get() = neuronType == NeuronType.Output
    }

    class Genome(
            val genes: MutableList<Gene>,
            var fitness: Float,
            val adjustedFitness: Int,
            var network: Network,
            var maxNeuron: Neuron?,
            var globalRank: Int,
            val mutationRates: HashMap<String, Float>
    ) : Serializable {
        fun nextMaxNeuron() {
            maxNeuron = network.inputNeurons.getOrNull(network.inputNeurons.indexOf(maxNeuron) + 1)
        }

        fun getMaxNeuronIndex(): Int {
            return network.inputNeurons.indexOf(maxNeuron)
        }

        fun getNeuronIndex(neuron: Neuron): Int {
            return network.inputNeurons.indexOf(neuron)
        }

        fun getNeuronById(id: String?): Neuron? {
            return network.getNeuronById(id)
        }
    }

    data class Species(
            var topFitness: Float,
            var staleness: Int,
            val genomes: MutableList<Genome>,
            var averageFitness: Float
    ) : Serializable

    data class Pool(
            var species: MutableList<Species>,
            var generation: Int,
            var currentSpecies: Species?,
            var currentGenome: Genome?,
            var maxFitness: Float,
            var innovation: Int
    ) : Serializable {
        fun setNewCurrentSpecies(species: Species) {
            this.currentSpecies = species
            this.currentGenome = species.genomes.first()
        }
    }

    data class Gene(
            var intoNeuronId: String?,
            var outNeuronId: String?,
            var weight: Float,
            var enabled: Boolean,
            var innovation: Int
    ) : Serializable

    data class Network(
            val inputNeurons: MutableList<Neuron>,
            val hiddenLayerNeurons: MutableList<Neuron>,
            val outputNeurons: MutableList<Neuron>
    ) : Serializable {
        val allNeurons get() = inputNeurons.plus(outputNeurons).plus(hiddenLayerNeurons)

        fun getNeuronById(id: String?): Neuron? {
            return allNeurons.firstOrNull { it.id == id }
        }
    }

    data class PoolDto(
            val uncompressedSize: Int,
            val pool: ByteArray
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
        pool.innovation += 1
        return pool.innovation
    }

    private fun newPool(): Pool {
        return Pool(mutableListOf(), 0, null, null, 0f, neuralNetworkController.outputs)
    }

    private fun newSpecies(): Species {
        return Species(0f, 0, mutableListOf(), 0f)
    }

    private fun newGenome(network: Network): Genome {
        return Genome(mutableListOf(), 0f, 0, network, null, 0,
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
        val genome = newGenome(network)
        genome.maxNeuron = network.inputNeurons.last()
        mutate(genome)
        return genome
    }

    private fun newGene(): Gene {
        return Gene(null, null, 0f, true, 0)
    }

    private fun copyGene(gene: Gene): Gene {
        return gene.copy(intoNeuronId = gene.intoNeuronId,
                outNeuronId = gene.outNeuronId,
                weight = gene.weight,
                enabled = gene.enabled,
                innovation = gene.innovation)
    }

    private fun newNeuron(neuronType: NeuronType): Neuron {
        return Neuron(UUID.randomUUID().toString(), hashSetOf(), 0f, neuronType)
    }

    private fun newNetwork(): Network {
        val network = Network(mutableListOf(), mutableListOf(), mutableListOf())

        for (i in 0 until inputSize) {
            network.inputNeurons.add(newNeuron(NeuronType.Input))
        }

        for (index in 0 until neuralNetworkController.outputs) {
            network.outputNeurons.add(newNeuron(NeuronType.Output))
        }

        return network
    }

    private fun generateNetwork(genome: Genome) {
        genome.genes.sortBy { genome.getNeuronById(it.outNeuronId)?.let { it1 -> genome.getNeuronIndex(it1) } }

        genome.genes.forEach { gene ->
            if (gene.enabled) {
                if (genome.getNeuronById(gene.outNeuronId) == null) {
                    val newNeuron = newNeuron(NeuronType.HiddenLayer)
                    genome.network.hiddenLayerNeurons.add(newNeuron)
                    gene.outNeuronId = newNeuron.id
                }
                val neuron = gene.outNeuronId
                genome.getNeuronById(neuron)!!.incoming.add(gene)
                if (genome.getNeuronById(gene.intoNeuronId) == null) {
                    val newNeuron = newNeuron(NeuronType.HiddenLayer)
                    genome.network.hiddenLayerNeurons.add(newNeuron)
                    gene.intoNeuronId = newNeuron.id
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
                val other = network.getNeuronById(incoming.intoNeuronId)

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

        val child = newGenome(newNetwork())

        val innovations2 = HashMap<Int, Gene>()

        genome2.genes.forEach { gene ->
            innovations2[gene.innovation] = gene
        }

        genome1.genes.forEach { gene1 ->
            val gene2 = innovations2[gene1.innovation]

            if (gene2 != null && Random.nextBoolean() && gene2.enabled) {
                child.genes.add(copyGene(gene2))
            } else {
                child.genes.add(copyGene(gene1))
            }
        }

        child.maxNeuron = if (genome1.getMaxNeuronIndex() > genome2.getMaxNeuronIndex()) genome1.maxNeuron else genome2.maxNeuron

        for ((mutation, rate) in genome1.mutationRates) {
            child.mutationRates[mutation] = rate
        }

        return child
    }

    private fun randomNeuron(genome: Genome, genes: MutableList<Gene>, nonInput: Boolean): Neuron? {
        val neurons = mutableListOf<Neuron>()

        if (nonInput.not()) {
            neurons.addAll(genome.network.inputNeurons)
        }

        neurons.addAll(genome.network.outputNeurons)

        genes.forEach {
            val intoNeuron = genome.getNeuronById(it.intoNeuronId)
            val outNeuron = genome.getNeuronById(it.intoNeuronId)

            if (nonInput.not() || intoNeuron?.isInputNeuron?.not() == true) {
                intoNeuron?.let { neurons.add(it) }
            }

            if (nonInput.not() || outNeuron?.isInputNeuron?.not() == true) {
                outNeuron?.let { neurons.add(it) }
            }
        }

        return if (neurons.isEmpty()) null else neurons.random()
    }

    private fun containsLink(genes: MutableList<Gene>, link: Gene): Boolean {
        genes.forEach { gene ->
            if (gene.intoNeuronId == link.intoNeuronId && gene.outNeuronId == link.outNeuronId) {
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
        var neuron1 = randomNeuron(genome, genome.genes, false)
        var neuron2 = randomNeuron(genome, genome.genes, true)

        if (neuron1 == null || neuron2 == null) return

        val newLink = newGene()

        if (neuron1.isInputNeuron && neuron2.isInputNeuron) {
            return
        }

        if (neuron2.isInputNeuron) {
            val temp = neuron1
            neuron1 = neuron2
            neuron2 = temp
        }

        newLink.intoNeuronId = neuron1.id
        newLink.outNeuronId = neuron2.id

        if (forceBias) newLink.intoNeuronId = genome.network.inputNeurons.last().id

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

        val gene1 = copyGene(gene)
        gene1.outNeuronId = genome.maxNeuron?.id
        gene1.weight = 1f
        gene1.innovation = newInnovation()
        gene1.enabled = true
        genome.genes.add(gene1)

        val gene2 = copyGene(gene)
        gene2.intoNeuronId = genome.maxNeuron?.id
        gene2.innovation = newInnovation()
        gene2.enabled = true
        genome.genes.add(gene2)
    }

    private fun enableDisableMutate(genome: Genome, enable: Boolean) {
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

    private fun calculateAverageFitness(species: Species) {
        var total = 0f

        species.genomes.forEach {
            total += it.globalRank
        }

        species.averageFitness = total / species.genomes.size
    }

    private fun totalAverageFitness(): Float {
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
            val childSpecies = newSpecies()
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
                nextGenome()
            }
            initRun()
        } else {
            pool = newPool()

            for (x in 0 until neuralNetworkParameters.POPULATION) {
                addToSpecies(basicGenome(newNetwork()))
            }

            pool.setNewCurrentSpecies(pool.species.first())

            initRun()
        }
    }

    private fun initRun() {
        generateNetwork(pool.currentGenome!!)
    }

    private fun evaluateCurrent(): T? {
        return evaluateNetwork(pool.currentGenome!!.network, neuralNetworkController.inputs)
    }

    private fun nextGenome() {
        val genomeIterator = pool.currentSpecies!!.genomes.listIterator(pool.currentSpecies!!.genomes.indexOf(pool.currentGenome) + 1)

        if (genomeIterator.hasNext()) {
            pool.currentGenome = genomeIterator.next()
        }

        if (genomeIterator.hasNext().not()) {
            val speciesIterator = pool.species.listIterator(pool.species.indexOf(pool.currentSpecies) + 1)

            pool.currentGenome = pool.currentSpecies!!.genomes.first()

            if (speciesIterator.hasNext()) {
                pool.setNewCurrentSpecies(speciesIterator.next())
            }

            if (speciesIterator.hasNext().not()) {
                newGeneration()
                pool.setNewCurrentSpecies(pool.species.first())
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

        pool.setNewCurrentSpecies(pool.species.first())

        while (fitnessAlreadyMeasured()) {
            nextGenome()
        }

        initRun()
    }

    private fun playTop() {
        var maxfitness = 0f
        lateinit var maxs: Species
        lateinit var maxg: Genome

        pool.species.forEach { species ->
            species.genomes.forEach { genome ->
                if (genome.fitness > maxfitness) {
                    maxfitness = genome.fitness
                    maxs = species
                    maxg = genome
                }
            }
        }

        pool.currentSpecies = maxs
        pool.currentGenome = maxg
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