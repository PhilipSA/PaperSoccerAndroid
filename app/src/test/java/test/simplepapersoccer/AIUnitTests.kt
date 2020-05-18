package test.simplepapersoccer

import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.ai.minimaxAI.MinimaxAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class AIUnitTests {
    private lateinit var gameHandler: GameHandler
    private lateinit var player1: IPlayer
    private lateinit var player2: IPlayer
    private val players get() = arrayListOf(player1, player2)

    @Before
    fun init() {
        player1 = AIPlayer(MinimaxAI::class.java.simpleName, 1, 0, true)
        player2 = AIPlayer(EuclideanAI::class.java.simpleName,2, 0, true)
        createGameHandler(players)
    }

    private fun createGameHandler(players: ArrayList<IPlayer>) {
        gameHandler = GameHandler(null, 10, 12, players, Dispatchers.Main)
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        runBlocking {
            for (i in 0 until 3) {
                createGameHandler(players)
                gameHandler.updateGameState()
            }

            assertEquals(0, player2.score)
            assertEquals(3, player1.score)
        }
    }
}