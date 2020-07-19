package test.simplepapersoccer

import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkAI
import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.ai.randomAI.RandomAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class AIUnitTests {

    private val testRuns = 1000

    private fun runTestGame(players: ArrayList<IPlayer>, handler: Handler) {
        val totalNumberOfTurns = mutableListOf<Int>()

        for (i in 0 until testRuns) {
            players.shuffle()
            val gameHandler = GameHandler(null, 10, 12, players, Dispatchers.Unconfined, handler)
            gameHandler.updateGameState()
            if (i == testRuns / 2) {
                System.gc()
                System.runFinalization()
            }

            if (gameHandler.winner?.winner?.playerName == NeuralNetworkAI::class.java.simpleName) {
                println(gameHandler.winner?.winner?.playerName)
                println(gameHandler.winner?.victoryConditionEnum?.name)
            }

            totalNumberOfTurns.add(gameHandler.numberOfTurns)
        }

        println("Average number of turns = ${totalNumberOfTurns.sum() / testRuns}")
        println("Max number of turns = ${totalNumberOfTurns.max()}")
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        val handler = mock<Handler> {
            on { post(any()) }.thenAnswer { invocation ->
                val msg = invocation.getArgument<Runnable>(0)
                msg.run()
                null
            }
        }

        val player1: IPlayer = NeuralNetworkAI(null, 1, 0)
        val player2: IPlayer = EuclideanAI(2, 1)
        val players = arrayListOf(player1, player2)

        runTestGame(players, handler)

        assertEquals(0.5, (player1.score.toDouble() / (player1.score + player2.score)))
    }
}