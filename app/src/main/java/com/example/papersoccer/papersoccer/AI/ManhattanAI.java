package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;

public class ManhattanAI implements IGameAI {

    @Override
    public Move MakeMove(GameHandler gameHandler) {
        Move manhattanMove = null;
        double manhattanDistance = Integer.MAX_VALUE;
        double tempManhattan = 0;

        for (Move move : gameHandler.allAvailibleMoves())
        {
            tempManhattan = MathHelper.euclideanDistance(move.newNode.xCord, 5, move.newNode.yCord, 10);
            if (tempManhattan < manhattanDistance)
            {
                manhattanDistance = tempManhattan;
                manhattanMove = move;
            }
        }
        return manhattanMove;
    }
}
