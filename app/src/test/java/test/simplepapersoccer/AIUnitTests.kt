package test.simplepapersoccer

import android.app.Application
import android.os.Handler
import android.os.Message
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.ps.simplepapersoccer.ai.alphazeroAI.AlphaZeroAI
import com.ps.simplepapersoccer.ai.euclideanAI.EuclideanAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AIUnitTests {
    private lateinit var gameHandler: GameHandler
    private lateinit var player1: IPlayer
    private lateinit var player2: IPlayer
    private val players get() = arrayListOf(player1, player2)

    private val handler = mock<Handler> {
        on { post(any()) }.thenAnswer { invocation ->
            val msg = invocation.getArgument<Runnable>(0)
            msg.run()
            null
        }
    }
    @Before
    fun init() {
        player1 = AIPlayer(ApplicationProvider.getApplicationContext<Application>(), AlphaZeroAI::class.java.simpleName, 1, 0, true)
        player2 = AIPlayer(ApplicationProvider.getApplicationContext<Application>(), EuclideanAI::class.java.simpleName, 2, 0, true)
        createGameHandler(players)
    }

    private fun createGameHandler(players: ArrayList<IPlayer>) {
        gameHandler = GameHandler(null, 10, 12, players, Dispatchers.Main, handler)
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        runBlocking {
            for (i in 0 until 2000) {
                createGameHandler(players)
                gameHandler.updateGameState()
            }

            assertEquals(0, player2.score)
            assertEquals(3, player1.score)
        }
    }
}