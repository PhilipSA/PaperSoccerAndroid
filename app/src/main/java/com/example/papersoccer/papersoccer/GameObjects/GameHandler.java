package com.example.papersoccer.papersoccer.GameObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.example.papersoccer.papersoccer.AI.GameAIHandler;
import com.example.papersoccer.papersoccer.Activites.GameActivity;
import com.example.papersoccer.papersoccer.Enums.DifficultyEnum;
import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;

public class GameHandler implements Cloneable {

	public List<Node> nodeList = new ArrayList<Node>();
	public List<Player> players = new ArrayList<Player>();
	
	private boolean isMyTurn = true;
	public Player playerTurn;
	public int myPlayerNumber = 0;

	public Node ballNode;
	
	private GameActivity gameActivity;
	
	private GameAIHandler gameAIHandler;
	
	public GameHandler(final GameActivity gameActivity, int gridX, int gridY, DifficultyEnum difficulty, Player player1, Player player2)
	{
		players.add(player1);
		players.add(player2);

		playerTurn = players.get(0);

		myPlayerNumber = 1;
		
		makeNodes(gridX, gridY);
		ballNode = findNodeByXY(gridX/2, gridY/2);
		
		this.gameActivity = gameActivity;
		gameActivity.reDraw();

		gameAIHandler = new GameAIHandler(this, difficulty);
	}
	
	//Clone constructor
	public GameHandler(GameHandler original)
	{
		nodeList = new ArrayList<Node>();
		for (Node node : original.nodeList)
		{
			nodeList.add(new Node(node));
		}
		for (final Node node : original.nodeList)
		{
			Node updateNode = nodeList.get(original.nodeList.indexOf(node));
			for (Node neighbor : node.neighbors)
			{
				updateNode.neighbors.add(nodeList.get(original.nodeList.indexOf(neighbor)));
			}
		}
		ballNode = findNodeById(original.ballNode.id);
		this.players = original.players;
		this.playerTurn = original.playerTurn;
	}
	
	//Creates the game nodes
	private void makeNodes(int gridSizeX, int gridSizeY)
	{
		for (int y = 1; y < gridSizeY; ++y)
		{
			for (int x = 0; x <= gridSizeX; ++x)
			{
				//Check if wall
				if (x == 0 || x == gridSizeX )
					nodeList.add(new Node(x,y, NodeTypeEnum.Wall));
				
				//Check if top wall special case
				else if (x != gridSizeX / 2 && y == 1)
				{
					nodeList.add(new Node(x,y, NodeTypeEnum.Wall));
				}

				//Check if bottom wall special case
				else if (x != gridSizeX / 2 && y == gridSizeY - 1)
				{
					nodeList.add(new Node(x,y, NodeTypeEnum.Wall));
				}
				
				//Regular empty node
				else
				{
					nodeList.add(new Node(x,y, NodeTypeEnum.Empty));
				}
			}
		}
		//Make the 2 goal nodes
		Node player1Goalnode = new Node(gridSizeX / 2, gridSizeY, NodeTypeEnum.Goal);
		nodeList.add(player1Goalnode);
		players.get(0).goalNode = player1Goalnode;
		
		Node player2Goalnode = new Node(gridSizeX / 2, 0, NodeTypeEnum.Goal);
		nodeList.add(player2Goalnode);
		players.get(1).goalNode = player2Goalnode;
		
		setNodeNeighbors();
	}
	
	//Sets the neighbors for all nodes
	private void setNodeNeighbors()
	{
		//Set neighbours
		for (Node n1 : nodeList)
		{
			for (Node n2 : nodeList)
			{
				//Vertical and horizontal neighbors
				if (n1.xCord - 1 == n2.xCord && n1.yCord == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord + 1 == n2.xCord && n1.yCord == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord == n2.xCord && n1.yCord + 1 == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord == n2.xCord && n1.yCord - 1 == n2.yCord)
					n1.neighbors.add(n2);
				
				//Diagonal neighbors
				if (n1.xCord - 1 == n2.xCord && n1.yCord - 1 == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord + 1 == n2.xCord && n1.yCord + 1 == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord - 1 == n2.xCord && n1.yCord + 1 == n2.yCord)
					n1.neighbors.add(n2);
				if (n1.xCord + 1 == n2.xCord && n1.yCord - 1 == n2.yCord)
					n1.neighbors.add(n2);
			}
		}
		
		//Walls should not have other walls as neighbors
		//Crap what about diagonals? Ugly temp solution
		for (Node n1 : nodeList)
		{
			for (Node n2 : nodeList)
			{
				if (n1.neighbors.contains(n2))
					if(n1.nodeType == NodeTypeEnum.Wall && n2.nodeType == NodeTypeEnum.Wall)
						if ((n1.yCord != n2.yCord && n1.xCord == n2.xCord) ||
							n1.yCord == n2.yCord && n1.xCord != n2.xCord)							
								n1.neighbors.remove(n2);
			}
		}
	}

