package com.ps.simplepapersoccer.ai.euclideanAI

import com.ps.simplepapersoccer.ai.abstraction.IGameAI
import com.ps.simplepapersoccer.gameObjects.game.GameHandler
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import com.ps.simplepapersoccer.helpers.PathFindingHelper

//Dumb AI to use as test opponent
class EuclideanAI : IGameAI {

    override suspend fun makeMove(gameHandler: GameHandler): PartialMove {
        var manhattanMove: PartialMove? = null
        var manhattanDistance = Integer.MAX_VALUE.toDouble()
        var tempManhattan: Double

        for (possibleMove in gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode)) {
            tempManhattan = PathFindingHelper.findPath(possibleMove.newNode, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goal!!.goalNode()).size.toDouble()
            if (tempManhattan < manhattanDistance) {
                manhattanDistance = tempManhattan
                manhattanMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.gameBoard.currentPlayersTurn)
            }
        }
        return manhattanMove!!
    }
}
