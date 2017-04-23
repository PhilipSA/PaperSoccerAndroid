package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.Move;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;

public class EuclideanAI implements IGameAI {

    @Override
    public PartialMove MakeMove(GameHandler gameHandler) {
        PartialMove manhattanMove = null;
        double manhattanDistance = Integer.MAX_VALUE;
        double tempManhattan = 0;

        for (PartialMove partialMove : gameHandler.allAvailiblePartialMovesFromNode(gameHandler.ballNode))
        {
            tempManhattan = MathHelper.euclideanDistance(partialMove.newNode.xCord, 5, partialMove.newNode.yCord, 10);
            if (tempManhattan < manhattanDistance)
            {
                manhattanDistance = tempManhattan;
                partialMove.madeTheMove = gameHandler.currentPlayersTurn;
                manhattanMove = partialMove;
            }
        }
        return manhattanMove;
    }
}
