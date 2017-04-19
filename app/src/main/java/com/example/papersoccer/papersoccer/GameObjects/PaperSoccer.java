package com.example.papersoccer.papersoccer.GameObjects;

import android.app.Application;
import android.graphics.Color;

import com.example.papersoccer.papersoccer.Enums.DifficultyEnum;

public class PaperSoccer extends Application {

	public Player player = new Player("Default", 1, Color.BLUE);
	public Player opponent = new Player("AI", 2, Color.RED);
	public DifficultyEnum AiDifficulty = DifficultyEnum.Medium;
	
	public void SetAiDifficulty(DifficultyEnum difficulty)
	{
		AiDifficulty = difficulty;
	}
	
	public String GetAiName()
	{
		if (AiDifficulty == DifficultyEnum.Easy)
		{
			opponent.playerName = "AI-Easy";
		}
		
		if (AiDifficulty == DifficultyEnum.Medium)
		{
			opponent.playerName = "AI-Medium";
		}

		if (AiDifficulty == DifficultyEnum.Hard)
		{
			opponent.playerName = "AI-Hard";
		}
		return opponent.playerName;
	}
}
