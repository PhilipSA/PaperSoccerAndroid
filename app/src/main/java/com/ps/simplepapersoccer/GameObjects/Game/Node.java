package com.ps.simplepapersoccer.GameObjects.Game;

import com.ps.simplepapersoccer.Enums.NodeTypeEnum;

import java.util.HashSet;
import java.util.UUID;

public class Node
{
	public UUID id;
	public int xCord;
	public int yCord;
	public NodeTypeEnum nodeType;
	public HashSet<UUID> neighbors = new HashSet<>();

	@Override
	public int hashCode() {
		return id.hashCode() ^ nodeType.hashCode() ^ neighbors.hashCode();
	}

	public void RemoveNeighborPair(Node other)
	{
		neighbors.remove(other.id);
		other.neighbors.remove(this.id);
	}

	public void AddNeighborPair(Node other)
	{
		neighbors.add(other.id);
		other.neighbors.add(this.id);
	}
	
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
		this.neighbors = node.neighbors;
	}
}
