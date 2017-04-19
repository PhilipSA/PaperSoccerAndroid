package com.example.papersoccer.papersoccer.AI.Abstraction;

import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Node;

public interface IGameAI
{
    public Node MakeMove(GameHandler gameHandler);
}
