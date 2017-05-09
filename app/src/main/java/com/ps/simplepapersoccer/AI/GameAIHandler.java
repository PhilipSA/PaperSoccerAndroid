package com.ps.simplepapersoccer.AI;

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI;
import com.ps.simplepapersoccer.AI.MinimaxAI.MinimaxAI;
import com.ps.simplepapersoccer.Enums.DifficultyEnum;
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler;

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
			gameAI = new EuclideanAI();
		}
		else if (difficulty == DifficultyEnum.Medium)
		{
			gameAI = new MinimaxAI(1200);
		}
		else if (difficulty == DifficultyEnum.Hard)
		{
			gameAI = new MinimaxAI(1500);
		}
		else if (difficulty == DifficultyEnum.VeryHard)
		{
			gameAI = new MinimaxAI(3000);
		}
	}

	public void MakeAIMove()
	{
		new MakeMoveAITask(gameAI, gameHandler).execute();
	}
}
