package com.ps.simplepapersoccer.AI;

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI;
import com.ps.simplepapersoccer.GameObjects.GameHandler;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;
import com.ps.simplepapersoccer.Helpers.MathHelper;

public class EuclideanAI implements IGameAI {

    @Override
    public PartialMove MakeMove(GameHandler gameHandler) {
        PartialMove manhattanMove = null;
        double manhattanDistance = Integer.MAX_VALUE;
        double tempManhattan = 0;

        for (PossibleMove possibleMove : gameHandler.allPossibleMovesFromNode(gameHandler.ballNode))
        {
            tempManhattan = MathHelper.euclideanDistance(possibleMove.newNode.xCord, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goalNode.xCord,
                    possibleMove.newNode.yCord, gameHandler.getOpponent(gameHandler.currentPlayersTurn).goalNode.yCord);
            if (tempManhattan < manhattanDistance)
            {
                manhattanDistance = tempManhattan;
                manhattanMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.currentPlayersTurn);
            }
        }
        return manhattanMove;
    }
}
