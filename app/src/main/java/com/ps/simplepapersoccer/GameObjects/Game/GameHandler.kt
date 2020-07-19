package com.ps.simplepapersoccer.gameObjects.game

import android.os.Handler
import com.ps.simplepapersoccer.ai.GameAIHandler
import com.ps.simplepapersoccer.ai.abstraction.IGameAiHandlerListener
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import kotlinx.coroutines.CoroutineDispatcher

class GameHandler(private val listener: IGameHandlerListener?, gridX: Int, gridY: Int,
                  private val players: ArrayList<IPlayer>,
                  private val dispatcher: CoroutineDispatcher, private val handler: Handler = Handler()): IGameAiHandlerListener {

    private val player1: IPlayer = players[0]
    private val player2: IPlayer = players[1]

    val currentPlayersTurn: IPlayer get() = players.first { it.playerNumber == gameBoard.currentPlayersTurn }

    var winner: Victory? = null

    var numberOfTurns = 0
    val gameBoard = GameBoard(gridX * 2, gridY * 2)
    private var ongoingTurn = false

    val ballNode: Node get() = gameBoard.ballNode

    override fun hashCode(): Int {
        return currentPlayersTurn.hashCode() xor gameBoard.hashCode()
    }

    init {
        gameBoard.currentPlayersTurn = players[0].playerNumber
        player1.goal = gameBoard.goal1
        player2.goal = gameBoard.goal2
        listener?.reDrawLiveData?.value = true
    }

    fun updateGameState() {
        ongoingTurn = false
        if (isGameOver) {
            winner(getWinner(ballNode)!!)
            return
        }
        if (currentPlayersTurn.isAi) {
            GameAIHandler(this, dispatcher, handler).makeAIMove(currentPlayersTurn as AIPlayer, this)
        }
    }

    override fun aiMove(partialMove: PartialMove?, timedOut: Boolean) {
        if (timedOut || partialMove == null || isPartialMoveLegal(partialMove, currentPlayersTurn).not()) {
            winner(Victory(getOpponent(currentPlayersTurn), if (partialMove == null) VictoryConditionEnum.IllegalMove else VictoryConditionEnum.TimedOut))
        } else {
            aiMakeMove(partialMove)
        }
    }

    fun playerMakeMove(node: Node, player: IPlayer) {
        val partialMove = PartialMove(ballNode, node, player.playerNumber)
        if (isPartialMoveLegal(partialMove, player) && currentPlayersTurn == player && !ongoingTurn) {
            ongoingTurn = true
            makeMove(partialMove)
        }
    }

    fun aiMakeMove(move: PartialMove) {
        ongoingTurn = true
        makeMove(move)
    }

    private fun makeMove(partialMove: PartialMove) {
        gameBoard.makePartialMove(partialMove)
        listener?.drawPartialMoveLiveData?.value = partialMove
        ++numberOfTurns
        updateGameState()
    }

    val isGameOver: Boolean
        get() {
            if (getWinner(gameBoard.ballNode) != null) {
                return true
            }
            return false
        }

    fun getWinner(node: Node): Victory? {
        if (node.nodeType == NodeTypeEnum.Goal) {
            if (player1.goal!!.isGoalNode(node)) return Victory(player2, VictoryConditionEnum.Goal)
            else if (player2.goal!!.isGoalNode(node)) return Victory(player1, VictoryConditionEnum.Goal)
        } else if (node.neighbors.size == 0) {
            return Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.OpponentOutOfMoves)
        }
        return null
    }

    private fun winner(victory: Victory) {
        this.winner = victory
        victory.winner.score += 1
        listener?.winnerLiveData?.value = victory
    }

    fun getOpponent(myPlayer: IPlayer): IPlayer {
        return when (myPlayer) {
            player1 -> player2
            player2 -> player1
            else -> player2
        }
    }

    fun getPlayerPosition(player: IPlayer): Int {
        return players.indexOf(player)
    }

    private fun isPartialMoveLegal(partialMove: PartialMove, player: IPlayer): Boolean {
        return gameBoard.allPossibleMovesFromNode(ballNode).any { x -> x.newNode == partialMove.newNode }
                && player == currentPlayersTurn
    }

    companion object {
        private val TAG = GameHandler::class.java.canonicalName
    }
}
