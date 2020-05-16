package com.ps.simplepapersoccer.ai

import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class GameAIHandler(private val gameHandler: GameHandler) {
    fun makeAIMove(aiPlayer : AIPlayer) {
        CoroutineScope(Dispatchers.IO).launch {
            makeAIMoveAsync(aiPlayer)
        }
    }

    private suspend fun makeAIMoveAsync(aiPlayer : AIPlayer)  {
        val aiMove = aiPlayer.gameAI.makeMove(deepCopy(gameHandler))
        gameHandler.aiMakeMove(aiMove)
    }

    private fun <T : Serializable> deepCopy(obj: T): T {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                objectOutputStream.writeObject(obj)
                val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
                val objectInputStream  = ObjectInputStream(byteArrayInputStream)
                @Suppress("unchecked_cast")
                return objectInputStream.readObject() as T
            }

        }
    }
}
