package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.neuralnetworkAI.NeuralNetworkAI
import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.ai.jonasAI.JonasAI
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.gameobjects.game.GameHandler
import com.ps.simplepapersoccer.gameobjects.game.Victory
import com.ps.simplepapersoccer.gameobjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class AIUnitTests {

    private val testRuns = 10000

    private fun runTestGame(players: ArrayList<IPlayer>) {
        val totalNumberOfTurns = mutableListOf<Int>()
        var neuralNetworkAIVictoryBoard: String? = null
        var neuralNetworkAIVictoryType: Victory? = null

        val player1 = players.first()
        val player2 = players[1]

        for (i in 0 until testRuns) {
            players.shuffle()
            val gameHandler = GameHandler(null, 8, 11, players, Dispatchers.Unconfined)
            gameHandler.updateGameState()
            if (i == testRuns / 2) {
                System.gc()
                System.runFinalization()
            }

            if (gameHandler.winner?.winner is NeuralNetworkAI) {
                neuralNetworkAIVictoryBoard = gameHandler.gameBoard.toString()
                neuralNetworkAIVictoryType = gameHandler.winner
            }

            totalNumberOfTurns.add(gameHandler.numberOfTurns)
        }

        println((player1.score.toDouble() / (player1.score + player2.score)) * 100)

        println(neuralNetworkAIVictoryBoard)
        println(neuralNetworkAIVictoryType)

        println("Average number of turns = ${totalNumberOfTurns.sum() / testRuns}")
        println("Max number of turns = ${totalNumberOfTurns.max()}")
    }

    @Test
    fun neuralNetworkAITraining() {
        val player1 = NeuralNetworkAI(null, 1, 0)
        val player2 = NeuralNetworkAI(null, 2, 1, "temp2.pool")
        val players = arrayListOf(player1 as IPlayer, player2)

        runTestGame(players)

        player1.neuralNetworkCache.createBackupFile()
        player2.neuralNetworkCache.createBackupFile()
    }

    @Test
    fun neuralNetworkAIVsEuclideanAITraining() {
        val player1 = NeuralNetworkAI(null, 1, 0)
        val player2 = EuclideanAI(2, 1)
        val players = arrayListOf(player1 as IPlayer, player2)

        runTestGame(players)

        player1.neuralNetworkCache.createBackupFile()
    }

    @Test
    fun neuralNetworkAIVsJonasAITraining() {
        val player1 = NeuralNetworkAI(null, 1, 0)
        val player2 = JonasAI(2, 1)
        val players = arrayListOf(player1 as IPlayer, player2)

        runTestGame(players)

        player1.neuralNetworkCache.createBackupFile()

        assertEquals(0.5, (player1.score.toDouble() / (player1.score + player2.score)))
    }

    @Test
    fun neuralNetworkAIVsMinimaxAITraining() {
        val player1 = NeuralNetworkAI(null, 1, 0)
        val player2 = MinimaxAI(2, 1)
        val players = arrayListOf(player1 as IPlayer, player2)

        runTestGame(players)

        player1.neuralNetworkCache.createBackupFile()
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        val player1: IPlayer = NeuralNetworkAI(null, 1, 0)
        val player2: IPlayer = EuclideanAI(2, 1)
        val players = arrayListOf(player1, player2)

        runTestGame(players)

        assertEquals(0.5, (player1.score.toDouble() / (player1.score + player2.score)))
    }
}