package com.ps.simplepapersoccer.GameObjects;

public class Player {
	public String playerName;
	public int playerNumber;
	public int playerColor;
	public Node goalNode;
	public int score;
	public boolean isAi;
	
	public Player(String name, int number, int color, boolean isAi)
	{
		this.playerName = name;
		this.playerNumber = number;
		this.playerColor = color;
		this.isAi = isAi;
	}
}
