package com.example.papersoccer.papersoccer.AI.Abstraction;

import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.Move;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;

public interface IGameAI
{
    public PartialMove MakeMove(GameHandler gameHandler);
}
