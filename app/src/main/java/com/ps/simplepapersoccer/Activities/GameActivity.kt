package com.ps.simplepapersoccer.Activities

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
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
import android.widget.Button
import android.widget.TextView

import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.Enums.GameModeEnum
import com.ps.simplepapersoccer.Enums.VictoryConditionEnum
import com.ps.simplepapersoccer.GameObjects.Game.*
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Player
import com.ps.simplepapersoccer.GameObjects.PlayerActivityData
import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.Sound.FXPlayer
import java.util.*

class GameActivity : Activity() {

    var gameView: GameView? = null

    var drawCallQueue: Queue<ReDrawGameViewTask>? = ArrayDeque<ReDrawGameViewTask>();
    var drawTaskRunning: Boolean = false

    private var player1NameTextView: TextView? = null
    private var player2NameTextView: TextView? = null
    private var playerTurnTextView: TextView? = null

    private var player1ScoreTextView: TextView? = null
    private var player2ScoreTextView: TextView? = null

    private var playerWinnerTextView: TextView? = null

    private val player1Color = Color.BLUE
    private val player2Color = Color.RED

    private val screenHeight: Int = 0
    private val screenWidth: Int = 0

    var fxPlayer: FXPlayer? = null

    var gameHandler: GameHandler? = null

    var myName: String = ""

    var playerActivityDataMap: MutableMap<Player, PlayerActivityData> = HashMap()

    private var playAgain: Button? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(gameHandler?.player1?.playerName, gameHandler?.player1!!.score)
        outState.putInt(gameHandler?.player2?.playerName, gameHandler?.player2!!.score)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val difficulty = sharedPreferences.getString("pref_difficultyLevel", "Medium")
        val playerName = sharedPreferences.getString("pref_playerName", "Player")

        val gameMode = intent.getIntExtra("MULTIPLAYER_MODE", GameModeEnum.PLAYER_VS_AI)

        myName = playerName

        gameView = findViewById(R.id.gameview) as GameView

        player1NameTextView = findViewById(R.id.player1TextView) as TextView
        player2NameTextView = findViewById(R.id.player2TextView) as TextView
        playerTurnTextView = findViewById(R.id.playerTurnTextView) as TextView
        playerWinnerTextView = findViewById(R.id.playerWinnerTextview) as TextView

        player1ScoreTextView = findViewById(R.id.player1ScoreTextView) as TextView
        player2ScoreTextView = findViewById(R.id.player2ScoreTextView) as TextView

        player1NameTextView!!.setTextColor(player1Color)
        player2NameTextView!!.setTextColor(player2Color)

        var players = ArrayList<Player>()
        if (gameMode == GameModeEnum.PLAYER_VS_AI) {
            players = assignPlayerAndAi(difficulty, playerName)
        } else if (gameMode == GameModeEnum.MULTIPLAYER_MODE) {
            players.add(Player(playerName, 1, player1Color, false))
            players.add(Player("Player2", 2, player2Color, false))
        } else {
            players = assignTwoAi(difficulty, playerName)
        }

        player1NameTextView!!.text = players[0].playerName
        player2NameTextView!!.text = players[1].playerName

        if (savedInstanceState != null) {
            players[0].score = savedInstanceState.getInt(players[0].playerName)
            players[1].score = savedInstanceState.getInt(players[1].playerName)
        }

        val playerActivityData = PlayerActivityData(player1NameTextView as TextView, player1ScoreTextView as TextView)
        playerActivityDataMap.put(players[0], playerActivityData)
        val playerActivityData2 = PlayerActivityData(player2NameTextView as TextView, player2ScoreTextView as TextView)
        playerActivityDataMap.put(players[1], playerActivityData2)

        SetScoreText(players[0])
        SetScoreText(players[1])

        fxPlayer = FXPlayer(this)

        gameHandler = GameHandler(this, gameView!!.gridSizeX, gameView!!.gridSizeY, DifficultyEnum.valueOf(difficulty), players, gameMode)

        playAgain = findViewById(R.id.playagainButton) as Button

        gameHandler!!.UpdateGameState()

        gameView?.SetValues(GameActivity.getWidth(this), GameActivity.getHeight(this), gameView!!.gridSizeX, gameView!!.gridSizeY, this)
        gameView?.gameViewDrawData = GameViewDrawData(null, gameHandler?.currentPlayersTurn, gameHandler?.ballNode())

