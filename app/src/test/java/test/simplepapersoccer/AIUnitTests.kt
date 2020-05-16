package test.simplepapersoccer

import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class AIUnitTests {
    var gameHandler: GameHandler? = null
    private lateinit var player1: IPlayer
    private lateinit var player2: IPlayer
    private val players get() = arrayListOf(player1, player2)

    @Before
    fun init()
    {
        player1 = AIPlayer(DifficultyEnum.VeryHard, "TestSubject", 1, 0, true)
        player2 = AIPlayer(DifficultyEnum.Medium, "TestOpponent", 2, 0, true)
        createGameHandler(players)
    }

    private fun createGameHandler(players: ArrayList<IPlayer>) {
        gameHandler = GameHandler(null, 10, 12, players, GameModeEnum.AI_VS_AI)
    }

    @Test
    fun higherDifficultyWinVsLowerDifficulty() {
        for (i in 0 until 3) {
            createGameHandler(players)
            gameHandler?.updateGameState()
        }

        assertEquals(3, player1.score)
    }
}