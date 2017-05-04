package com.ps.simplepapersoccer.GameObjects;

import com.ps.simplepapersoccer.Enums.NodeTypeEnum;

import java.util.UUID;

public class Node
{
	public UUID id;
	public int xCord;
	public int yCord;
	public NodeTypeEnum nodeType;
	
	Node(int x, int y, NodeTypeEnum nodeType)
	{
		id = UUID.randomUUID();
		this.xCord = x;
		this.yCord = y;
		this.nodeType = nodeType;
	}

	Node(Node node)
	{
		this.id = node.id;
		this.xCord = node.xCord;
		this.yCord = node.yCord;
		this.nodeType = node.nodeType;
	}
}
