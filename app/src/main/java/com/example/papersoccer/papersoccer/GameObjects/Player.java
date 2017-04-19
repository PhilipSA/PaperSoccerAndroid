package com.example.papersoccer.papersoccer.GameObjects;

public class Player {

	public String playerName;
	public int playerNumber;
	public int playerColor;
	public Node goalNode;
	
	public Player(String name, int number, int color)
	{
		this.playerName = name;
		this.playerNumber = number;
		this.playerColor = color;
	}
}
