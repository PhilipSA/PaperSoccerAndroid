package com.example.papersoccer.papersoccer.GameObjects;

import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Node
{
	public UUID id;
	public int xCord;
	public int yCord;
	public NodeTypeEnum nodeType;
	public HashSet<Node> neighbors = new HashSet<>();
	
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
