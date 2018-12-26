package com.ps.simplepapersoccer.viewmodel

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.gameObjects.Game.GameHandler
import com.ps.simplepapersoccer.gameObjects.Game.GameViewDrawData
import com.ps.simplepapersoccer.gameObjects.Game.Geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.gameObjects.Player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer
import com.ps.simplepapersoccer.gameObjects.Player.Player
import java.util.*

class GameViewModel: ViewModel() {
    lateinit var gameHandler: GameHandler
    lateinit var difficulty: String

    var players = ArrayList<IPlayer>()

    val player1Color = Color.BLUE
    val player2Color = Color.RED

    val executeUpdateGameViewTaskLiveData = MutableLiveData<GameViewDrawData>()
    val playerTurnTextLiveData = MutableLiveData<Boolean>()

    fun setGameMode(gameMode: Int, playerName: String) {
        when (gameMode) {
            GameModeEnum.PLAYER_VS_AI -> players = assignPlayerAndAi(difficulty, DifficultyEnum.valueOf(difficulty), playerName)
            GameModeEnum.MULTIPLAYER_MODE -> {
                players.add(Player(playerName, 1, player1Color, false))
                players.add(Player("Player2", 2, player2Color, false))
            }
            else -> players = assignTwoAi(difficulty, DifficultyEnum.valueOf(difficulty))
        }
    }

    private fun assignPlayerAndAi(difficulty: String, difficultyEnum: DifficultyEnum, playerName: String): ArrayList<IPlayer> {
        val players = ArrayList<IPlayer>()
        val random = Random()
        if (random.nextBoolean()) {
            players.add(Player(playerName, 1, player1Color, false))
            players.add(AIPlayer(difficultyEnum, "Ai$difficulty", 2, player2Color, true))
        } else {
            players.add(AIPlayer(difficultyEnum, "Ai$difficulty", 1, player1Color, true))
            players.add(Player(playerName, 2, player2Color, false))
        }
        return players
    }

    private fun assignTwoAi(difficulty: String, difficultyEnum: DifficultyEnum): ArrayList<IPlayer> {
        val players = ArrayList<IPlayer>()
        val random = Random()
        if (random.nextBoolean()) {
            players.add(AIPlayer(difficultyEnum, "OtherAI", 1, player1Color, true))
            players.add(AIPlayer(difficultyEnum, "Ai$difficulty", 2, player2Color, true))
        } else {
            players.add(AIPlayer(difficultyEnum, "Ai$difficulty", 1, player1Color, true))
            players.add(AIPlayer(difficultyEnum, "OtherAI", 2, player2Color, true))
        }
        return players
    }

    fun addDrawDataToQueue(linesToDraw: LinesToDraw, ballNode: Node, madeTheMove: IPlayer) {
        executeUpdateGameViewTaskLiveData.value = GameViewDrawData(linesToDraw, madeTheMove, gameHandler.currentPlayersTurn, ballNode, getAllNodeNeighbors(ballNode))
    }

    fun getAllNodeNeighbors(node: Node): MutableList<Node> {
        return node.neighbors
    }

    fun updatePlayerTurnText() {
        playerTurnTextLiveData.postValue(true)
    }
}