package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.ai.GameAIHandler
import com.ps.simplepapersoccer.ai.abstraction.IGameAiHandlerListener
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer
import kotlinx.coroutines.CoroutineDispatcher

class GameHandler(private val listener: IGameHandlerListener?, gridX: Int, gridY: Int, players: ArrayList<IPlayer>, private val dispatcher: CoroutineDispatcher): IGameAiHandlerListener {

    private val player1: IPlayer = players[0]
    private val player2: IPlayer = players[1]

    var currentPlayersTurn: IPlayer; private set

    var numberOfTurns = 0
    val gameBoard: GameBoard
    private var ongoingTurn = false

    val ballNode: Node get() = gameBoard.ballNode

    override fun hashCode(): Int {
        return currentPlayersTurn.hashCode() xor gameBoard.hashCode()
    }

    init {
        currentPlayersTurn = players[0]

        gameBoard = GameBoard(gridX, gridY)
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
            GameAIHandler(this, dispatcher).makeAIMove(currentPlayersTurn as AIPlayer, this)
        }
    }

    override fun aiMove(partialMove: PartialMove, timedOut: Boolean) {
        if (timedOut) {
            winner(Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.TimedOut))
        } else {
            aiMakeMove(partialMove)
        }
    }

    fun playerMakeMove(node: Node, player: IPlayer) {
        val partialMove = PartialMove(ballNode, node, player)
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
        makePartialMove(partialMove)
        listener?.drawPartialMoveLiveData?.value = partialMove
        ++numberOfTurns
        updateGameState()
    }

    fun undoLastMove() {
        currentPlayersTurn = gameBoard.undoLastMove().madeTheMove
    }

    fun makePartialMove(partialMove: PartialMove) {
        gameBoard.makePartialMove(partialMove)

        if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
            changeTurn()
        }
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

    private fun changeTurn(): IPlayer {
        currentPlayersTurn = getOpponent(currentPlayersTurn)
        return currentPlayersTurn
    }

    private fun isPartialMoveLegal(partialMove: PartialMove, player: IPlayer): Boolean {
        return gameBoard.allPossibleMovesFromNode(ballNode).any { x -> x.newNode == partialMove.newNode }
                && player == currentPlayersTurn
    }

    companion object {
        private val TAG = GameHandler::class.java.canonicalName
    }
}
