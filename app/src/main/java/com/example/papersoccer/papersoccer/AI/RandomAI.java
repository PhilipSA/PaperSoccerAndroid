package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Node;

import java.util.Random;

public class RandomAI implements IGameAI {
    public Node MakeMove(GameHandler gameHandler)
    {
        Random random = new Random();
        Node randomNode = gameHandler.ballNode.neighbors.get(random.nextInt(gameHandler.ballNode.neighbors.size()));
        return randomNode;
    }
}
