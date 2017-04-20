package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Node;

public class ManhattanAI implements IGameAI {

    @Override
    public Node MakeMove(GameHandler gameHandler) {
        Node manhattanNode = null;
        int manhattanDistance = Integer.MAX_VALUE;
        int tempManhattan = 0;

        for (Node n : gameHandler.allAvailibleMoves())
        {
            tempManhattan = Math.abs(n.xCord - 5) + Math.abs(n.yCord - 12);
            if (tempManhattan < manhattanDistance)
            {
                manhattanDistance = tempManhattan;
                manhattanNode = n;
            }
        }
        return manhattanNode;
    }
}
