package com.ps.simplepapersoccer.viewmodel

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.ps.simplepapersoccer.enums.DifficultyEnum
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.game.GameViewDrawData
import com.ps.simplepapersoccer.gameObjects.game.Victory
import com.ps.simplepapersoccer.gameObjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import com.ps.simplepapersoccer.gameObjects.player.Player
import com.ps.simplepapersoccer.event.CompletableLiveEventLiveData
import com.ps.simplepapersoccer.event.LiveEventLiveData
import java.util.*
import kotlin.collections.HashSet

class GameViewModel: ViewModel() {
    lateinit var gameHandler: GameHandler
    lateinit var difficulty: String

    var players = ArrayList<IPlayer>()

    val player1Color = Color.BLUE
    val player2Color = Color.RED

    val executeUpdateGameViewTaskLiveData = LiveEventLiveData<GameViewDrawData>()
    val playerTurnTextLiveData = CompletableLiveEventLiveData()
    val winnerLiveData = LiveEventLiveData<Victory>()
    val reDrawLiveData = CompletableLiveEventLiveData()
    val drawPartialMoveLiveData = LiveEventLiveData<PartialMove>()

    fun setGameMode(gameMode: Int, playerName: String) {
        if (players.isNotEmpty()) {
            players.shuffle()
            return
        }

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
        executeUpdateGameViewTaskLiveData.postWrappedValue(GameViewDrawData(linesToDraw, madeTheMove, gameHandler.currentPlayersTurn, ballNode, getAllNodeNeighbors(ballNode)))
    }

    fun getAllNodeNeighbors(node: Node): HashSet<Node> {
        return node.neighbors
    }

    fun updatePlayerTurnText() {
        playerTurnTextLiveData.postWrappedValue(true)
    }
}