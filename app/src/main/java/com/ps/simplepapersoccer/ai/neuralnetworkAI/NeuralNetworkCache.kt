package com.ps.simplepapersoccer.ai.neuralnetworkAI

import android.content.Context
import android.util.Log
import com.ps.simplepapersoccer.data.constants.StringConstants
import java.io.*
import java.util.zip.Deflater
import java.util.zip.Inflater

class NeuralNetworkCache(context: Context?,
                         private val networkFileBackupEnabled: Boolean,
                         private val fileName: String = StringConstants.NEURAL_NETWORK_FILE_NAME) {

    private val poolCacheDirectory = context?.filesDir?.toString()
            ?: "C:\\Users\\Admin\\Documents\\AlphaZero"

    private var cachedPool: NeuralNetwork.Pool? = null

    fun savePool(pool: NeuralNetwork.Pool) {
        cachedPool = pool
    }

    fun loadPool(): NeuralNetwork.Pool? {
        return if (cachedPool != null) cachedPool else loadFile()
    }

    fun createBackupFile() {
        cachedPool?.let { writeFile(it) }
        cachedPool = null
    }

    private fun writeFile(pool: NeuralNetwork.Pool) {
        if (networkFileBackupEnabled.not()) return
        val file = File(poolCacheDirectory, fileName)
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
            val poolDto = NeuralNetwork.PoolDto(poolByteArray.size, compressedPool)

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

    private fun loadFile(): NeuralNetwork.Pool? {
        if (networkFileBackupEnabled.not()) return null

        val file = File(poolCacheDirectory, fileName)

        return if (file.exists()) {

            var pool: NeuralNetwork.Pool? = null

            val poolDto = ObjectInputStream(ByteArrayInputStream(file.readBytes(), 0, file.length().toInt())).use {
                it.readObject() as NeuralNetwork.PoolDto
            }

            val decompresser = Inflater()
            decompresser.setInput(poolDto.pool)
            val result = ByteArray(poolDto.uncompressedSize)
            val resultLength = decompresser.inflate(result)
            decompresser.end()

            ObjectInputStream(ByteArrayInputStream(result, 0, resultLength)).use {
                pool = it.readObject() as NeuralNetwork.Pool
            }

            pool
        } else {
            null
        }
    }
}