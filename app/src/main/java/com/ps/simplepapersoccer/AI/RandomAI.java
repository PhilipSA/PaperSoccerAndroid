package com.ps.simplepapersoccer.AI;

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI;
import com.ps.simplepapersoccer.GameObjects.GameHandler;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;

public class RandomAI implements IGameAI {
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        PossibleMove randomMove = (PossibleMove)gameHandler.allPossibleMovesFromNode(gameHandler.ballNode).toArray()[0];
        return new PartialMove(randomMove.oldNode, randomMove.newNode, gameHandler.currentPlayersTurn);
    }
}
