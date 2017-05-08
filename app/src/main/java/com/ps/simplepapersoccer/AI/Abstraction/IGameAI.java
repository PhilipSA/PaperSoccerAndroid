package com.ps.simplepapersoccer.AI.Abstraction;

import com.ps.simplepapersoccer.GameObjects.Game.GameHandler;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;

public interface IGameAI
{
    public PartialMove MakeMove(GameHandler gameHandler);
}
