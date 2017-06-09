package com.ps.simplepapersoccer.AI

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.Helpers.PathFindingHelper

class EuclideanAI : IGameAI {

    override fun MakeMove(gameHandler: GameHandler): PartialMove {
        var manhattanMove: PartialMove? = null
        var manhattanDistance = Integer.MAX_VALUE.toDouble()
        var tempManhattan = 0.0

        for (possibleMove in gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode())) {
            tempManhattan = PathFindingHelper.findPath(possibleMove.newNode, gameHandler.getOpponent(gameHandler.currentPlayersTurn)?.goal!!.goalNode()).size.toDouble()
            if (tempManhattan < manhattanDistance) {
                manhattanDistance = tempManhattan
                manhattanMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.currentPlayersTurn)
            }
        }
        return manhattanMove!!
    }
}
