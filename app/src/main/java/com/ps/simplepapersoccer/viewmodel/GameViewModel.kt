package com.ps.simplepapersoccer.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.game.GameViewDrawData
import com.ps.simplepapersoccer.gameObjects.game.IGameHandlerListener
import com.ps.simplepapersoccer.gameObjects.game.Victory
import com.ps.simplepapersoccer.gameObjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import com.ps.simplepapersoccer.gameObjects.player.Player
import java.util.*
import kotlin.collections.HashSet

class GameViewModel(application: Application): AndroidViewModel(application), IGameHandlerListener {
    lateinit var gameHandler: GameHandler

    lateinit var player1Arg: String
    lateinit var player2Arg: String

    val isPlayerVsAi get() = players.count { it.isAi } == 1

    var players = ArrayList<IPlayer>()

    val player1Color = Color.BLUE
    val player2Color = Color.RED

    val executeUpdateGameViewTaskLiveData = MutableLiveData<GameViewDrawData>()
    val playerTurnTextLiveData = MutableLiveData<Boolean>()
    override val winnerLiveData = MutableLiveData<Victory>()
    override val reDrawLiveData = MutableLiveData<Boolean>()
    override val drawPartialMoveLiveData = MutableLiveData<PartialMove>()

    fun initPlayers(playerName: String) {
        if (players.isNotEmpty()) {
            players.shuffle()
            return
        }

        if (player1Arg == "Player") players.add(Player(playerName, 1, player1Color, false)) else {
            players.add(AIPlayer.createAi(getApplication(), player1Arg, 1, player1Color))
        }

        if (player2Arg == "Player") players.add(Player("Player2", 2, player2Color, false)) else {
            players.add(AIPlayer.createAi(getApplication(), player2Arg, 2, player2Color))
        }
    }

    fun addDrawDataToQueue(linesToDraw: LinesToDraw, ballNode: Node, madeTheMove: IPlayer) {
        executeUpdateGameViewTaskLiveData.value = GameViewDrawData(linesToDraw, madeTheMove, gameHandler.currentPlayersTurn, ballNode, getAllNodeNeighbors(ballNode))
    }

    fun getAllNodeNeighbors(node: Node): HashSet<Node> {
        return node.connectedNodes.toHashSet()
    }

    fun updatePlayerTurnText() {
        playerTurnTextLiveData.value = true
    }
}