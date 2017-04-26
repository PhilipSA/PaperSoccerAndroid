package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Move.PossibleMove;

import java.util.Random;

public class RandomAI implements IGameAI {
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        PossibleMove randomMove = (PossibleMove)gameHandler.allPossibleMovesFromNode(gameHandler.ballNode).toArray()[0];
        return new PartialMove(randomMove.oldNode, randomMove.newNode, gameHandler.currentPlayersTurn);
    }
}
