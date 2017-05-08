package com.ps.simplepapersoccer.GameObjects.Game;

public class LinesToDraw
{
	public LinesToDraw(float fromX, float fromY, float toX, float toY, int color)
	{
		this.fromX = fromX;
		this.fromY = fromY;
		
		this.toX = toX;
		this.toY = toY;
		
		this.color = color;
	}
	public float fromX;
	public float fromY;
	
	public float toX;
	public float toY;
	
	public int color;
}