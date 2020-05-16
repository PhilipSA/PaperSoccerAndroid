package test.simplepapersoccer

import androidx.lifecycle.MutableLiveData
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.game.IGameHandlerListener
import com.ps.simplepapersoccer.gameObjects.game.Victory
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class AIUnitTests {
    lateinit var gameHandler: GameHandler
    private lateinit var player1: IPlayer
    private lateinit var player2: IPlayer
    private val players get() = arrayListOf(player1, player2)

    @Before
    fun init() {
        player1 = AIPlayer(DifficultyEnum.VeryHard, "TestSubject", 1, 0, true)
        player2 = AIPlayer(DifficultyEnum.Easy, "TestOpponent", 2, 0, true)
        createGameHandler(players)
    }

    private fun createGameHandler(players: ArrayList<IPlayer>) {
        gameHandler = GameHandler(null, 10, 12, players, GameModeEnum.AI_VS_AI)
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        runBlockingTest {
            for (i in 0 until 3) {
                createGameHandler(players)
                gameHandler.updateGameState()
            }

            assertEquals(3, player1.score)
        }
    }
}