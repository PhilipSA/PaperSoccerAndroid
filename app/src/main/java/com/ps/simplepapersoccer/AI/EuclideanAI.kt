package com.ps.simplepapersoccer.AI

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.Helpers.MathHelper

class EuclideanAI : IGameAI {

    override fun MakeMove(gameHandler: GameHandler): PartialMove {
        var manhattanMove: PartialMove? = null
        var manhattanDistance = Integer.MAX_VALUE.toDouble()
        var tempManhattan = 0.0

        for (possibleMove in gameHandler.gameBoard.allPossibleMovesFromNode(gameHandler.ballNode())) {
            tempManhattan = MathHelper.euclideanDistance(possibleMove.newNode.coords, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goalNode!!.coords)
            if (tempManhattan < manhattanDistance) {
                manhattanDistance = tempManhattan
                manhattanMove = PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.currentPlayersTurn)
            }
        }
        return manhattanMove!!
    }
}