        gameView!!.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val touchedNode = nodeCoordsToNode(Math.round(motionEvent.x).toFloat(), Math.round(motionEvent.y).toFloat())
                if (touchedNode != null) {
                    gameHandler!!.PlayerMakeMove(touchedNode, gameHandler!!.currentPlayersTurn)
                }
            }
            false
        }

    }

    fun nodeCoordsToNode(x: Float, y: Float): Node? {
        val coordsArray = gameView?.coordsToNode(x, y)
        return gameHandler?.gameBoard?.findNodeByXY(coordsArray!![0].toInt(), coordsArray[1].toInt())
    }

    private fun assignTwoAi(difficulty: String, playerName: String): ArrayList<Player> {
        val players = ArrayList<Player>()
        val random = Random()
        if (random.nextBoolean()) {
            players.add(Player("Calculatos Maximus", 1, player1Color, true))
            players.add(Player("Ai" + difficulty, 2, player2Color, true))
        } else {
            players.add(Player("Ai" + difficulty, 1, player1Color, true))
            players.add(Player("Calculatos Maximus", 2, player2Color, true))
        }
        return players
    }

    private fun assignPlayerAndAi(difficulty: String, playerName: String): ArrayList<Player> {
        val players = ArrayList<Player>()
        val random = Random()
        if (random.nextBoolean()) {
            players.add(Player(playerName, 1, player1Color, false))
            players.add(Player("Ai" + difficulty, 2, player2Color, true))
        } else {
            players.add(Player("Ai" + difficulty, 1, player1Color, true))
            players.add(Player(playerName, 2, player2Color, false))
        }
        return players
    }

    private fun SetScoreText(player: Player) {
        playerActivityDataMap[player]?.playerScoreTextView?.text = String.format("%s: %d", player.playerName, player.score)
    }

    fun AddDrawDataToQueue(linesToDraw: LinesToDraw, ballNode: Node, playerTurn: Player ) {
        drawCallQueue?.add(ReDrawGameViewTask(gameView!!, GameViewDrawData(linesToDraw, playerTurn, ballNode), this))

        if (!drawTaskRunning) {
            drawTaskRunning = true
            drawCallQueue?.poll()?.execute()
        }
    }

    fun DrawPartialMove(move: PartialMove) {
        val newLineCoords = gameView?.nodeToCoords(move.newNode)
        val oldNodeCoords = gameView?.nodeToCoords(move.oldNode)

        val linesToDraw = LinesToDraw(oldNodeCoords?.get(0)!!, oldNodeCoords[1], newLineCoords?.get(0)!!, newLineCoords[1], move.madeTheMove.playerColor)
        AddDrawDataToQueue(linesToDraw, move.newNode, move.madeTheMove)
    }

    fun Winner(victory: Victory) {
        SetScoreText(victory.winner)

        if (victory.victoryConditionEnum == VictoryConditionEnum.Goal) {
            playerWinnerTextView!!.setText(String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_scored_goal)))
        } else {
            playerWinnerTextView!!.setText(String.format("%s %s", victory.winner.playerName, getString(R.string.game_victory_out_of_moves)))
        }

        if (victory.winner.isAi) {
            fxPlayer?.playSound(R.raw.failure)
        } else {
            fxPlayer?.playSound(R.raw.goodresult)
        }

        val anim = AlphaAnimation(0.5f, 0.0f)
        anim.duration = Integer.MAX_VALUE.toLong()
        anim.repeatMode = Animation.REVERSE
        gameView?.animation = anim

        playerWinnerTextView!!.visibility = View.VISIBLE
        playAgain!!.visibility = View.VISIBLE
        gameView?.isEnabled = false
    }

    fun ResetGame(view: View) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            playAgain!!.visibility = View.INVISIBLE
            recreate()
        }
    }

    fun Quit(view: View) {
        fxPlayer?.cleanUpIfEnd()
        finish()
    }

    fun reDraw() {
        gameView?.invalidate()
    }

    companion object {

        fun getWidth(mContext: Context): Int {
            var width = 0
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                val size = Point()
                display.getSize(size)
                width = size.x
            } else {
                width = display.width  // deprecated
            }
            return width
        }

        fun getHeight(mContext: Context): Int {
            var height = 0
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                val size = Point()
                display.getSize(size)
                height = size.y
            } else {
                height = display.height  // deprecated
            }
            return height
        }
    }
}
