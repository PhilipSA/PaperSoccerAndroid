package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.math.*
import kotlin.random.Random

class NeuralNetwork<T>(context: Context?,
                       private val neuralNetworkController: INeuralNetworkController<T>,
                       private val networkBackupEnabled: Boolean,
                       private val neuralNetworkParameters: NeuralNetworkParameters = NeuralNetworkParameters()) {

    data class Neuron(
            val incoming: HashSet<Gene>,
            var value: Double
    ) : Serializable

    data class Genome(
            val genes: MutableList<Gene>,
            var fitness: Double,
            val adjustedFitness: Int,
            var network: Network,
            var maxNeuron: Int,
            var globalRank: Int,
            val mutationRates: HashMap<String, Double>
    ) : Serializable

    data class Species(
            var topFitness: Double,
            var staleness: Int,
            val genomes: MutableList<Genome>,
            var averageFitness: Double
    ) : Serializable

    data class Pool(
            var species: MutableList<Species>,
            var generation: Int,
            var currentSpeciesIndex: Int,
            var currentGenomeIndex: Int,
            var maxFitness: Double,
            var innovation: Int
    ) : Serializable {
        val currentSpecies get() = species.getOrNull(currentSpeciesIndex)
        val currentGenome get() = currentSpecies?.genomes?.getOrNull(currentGenomeIndex)
    }

    data class Gene(
            var into: Int,
            var out: Int,
            var weight: Double,
            var enabled: Boolean,
            var innovation: Int
    ) : Serializable

    data class Network(
            val neurons: HashMap<Int, Neuron>
    ) : Serializable

    data class PoolDto(
            val uncompressedSize: Int,
            val pool: ByteArray
    ) : Serializable

    lateinit var pool: Pool

    private val poolCacheDirectory = context?.filesDir?.toString()
            ?: "C:\\Users\\Admin\\Documents\\AlphaZero"

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
        return Pool(mutableListOf(), 0, 0, 0, 0.0, neuralNetworkController.outputs)
    }

    private fun newSpecies(): Species {
        return Species(0.0, 0, mutableListOf(), 0.0)
    }

    private fun newGenome(): Genome {
        return Genome(mutableListOf(), 0.0, 0, Network(hashMapOf()), 0, 0,
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
        genome.maxNeuron = neuralNetworkController.inputs.size - 1
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
        return Neuron(hashSetOf(), 0.0)
    }

    private fun generateNetwork(genome: Genome) {
        val network = Network(hashMapOf())

        for (i in neuralNetworkController.inputs.indices) {
            network.neurons[i] = newNeuron()
        }

        for (index in 0 until neuralNetworkController.outputs) {
            network.neurons[neuralNetworkParameters.MAX_NODES + index] = newNeuron()
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

    private fun evaluateNetwork(network: Network, inputsArg: List<Double>): T? {
        if (inputsArg.size != neuralNetworkController.inputs.size) {
            throw(Exception("No"))
        }

        for (index in neuralNetworkController.inputs.indices) {
            network.neurons[index]?.value = inputsArg[index]
        }

        network.neurons.values.forEach { neuron ->
            var sum = 0.0

            neuron.incoming.forEach { incoming ->
                val other = network.neurons[incoming.into]

                sum += incoming.weight * other!!.value
            }

            if (neuron.incoming.size > 0) {
                neuron.value = sigmoid(sum)
            }
        }

        val allOutputNeurons = mutableListOf<Double>()

        for (index in 0 until neuralNetworkController.outputs) {
            val neuronValue = network.neurons[neuralNetworkParameters.MAX_NODES + index]!!.value
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

        val child = newGenome()

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

        child.maxNeuron = max(genome1.maxNeuron, genome2.maxNeuron)

        for ((mutation, rate) in genome1.mutationRates) {
            child.mutationRates[mutation] = rate
        }

        return child
    }

    private fun randomNeuron(genes: MutableList<Gene>, nonInput: Boolean): Int {
        val neurons = hashMapOf<Int, Boolean>()

        if (nonInput.not()) {
            for (i in neuralNetworkController.inputs.indices) {
                neurons[i] = true
            }
        }

        for (i in 0 until neuralNetworkController.outputs) {
            neurons[neuralNetworkParameters.MAX_NODES + i] = true
        }

        genes.forEach {
            if (nonInput.not() || it.into > neuralNetworkController.inputs.size) {
                neurons[it.into] = true
            }

            if (nonInput.not() || it.out > neuralNetworkController.inputs.size) {
                neurons[it.out] = true
            }
        }

        val count = neurons.size
        var n = Random.nextInt(0, count)

        for ((k, _) in neurons) {
            n -= 1
            if (n == -1) return k
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
        val step = genome.mutationRates.getValue("step")

        genome.genes.forEach { gene ->
            if (Random.nextDouble(0.0, 1.0) < neuralNetworkParameters.PERTURB_CHANCE) {
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

        if (neuron1 < neuralNetworkController.inputs.size && neuron2 < neuralNetworkController.inputs.size) {
            //Both input nodes
            return
        }

        if (neuron2 < neuralNetworkController.inputs.size) {
            val temp = neuron1
            neuron1 = neuron2
            neuron2 = temp
        }

        newLink.into = neuron1
        newLink.out = neuron2

        if (forceBias) newLink.into = neuralNetworkController.inputs.size - 1

        if (containsLink(genome.genes, newLink)) return

        newLink.innovation = newInnovation()
        newLink.weight = Random.nextDouble(0.0, 1.0) * 4 - 2

        genome.genes.add(newLink)
    }

    private fun nodeMutate(genome: Genome) {
        if (genome.genes.size == 0) return

        ++genome.maxNeuron

        val gene = genome.genes.random()

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
        return if (Random.nextBoolean()) {
            0.95 * rate
        } else {
            1.05263 * rate
        }
    }

    private fun mutate(genome: Genome) {
        for ((mutation, rate) in genome.mutationRates) {
            val value = mutateHelper(rate)
            genome.mutationRates[mutation] = value
        }

        if (Random.nextDouble(0.0, 1.0) < genome.mutationRates.getValue("connections")) {
            pointMutate(genome)
        }

        var p = genome.mutationRates.getValue("link")
        if (Random.nextDouble(0.0, 1.0) < p - floor(p)) {
            linkMutate(genome, false)
        }

        p = genome.mutationRates.getValue("bias")
        if (Random.nextDouble(0.0, 1.0) < p - floor(p)) {
            linkMutate(genome, true)
        }

        p = genome.mutationRates.getValue("node")
        if (Random.nextDouble(0.0, 1.0) < p - floor(p)) {
            nodeMutate(genome)
        }

        p = genome.mutationRates.getValue("enable")
        if (Random.nextDouble(0.0, 1.0) < p - floor(p)) {
            enableDisableMutate(genome, true)
        }

        p = genome.mutationRates.getValue("disable")
        if (Random.nextDouble(0.0, 1.0) < p - floor(p)) {
            enableDisableMutate(genome, false)
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
            genome.globalRank = g
        }
    }

    private fun calculateAverageFitness(species: Species) {
        var total = 0.0

        species.genomes.forEach {
            total += it.globalRank
        }

        species.averageFitness = if (species.genomes.size == 0) 0.0 else total / species.genomes.size
    }

    private fun totalAverageFitness(): Double {
        return pool.species.sumByDouble { species ->
            species.averageFitness
        }
    }

    private fun cullSpecies(cutToOne: Boolean) {
        pool.species.forEach { species ->
            species.genomes.sortByDescending { it.fitness }

            var remaining = ceil((species.genomes.size / 2).toDouble())
            if (cutToOne) remaining = 1.0

            species.genomes.subList(remaining.toInt(), species.genomes.size).clear()
        }
    }

    private fun breedChild(species: Species): Genome {
        val child = if (Random.nextDouble(0.0, 1.0) < neuralNetworkParameters.CROSSOVER_CHANCE) {
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
        if (loadFile().not()) {
            pool = newPool()

            for (x in 0 until neuralNetworkParameters.POPULATION) {
                addToSpecies(basicGenome())
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

    private fun nextGenome() {
        ++pool.currentGenomeIndex

        if (pool.currentGenomeIndex + 1 > pool.currentSpecies?.genomes?.size ?: 0) {
            pool.currentGenomeIndex = 0
            ++pool.currentSpeciesIndex

            if (pool.currentSpeciesIndex + 1 > pool.species.size) {
                newGeneration()
                pool.currentSpeciesIndex = 0
            }
        }
    }

    private fun fitnessAlreadyMeasured(): Boolean {
        return pool.currentGenome?.fitness ?: -1 != 0.0
    }

    fun cutOff() {
        val fitness = neuralNetworkController.fitnessEvaluation()

        pool.currentGenome?.fitness = fitness

        if (fitness > pool.maxFitness) {
            println("Max fitness: ${pool.maxFitness} Gen ${pool.generation} species ${pool.species.sumBy { it.averageFitness.toInt() }} genome: ${pool.currentGenomeIndex}")
            pool.maxFitness = fitness
            writeFile()
        }

        pool.currentSpeciesIndex = 0
        pool.currentGenomeIndex = 0

        while (fitnessAlreadyMeasured()) {
            nextGenome()
        }

        initRun()
    }

    private fun playTop() {
        var maxfitness = 0.0
        var maxs = 0
        var maxg = 0

        pool.species.forEach { species ->
            species.genomes.forEach { genome ->
                if (genome.fitness > maxfitness) {
                    maxfitness = genome.fitness
                    maxs = pool.species.indexOf(species)
                    maxg = pool.currentSpecies!!.genomes.indexOf(genome)
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
                if (genome.fitness != 0.0) {
                    ++measured
                }
            }
        }

        return current
    }

    private fun writeFile() {
        if (networkBackupEnabled.not()) return
        val file = File(poolCacheDirectory, neuralNetworkParameters.FILE_NAME)
        try {
            file.createNewFile()

            val poolByteArrayOutput = ByteArrayOutputStream().use { byteArray ->
                ObjectOutputStream(byteArray).use {
                    it.writeObject(pool)
                }
                byteArray
            }

            val poolByteArray = poolByteArrayOutput.toByteArray()

            val input = ByteArray(poolByteArray.size)
            val compresser = Deflater()
            compresser.setInput(poolByteArray)
            compresser.finish()
            val resultLength = compresser.deflate(input)
            compresser.end()

            val compressedPool = input.copyOf(resultLength)
            val poolDto = PoolDto(poolByteArray.size, compressedPool)

            val poolDtoByteOutput = ByteArrayOutputStream().use { byteArray ->
                ObjectOutputStream(byteArray).use {
                    it.writeObject(poolDto)
                }
                byteArray
            }

            file.writeBytes(poolDtoByteOutput.toByteArray())
        } catch (e: IOException) {
            Log.d(NeuralNetworkAI::class.java.canonicalName, e.message, e)
        }
    }

    private fun loadFile(): Boolean {
        if (networkBackupEnabled.not()) return false

        val file = File(poolCacheDirectory, neuralNetworkParameters.FILE_NAME)

        return if (file.exists()) {

            val poolDto = ObjectInputStream(ByteArrayInputStream(file.readBytes(), 0, file.length().toInt())).use {
                it.readObject() as PoolDto
            }

            val decompresser = Inflater()
            decompresser.setInput(poolDto.pool)
            val result = ByteArray(poolDto.uncompressedSize)
            val resultLength = decompresser.inflate(result)
            decompresser.end()

            ObjectInputStream(ByteArrayInputStream(result, 0, resultLength)).use {
                pool = it.readObject() as Pool
            }

            while (fitnessAlreadyMeasured()) {
                nextGenome()
            }
            initRun()
            true
        } else {
            false
        }
    }
}