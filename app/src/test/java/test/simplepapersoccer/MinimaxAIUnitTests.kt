package test.simplepapersoccer

import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.gameObjects.Game.GameHandler
import com.ps.simplepapersoccer.gameObjects.Player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer
import org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.Before

class MinimaxAIUnitTests {
    var gameHandler: GameHandler? = null
    lateinit var player1: IPlayer
    lateinit var player2: IPlayer

    @Before
    fun init()
    {
        player1 = AIPlayer(DifficultyEnum.VeryHard, "TestSubject", 1, 0, true)
        player2 = AIPlayer(DifficultyEnum.Hard, "TestOpponent", 2, 0, true)

        val players = arrayListOf(player1, player2)

        gameHandler = GameHandler(null, 10, 12, players, GameModeEnum.AI_VS_AI, false, false)
    }

    @Test
    @Throws(Exception::class)
    fun higher_difficulty_win_vs_lower_difficulty() {
        gameHandler?.UpdateGameState()
        assertEquals(1, player1.score)
    }
}