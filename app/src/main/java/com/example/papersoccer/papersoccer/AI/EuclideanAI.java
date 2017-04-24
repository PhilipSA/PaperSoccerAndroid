package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Move.PossibleMove;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;

public class EuclideanAI implements IGameAI {

    @Override
    public PartialMove MakeMove(GameHandler gameHandler) {
        PartialMove manhattanMove = null;
        double manhattanDistance = Integer.MAX_VALUE;
        double tempManhattan = 0;

        for (PossibleMove possibleMove : gameHandler.allPossibleMovesFromNode(gameHandler.ballNode))
        {
            tempManhattan = MathHelper.euclideanDistance(possibleMove.newNode.xCord, 5, possibleMove.newNode.yCord, 10);
            if (tempManhattan < manhattanDistance)
            {
                manhattanDistance = tempManhattan;
                manhattanMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, gameHandler.currentPlayersTurn);
            }
        }
        return manhattanMove;
    }
}