	public void ProgressGame()
	{
		if (playerTurn.playerNumber != myPlayerNumber) gameAIHandler.MakeAIMove();
	}

	public void MakeMove(Move move)
	{
		if(isLegalMove(move))
		{
			float[] newLineCoords = nodeToCoords(move.newNode);
			float[] oldNodeCoords = nodeToCoords(move.oldNode);

			ballNode.neighbors.remove(move.newNode);
			ballNode.nodeType = NodeTypeEnum.BounceAble;

			move.newNode.neighbors.remove(ballNode);
			ballNode = move.newNode;

			gameActivity.AddNewLineToDraw(oldNodeCoords[0], oldNodeCoords[1], newLineCoords[0], newLineCoords[1], move.madeTheMove.playerColor);

			if(checkStateForWinner(move.newNode))
				return;

			if (!(ballNode.nodeType == NodeTypeEnum.BounceAble) && !(ballNode.nodeType == NodeTypeEnum.Wall))
			{
				move.newNode.nodeType = NodeTypeEnum.BounceAble;
				changeTurn();
			}
			if (!gameActivity.isMultiplayer) ProgressGame();
		}
		else
		{
			System.out.print("ILLEGAL MOVE YOU VILLAN!");
		}
	}

	public boolean checkStateForWinner(Node n)
	{
		Player winner = victoryCheck(n);
		if (winner != null)
		{
			winner(winner);
			return true;
		};
		return false;
	}

	public Player victoryCheck(Node n)
	{
		if(n.nodeType == NodeTypeEnum.Goal)
		{
			if (n.id == playerTurn.goalNode.id)
			{
				return getOpponent(playerTurn);
			}
			return playerTurn;
		}	
		else if (n.neighbors.size() == 0 && (n.nodeType == NodeTypeEnum.Wall || n.nodeType == NodeTypeEnum.BounceAble))
		{
			return getOpponent(playerTurn);
		}
		return null;
	}
	
	//Let the activity know we have a winner
	public void winner(Player winningPlayer)
	{
		gameActivity.Winner(winningPlayer);
	}
		
	//Converts screen coordinates to node coordinates
	public Node coordsToNode(float x, float y)
	{
		x = (x+gameActivity.gameView.gridXdraw/2)/gameActivity.gameView.gridXdraw * gameActivity.gameView.gridXdraw;
		y = (y+gameActivity.gameView.gridYdraw/2)/gameActivity.gameView.gridYdraw * gameActivity.gameView.gridYdraw-gameActivity.gameView.topEdge;
		
		x = x/gameActivity.gameView.gridXdraw;
		y = y/gameActivity.gameView.gridYdraw;

		System.out.print(x+","+y + " " + (int)x + "," + (int)y + " ");
		return findNodeByXY((int)x, (int)y);
	}
	
	//Returns the node with the XY coordinates
	public Node findNodeByXY(int x, int y)
	{
		for (Node n : nodeList)
		{
			if (n.xCord == x && n.yCord == y)			
				return n;		
		}
		return null;
	}

	public Node findNodeById(UUID id)
	{
		for (Node node : nodeList)
		{
			if (node.id == id)
				return node;
		}
		return null;
	}
	
	//Converts node coordinates to screen coordinates
	public float[] nodeToCoords(Node n)
	{
		float[] coords = new float[2];
		coords[0] = n.xCord * gameActivity.gameView.gridXdraw + gameActivity.gameView.leftEdge;
		coords[1] = n.yCord * gameActivity.gameView.gridYdraw + gameActivity.gameView.topEdge;
		return coords;
	}

	public Player getOpponent(Player myPlayer)
	{
		return players.get((players.indexOf(myPlayer) + 1) % 2);
	}
	
	//Gives the turn to the other player
	public Player changeTurn()
	{
		playerTurn = players.get((players.indexOf(playerTurn) + 1) % 2);
		return playerTurn;
	}
	
	//Checks if a move is legal in the current gamestate
	public boolean isLegalMove(Move move)
	{
		return ballNode.neighbors.contains(move.newNode) && move.madeTheMove == playerTurn;
	}
}
