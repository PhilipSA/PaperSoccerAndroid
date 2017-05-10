package com.ps.simplepapersoccer.GameObjects.Game

import java.util.ArrayList
import java.util.HashSet

import com.ps.simplepapersoccer.AI.GameAIHandler
import com.ps.simplepapersoccer.Activities.GameActivity
import com.ps.simplepapersoccer.Enums.DifficultyEnum
import com.ps.simplepapersoccer.Enums.GameModeEnum
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.Enums.VictoryConditionEnum
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.GameObjects.Player
import com.ps.simplepapersoccer.R

class GameHandler(private val gameActivity: GameActivity, gridX: Int, gridY: Int, difficulty: DifficultyEnum, players: ArrayList<Player>, private val gameMode: Int) {
    var player1: Player
    var player2: Player

    var currentPlayersTurn: Player

    var numberOfTurns = 0
    private val gameAIHandler: GameAIHandler
    val gameBoard: GameBoard
    var aiTurn = false

    fun ballNode(): Node {
        return gameBoard.ballNode as Node
    }

    override fun hashCode(): Int {
        return currentPlayersTurn.hashCode() xor gameBoard.hashCode()
    }

    init {
        this.player1 = players[0]
        this.player2 = players[1]

        currentPlayersTurn = players[0]

        gameBoard = GameBoard(gridX, gridY)
        player1.goalNode = gameBoard.goalNode1
        player2.goalNode = gameBoard.goalNode2
        gameActivity.reDraw()

        gameAIHandler = GameAIHandler(this, difficulty)
    }

    fun allPossibleMovesFromNode(node: Node): HashSet<PossibleMove> {
        return gameBoard.allPossibleMovesFromNode(node)
    }

    fun UpdateGameState() {
        gameActivity.UpdateDrawData()

        if (isGameOver) {
            winner(getWinner(ballNode())!!)
            return
        }
        if (currentPlayersTurn.isAi && gameMode != GameModeEnum.MULTIPLAYER_MODE) {
            aiTurn = true
            gameAIHandler.MakeAIMove()
        }
    }

    fun PlayerMakeMove(node: Node, player: Player) {
        val partialMove = PartialMove(ballNode(), node, player)
        if (isPartialMoveLegal(partialMove, player) && currentPlayersTurn == player) {
            MakeMove(partialMove)
            UpdateGameState()
        }
    }

    fun AIMakeMove(move: PartialMove) {
        MakeMove(move)
        aiTurn = false
        UpdateGameState()
    }

    fun MakeMove(partialMove: PartialMove) {
        MakePartialMove(partialMove)

        if (ballNode().nodeType != NodeTypeEnum.Empty) {
            gameActivity.fxPlayer?.playSound(R.raw.bounce)
        } else {
            gameActivity.fxPlayer?.playSound(R.raw.soccerkick)
        }

        gameActivity.DrawPartialMove(partialMove, partialMove.madeTheMove.playerColor)
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
        gameActivity.Winner(victory)
    }

    fun getOpponent(myPlayer: Player): Player {
        if (myPlayer === player1) return player2
        return player1
    }

    fun changeTurn(): Player {
        currentPlayersTurn = getOpponent(currentPlayersTurn)
        return currentPlayersTurn
    }

    fun isPartialMoveLegal(partialMove: PartialMove, player: Player): Boolean {
        val test = allPossibleMovesFromNode(ballNode())
        return test.contains(PossibleMove(partialMove.oldNode, partialMove.newNode)) && player === currentPlayersTurn
    }
}
