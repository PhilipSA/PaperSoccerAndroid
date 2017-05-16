package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.AI.GameAIHandler
import com.ps.simplepapersoccer.Activities.GameActivity
import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.Enums.GameModeEnum
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.Enums.VictoryConditionEnum
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.GameObjects.Player.AIPlayer
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer
import com.ps.simplepapersoccer.GameObjects.Player.Player
import com.ps.simplepapersoccer.R

class GameHandler(private val gameActivity: GameActivity?, gridX: Int, gridY: Int, players: ArrayList<IPlayer>, private val gameMode: Int, aiIsAsync: Boolean) {
    var player1: IPlayer = players[0]
    var player2: IPlayer = players[1]

    var currentPlayersTurn: IPlayer

    var numberOfTurns = 0
    private val gameAIHandler: GameAIHandler
    val gameBoard: GameBoard
    var ongoingTurn = false

    fun ballNode(): Node {
        return gameBoard.ballNode as Node
    }

    override fun hashCode(): Int {
        return currentPlayersTurn.hashCode() xor gameBoard.hashCode()
    }

    init {

        currentPlayersTurn = players[0]

        gameBoard = GameBoard(gridX, gridY)
        player1.goalNode = gameBoard.goalNode1
        player2.goalNode = gameBoard.goalNode2
        gameActivity?.reDraw()

        gameAIHandler = GameAIHandler(this, aiIsAsync)
    }

    fun allPossibleMovesFromNode(node: Node): HashSet<PossibleMove> {
        return gameBoard.allPossibleMovesFromNode(node)
    }

    fun UpdateGameState() {
        ongoingTurn = false
        if (isGameOver) {
            winner(getWinner(ballNode())!!)
            return
        }
        if (currentPlayersTurn.isAi && gameMode != GameModeEnum.MULTIPLAYER_MODE) {
            gameAIHandler.MakeAIMove(currentPlayersTurn as AIPlayer)
        }
    }

    fun PlayerMakeMove(node: Node, player: IPlayer) {
        val partialMove = PartialMove(ballNode(), node, player)
        if (isPartialMoveLegal(partialMove, player) && currentPlayersTurn == player && !ongoingTurn) {
            ongoingTurn = true
            MakeMove(partialMove)
            UpdateGameState()
        }
    }

    fun AIMakeMove(move: PartialMove) {
        ongoingTurn = true
        MakeMove(move)
        UpdateGameState()
    }

    fun MakeMove(partialMove: PartialMove) {
        MakePartialMove(partialMove)

        gameActivity?.DrawPartialMove(partialMove)
        ++numberOfTurns
    }

    fun UndoLastMove() {
        currentPlayersTurn = gameBoard.UndoLastMove().madeTheMove
    }

    fun MakePartialMove(partialMove: PartialMove) {
        gameBoard.MakePartialMove(partialMove)

        if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
            changeTurn()
        }
    }

    val isGameOver: Boolean
        get() {
            if (getWinner(gameBoard.ballNode!!) != null) {
                return true
            }
            return false
        }

    fun getWinner(node: Node): Victory? {
        if (node.nodeType == NodeTypeEnum.Goal) {
            if (node.id === currentPlayersTurn.goalNode?.id) {
                return Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.Goal)
            }
            return Victory(currentPlayersTurn, VictoryConditionEnum.Goal)
        } else if (node.neighbors.size == 0) {
            return Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.OpponentOutOfMoves)
        }
        return null
    }

    //Let the activity know we have a winner
    fun winner(victory: Victory) {
        victory.winner.score += 1
        gameActivity?.Winner(victory)
    }

    fun getOpponent(myPlayer: IPlayer): IPlayer {
        if (myPlayer === player1) return player2
        return player1
    }

    fun changeTurn(): IPlayer {
        currentPlayersTurn = getOpponent(currentPlayersTurn)
        return currentPlayersTurn
    }

    fun isPartialMoveLegal(partialMove: PartialMove, player: IPlayer): Boolean {
        return allPossibleMovesFromNode(ballNode()).contains(PossibleMove(partialMove.oldNode, partialMove.newNode)) && player === currentPlayersTurn
    }
}
