package test.simplepapersoccer

import com.ps.simplepapersoccer.AI.MinimaxAI.MinimaxAI
import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.Enums.GameModeEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Player

import org.junit.Test
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.`when`

class MinimaxAIUnitTests {

    var gameHandler: GameHandler? = null
    var miniMaxAi: MinimaxAI? = null
    var player1: Player? = null
    var player2: Player? = null

    @Before
    fun init()
    {
        player1 = Player("TestSubject", 1, 0, true)
        player2 = Player("TestOpponent", 2, 0, true)

        var players = arrayListOf(player1!!, player2!!)

        gameHandler = GameHandler(null, 10, 12, DifficultyEnum.VeryHard, players, GameModeEnum.AI_VS_AI)
        miniMaxAi = MinimaxAI(2000)
    }

    @Test
    @Throws(Exception::class)
    fun minimaxAi_makes_valid_move() {
        gameHandler?.UpdateGameState()
        assert(player1?.score === 1)
    }
}