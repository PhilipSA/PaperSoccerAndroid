package com.ps.simplepapersoccer.ai.euclideanAI

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.gameObjects.player.AIPlayer
import com.ps.simplepapersoccer.helpers.PathFindingHelper

//Dumb AI to use as test opponent
class EuclideanAI(playerNumber: Int,
                  playerColor: Int,
                  playerName: String = EuclideanAI::class.java.simpleName) : IGameAI, AIPlayer(playerName, playerNumber, playerColor) {

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        var manhattanMove: PartialMove? = null
        var manhattanDistance = Integer.MAX_VALUE.toDouble()
        var tempManhattan: Double

        for (possibleMove in gameHandler.gameBoard.allLegalMovesFromBallNode) {
            tempManhattan = PathFindingHelper.findPathGreedyBestFirstSearchBiDirectional(possibleMove.newNode, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goal!!.goalNode()).size.toDouble()
            if (tempManhattan < manhattanDistance) {
                manhattanDistance = tempManhattan
                manhattanMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.gameBoard.currentPlayersTurn)
            }
        }
        return manhattanMove!!
    }
}
