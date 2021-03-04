package com.ps.simplepapersoccer.activities

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.data.constants.StringConstants.PREF_PLAYER_NAME
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameobjects.game.GameHandler
import com.ps.simplepapersoccer.gameobjects.game.GameViewDrawData
import com.ps.simplepapersoccer.gameobjects.game.Victory
import com.ps.simplepapersoccer.gameobjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameobjects.game.geometry.Node
import com.ps.simplepapersoccer.gameobjects.game.geometry.TwoDimensionalPoint
import com.ps.simplepapersoccer.gameobjects.game.geometry.TwoDimensionalPointF
import com.ps.simplepapersoccer.gameobjects.move.PartialMove
import com.ps.simplepapersoccer.gameobjects.player.abstraction.IPlayer
import com.ps.simplepapersoccer.sound.FXPlayer
import com.ps.simplepapersoccer.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToLong

class GameActivity : AppCompatActivity() {
    private val fxPlayer by lazy { FXPlayer(this) }
    private var myName: String = ""
    private var playerActivityDataMap: MutableMap<IPlayer, TextView> = HashMap()

    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game)

        gameViewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        gameViewModel.player1Arg = intent.getStringExtra(ARG_PLAYER1)!!
        gameViewModel.player2Arg = intent.getStringExtra(ARG_PLAYER2)!!

        val playerName = sharedPreferences.getString(PREF_PLAYER_NAME, "Player")!!

        val gridSizeX = intent.getIntExtra(ARG_GRID_SIZE_X, 8)
        val gridSizeY = intent.getIntExtra(ARG_GRID_SIZE_Y, 11)

        myName = playerName

        player1_name!!.setTextColor(gameViewModel.player1Color)
        player2_name!!.setTextColor(gameViewModel.player2Color)

        gameViewModel.initPlayers(playerName)

        setPlayerNameTexts()

        playerActivityDataMap[gameViewModel.players[0]] = player1_name
        playerActivityDataMap[gameViewModel.players[1]] = player2_name

        gameViewModel.gameHandler = GameHandler(gameViewModel, gridSizeX, gridSizeY, gameViewModel.players, Dispatchers.Default)

        setPlayerTurnTextViewText()

        game_view?.init(getWidth(this), getHeight(this), gridSizeX, gridSizeY, gameViewModel, gameViewModel.gameHandler.gameBoard)
        game_view?.gameViewDrawData = GameViewDrawData(null, gameViewModel.gameHandler.currentPlayersTurn, gameViewModel.gameHandler.currentPlayersTurn,
                gameViewModel.gameHandler.ballNode,
                gameViewModel.getAllNodeNeighbors(gameViewModel.gameHandler.ballNode))

        if (gameViewModel.player1Arg == "Player" || gameViewModel.player2Arg == "Player") {
            game_view?.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val touchedNode = nodeCoordsToNode(motionEvent.x.roundToLong().toFloat() * 2, motionEvent.y.roundToLong().toFloat() * 2)
                    if (touchedNode != null) {
                        gameViewModel.gameHandler.playerMakeMove(touchedNode, getNonAIPlayer())
                    }
                }
                false
            }
        }

        registerObservers()

        gameViewModel.gameHandler.updateGameState()

        play_again_button.setOnClickListener {
            resetGame()
        }

        quit_button.setOnClickListener {
            quit()
        }
    }

    private fun registerObservers() {
        gameViewModel.executeUpdateGameViewTaskLiveData.observe(this, Observer {
            if (it != null) {
                executeUpdateGameViewTask(it)
                gameViewModel.executeUpdateGameViewTaskLiveData.value = null
            }
        })

        gameViewModel.playerTurnTextLiveData.observe(this, Observer {
            setPlayerTurnTextViewText()
        })

        gameViewModel.winnerLiveData.observe(this, Observer {
            if (it != null) {
                winner(it)
                gameViewModel.winnerLiveData.value = null
            }
        })

        gameViewModel.reDrawLiveData.observe(this, Observer {
            if (it == true) {
                reDraw()
                gameViewModel.reDrawLiveData.value = false
            }
        })

        gameViewModel.drawPartialMoveLiveData.observe(this, Observer { partialMove ->
            if (partialMove != null) {
                drawPartialMove(partialMove, gameViewModel.players.first { it.playerNumber == partialMove.madeTheMove})
                gameViewModel.drawPartialMoveLiveData.value = null
            }
        })
    }

    private fun setPlayerNameTexts() {
        player1_name!!.text = String.format("%s: %d", gameViewModel.players[0].playerName, gameViewModel.players[0].score)
        player2_name!!.text = String.format("%s: %d", gameViewModel.players[1].playerName, gameViewModel.players[1].score)
    }

    private fun getNonAIPlayer(): IPlayer {
        return if (gameViewModel.gameHandler.currentPlayersTurn.isAi) gameViewModel.gameHandler.getOpponent(gameViewModel.gameHandler.currentPlayersTurn)
        else gameViewModel.gameHandler.currentPlayersTurn
    }

    private fun nodeCoordsToNode(x: Float, y: Float): Node? {
        val coordsArray = game_view?.coordsToNode(x, y)
        return gameViewModel.gameHandler.gameBoard.findNodeByCoords(TwoDimensionalPoint(coordsArray!![0], coordsArray[1]))
    }

    private fun setPlayerTurnTextViewText() {
        player_turn?.text = String.format("%s %s%s", getString(R.string.game_partial_its), gameViewModel.gameHandler.currentPlayersTurn.playerName, getString(R.string.game_partial_turn))
        player_turn?.setTextColor(gameViewModel.gameHandler.currentPlayersTurn.playerColor)
    }

    private fun executeUpdateGameViewTask(gameViewDrawData: GameViewDrawData) {
        playBallSound(gameViewDrawData)
        game_view?.drawAsync(gameViewDrawData)
    }

    private fun playBallSound(gameViewDrawData: GameViewDrawData) {
        if (gameViewModel.gameHandler.isGameOver) return
        if (gameViewDrawData.ballNode.nodeType != NodeTypeEnum.Empty) {
            fxPlayer.playSound(R.raw.bounce)
        } else {
            fxPlayer.playSound(R.raw.soccerkick)
        }
    }

    private fun drawPartialMove(move: PartialMove, madeTheMove: IPlayer) {
        val newLineCoords = game_view?.nodeToCoords(move.newNode)
        val oldNodeCoords = game_view?.nodeToCoords(move.oldNode)

        val linesToDraw = LinesToDraw(TwoDimensionalPointF(oldNodeCoords?.get(0)!!, oldNodeCoords[1]), TwoDimensionalPointF(newLineCoords?.get(0)!!, newLineCoords[1]),
                madeTheMove.playerColor)
        gameViewModel.addDrawDataToQueue(linesToDraw, move.newNode, madeTheMove)
    }

    private fun winner(victory: Victory) {
        setPlayerNameTexts()

        when (victory.victoryConditionEnum) {
            VictoryConditionEnum.Goal -> {
                player_winner?.text = String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_scored_goal))
            }
            VictoryConditionEnum.OpponentOutOfMoves -> {
                player_winner?.text = String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_out_of_moves))
            }
            else -> {
                player_winner?.text = String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_timed_out))
            }
        }

        if (victory.winner.isAi && gameViewModel.isPlayerVsAi) {
            fxPlayer.playSound(R.raw.failure)
        } else {
            fxPlayer.playSound(R.raw.goodresult)
        }

        val anim = AlphaAnimation(0.5f, 0.0f)
        anim.duration = Integer.MAX_VALUE.toLong()
        anim.repeatMode = Animation.REVERSE
        game_view?.animation = anim

        player_winner.visibility = View.VISIBLE
        play_again_button.visibility = View.VISIBLE
        game_view?.isEnabled = false

        play_again_button.visibility = View.INVISIBLE
        recreate()
    }

    fun resetGame() {
        play_again_button.visibility = View.INVISIBLE
        recreate()
    }

    fun quit() {
        fxPlayer.cleanUpIfEnd()
        finish()
    }

    fun reDraw() {
        game_view?.invalidate()
    }

    companion object {

        const val ARG_PLAYER1 = "player1"
        const val ARG_PLAYER2 = "player2"

        const val ARG_GRID_SIZE_X = "grid_size_x"
        const val ARG_GRID_SIZE_Y = "grid_size_y"

        fun getWidth(context: Context): Int {
            val width: Int
            val display = context.display
            val size = Point()
            display?.getSize(size)
            width = size.x
            return width
        }

        fun getHeight(context: Context): Int {
            val height: Int
            val display = context.display
            val size = Point()
            display?.getSize(size)
            height = size.y
            return height
        }
    }
}
