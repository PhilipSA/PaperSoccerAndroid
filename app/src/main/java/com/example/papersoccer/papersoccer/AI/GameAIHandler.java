package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.Enums.DifficultyEnum;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;

public class GameAIHandler
{
	private DifficultyEnum difficulty;
	private final GameHandler gameHandler;
	private IGameAI gameAI;
	
	public GameAIHandler(GameHandler gameHandler, DifficultyEnum difficulty)
	{
		this.gameHandler = gameHandler;
		this.difficulty = difficulty;

		if (difficulty == DifficultyEnum.Easy)
		{
			gameAI = new RandomAI();
		}
		else if (difficulty == DifficultyEnum.Medium)
		{
			gameAI = new ManhattanAI();
		}
		else if (difficulty == DifficultyEnum.Hard)
		{
			gameAI = new MinimaxAI();
		}
	}

	public void MakeAIMove()
	{
		new MakeMoveAITask(gameAI, gameHandler).execute();
	}
}
