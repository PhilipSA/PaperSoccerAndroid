package com.ps.simplepapersoccer.activities

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.enums.NodeTypeEnum
import com.ps.simplepapersoccer.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.Game.GameHandler
import com.ps.simplepapersoccer.gameObjects.Game.GameViewDrawData
import com.ps.simplepapersoccer.gameObjects.Game.Geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.gameObjects.Game.Victory
import com.ps.simplepapersoccer.gameObjects.Move.PartialMove
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer
import com.ps.simplepapersoccer.gameObjects.Player.PlayerActivityData
import com.ps.simplepapersoccer.sound.FXPlayer
import com.ps.simplepapersoccer.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*

class GameActivity : AppCompatActivity() {

    var gameView: GameView? = null

    private var fxPlayer: FXPlayer? = null
    private var myName: String = ""
    private var playerActivityDataMap: MutableMap<IPlayer, PlayerActivityData> = HashMap()

    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game)

        gameViewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        gameViewModel.difficulty = sharedPreferences.getString("pref_difficultyLevel", "Medium")
        val playerName = sharedPreferences.getString("pref_playerName", "Player")

        val gridSizeX = sharedPreferences.getString("gridsize_x", "8").toInt()
        val gridSizeY = sharedPreferences.getString("gridsize_y", "10").toInt()

        val gameMode = intent.getIntExtra("MULTIPLAYER_MODE", GameModeEnum.PLAYER_VS_AI)

        myName = playerName

        gameView = findViewById(R.id.gameview)

        player1_name!!.setTextColor(gameViewModel.player1Color)
        player2_name!!.setTextColor(gameViewModel.player2Color)

        gameViewModel.setGameMode(gameMode, playerName)

        player1_name!!.text = gameViewModel.players[0].playerName
        player2_name!!.text = gameViewModel.players[1].playerName

        val playerActivityData = PlayerActivityData(player1_name as TextView, player1_score as TextView)
        playerActivityDataMap[gameViewModel.players[0]] = playerActivityData
        val playerActivityData2 = PlayerActivityData(player2_name as TextView, player2_score as TextView)
        playerActivityDataMap[gameViewModel.players[1]] = playerActivityData2

        setScoreText(gameViewModel.players[0])
        setScoreText(gameViewModel.players[1])

        fxPlayer = FXPlayer(this)

        gameViewModel.gameHandler = GameHandler(this, gridSizeX, gridSizeY, gameViewModel.players, gameMode, true, gameView != null)

        setPlayerTurnTextViewText()

        gameView?.init(GameActivity.getWidth(this), GameActivity.getHeight(this), gridSizeX, gridSizeY, gameViewModel, gameViewModel.gameHandler.gameBoard)
        gameView?.gameViewDrawData = GameViewDrawData(null, gameViewModel.gameHandler.currentPlayersTurn, gameViewModel.gameHandler.currentPlayersTurn,
                gameViewModel.gameHandler.ballNode(),
                gameViewModel.getAllNodeNeighbors(gameViewModel.gameHandler.ballNode()))

        if (gameMode != GameModeEnum.AI_VS_AI) {
            gameView?.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val touchedNode = nodeCoordsToNode(Math.round(motionEvent.x).toFloat(), Math.round(motionEvent.y).toFloat())
                    if (touchedNode != null) {
                        gameViewModel.gameHandler?.PlayerMakeMove(touchedNode, getNonAIPlayer())
                    }
                }
                false
            }
        }

        registerObservers()

        gameViewModel.gameHandler.UpdateGameState()
    }

    private fun registerObservers() {
        gameViewModel.executeUpdateGameViewTaskLiveData.observe(this, androidx.lifecycle.Observer {
            executeUpdateGameViewTask(it)
        })

        gameViewModel.playerTurnTextLiveData.observe(this, androidx.lifecycle.Observer {
            setPlayerTurnTextViewText()
        })
    }

    private fun getNonAIPlayer(): IPlayer {
        return if (gameViewModel.gameHandler.currentPlayersTurn.isAi) gameViewModel.gameHandler.getOpponent(gameViewModel.gameHandler.currentPlayersTurn)!!
        else gameViewModel.gameHandler.currentPlayersTurn
    }

    private fun nodeCoordsToNode(x: Float, y: Float): Node? {
        val coordsArray = gameView?.coordsToNode(x, y)
        return gameViewModel.gameHandler.gameBoard.findNodeByCoords(Point(coordsArray!![0].toInt(), coordsArray[1].toInt()))
    }

    private fun setPlayerTurnTextViewText() {
        player_turn?.text = String.format("%s %s%s", getString(R.string.game_partial_its), gameViewModel.gameHandler.currentPlayersTurn.playerName, getString(R.string.game_partial_turn))
        player_turn?.setTextColor(gameViewModel.gameHandler.currentPlayersTurn.playerColor)
    }

    private fun setScoreText(player: IPlayer) {
        playerActivityDataMap[player]?.playerScoreTextView?.text = String.format("%s: %d", player.playerName, player.score)
    }

    private fun executeUpdateGameViewTask(gameViewDrawData: GameViewDrawData) {
        playBallSound(gameViewDrawData)
        gameView?.drawAsync(gameViewDrawData)
        Handler().post {
            gameViewModel.gameHandler.UpdateGameState()
        }
    }

    private fun playBallSound(gameViewDrawData: GameViewDrawData) {
        if (gameViewModel.gameHandler.isGameOver) return
        if (gameViewDrawData.ballNode?.nodeType != NodeTypeEnum.Empty) {
            fxPlayer?.playSound(R.raw.bounce)
        } else {
            fxPlayer?.playSound(R.raw.soccerkick)
        }
    }

    fun drawPartialMove(move: PartialMove) {
        val newLineCoords = gameView?.nodeToCoords(move.newNode)
        val oldNodeCoords = gameView?.nodeToCoords(move.oldNode)

        val linesToDraw = LinesToDraw(PointF(oldNodeCoords?.get(0)!!, oldNodeCoords[1]), PointF(newLineCoords?.get(0)!!, newLineCoords[1]), move.madeTheMove.playerColor)
        gameViewModel.addDrawDataToQueue(linesToDraw, move.newNode, move.madeTheMove)
    }

    fun winner(victory: Victory) {
        setScoreText(victory.winner)

        if (victory.victoryConditionEnum == VictoryConditionEnum.Goal) {
            player_winner?.text = String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_scored_goal))
        } else {
            player_winner?.text = String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_out_of_moves))
        }

        if (victory.winner.isAi && gameViewModel.gameHandler.gameMode == GameModeEnum.PLAYER_VS_AI) {
            fxPlayer?.playSound(R.raw.failure)
        } else {
            fxPlayer?.playSound(R.raw.goodresult)
        }

        val anim = AlphaAnimation(0.5f, 0.0f)
        anim.duration = Integer.MAX_VALUE.toLong()
        anim.repeatMode = Animation.REVERSE
        gameView?.animation = anim

        player_winner.visibility = View.VISIBLE
        play_again_button.visibility = View.VISIBLE
        gameView?.isEnabled = false
    }

    fun resetGame(view: View) {
        play_again_button.visibility = View.INVISIBLE
        recreate()
    }

    fun quit(view: View) {
        fxPlayer?.cleanUpIfEnd()
        finish()
    }

    fun reDraw() {
        gameView?.invalidate()
    }

    companion object {

        fun getWidth(mContext: Context): Int {
            val width: Int
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            width = size.x
            return width
        }

        fun getHeight(mContext: Context): Int {
            val height: Int
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            height = size.y
            return height
        }
    }
}
