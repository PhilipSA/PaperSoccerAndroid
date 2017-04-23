package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.Move;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;

import java.util.Random;

public class RandomAI implements IGameAI {
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        Random random = new Random();
        PartialMove randomMove = (PartialMove)gameHandler.allAvailiblePartialMovesFromNode(gameHandler.ballNode).toArray()[random.nextInt(gameHandler.allAvailiblePartialMovesFromNode(gameHandler.ballNode).size())];
        return randomMove;
    }
}
