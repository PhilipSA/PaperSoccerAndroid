package test.simplepapersoccer

import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.Enums.GameModeEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Player.AIPlayer
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer
import org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.Before

class MinimaxAIUnitTests {
    var gameHandler: GameHandler? = null
    var player1: IPlayer? = null
    var player2: IPlayer? = null

    @Before
    fun init()
    {
        player1 = AIPlayer(DifficultyEnum.VeryHard, "TestSubject", 1, 0, true)
        player2 = AIPlayer(DifficultyEnum.Hard, "TestOpponent", 2, 0, true)

        var players = arrayListOf(player1!!, player2!!)

        gameHandler = GameHandler(null, 10, 12, players, GameModeEnum.AI_VS_AI, false)
    }

    @Test
    @Throws(Exception::class)
    fun higher_difficulty_win_vs_lower_difficulty() {
        gameHandler?.UpdateGameState()
        assertEquals(player1?.score, 1)
    }
}