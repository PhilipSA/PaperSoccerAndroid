package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;

import java.util.Random;

public class RandomAI implements IGameAI {
    public Move MakeMove(GameHandler gameHandler)
    {
        Random random = new Random();
        Move randomMove = (Move)gameHandler.allAvailibleMoves().toArray()[random.nextInt(gameHandler.allAvailibleMoves().size())];
        return randomMove;
    }
}
