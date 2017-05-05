package com.ps.simplepapersoccer.GameObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.ps.simplepapersoccer.AI.GameAIHandler;
import com.ps.simplepapersoccer.Activities.GameActivity;
import com.ps.simplepapersoccer.Enums.DifficultyEnum;
import com.ps.simplepapersoccer.Enums.NodeTypeEnum;
import com.ps.simplepapersoccer.Enums.VictoryConditionEnum;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;
import com.ps.simplepapersoccer.Helpers.MathHelper;
import com.ps.simplepapersoccer.R;
import com.ps.simplepapersoccer.Sound.FXPlayer;
import com.google.common.collect.Iterables;

public class GameHandler {

	public HashMap<UUID, Node> nodeHashMap = new HashMap<>();

	public Player player1;
	public Player player2;

	public Player currentPlayersTurn;

	public int numberOfTurns = 0;

	public Node ballNode;

	private ArrayList<PartialMove> allPartialMoves = new ArrayList<>();

	private GameActivity gameActivity;
	private GameAIHandler gameAIHandler;

	private boolean isMultiplayer;
	public boolean aiTurn = false;
	
	public GameHandler(final GameActivity gameActivity, int gridX, int gridY, DifficultyEnum difficulty, ArrayList<Player> players, boolean isMultiplayer)
	{
		this.player1 = players.get(0);
		this.player2 = players.get(1);

		currentPlayersTurn = players.get(0);

		this.isMultiplayer = isMultiplayer;

		makeNodes(gridX, gridY);
		ballNode = findNodeByXY(gridX/2, gridY/2);
		
		this.gameActivity = gameActivity;
		gameActivity.reDraw();

		gameAIHandler = new GameAIHandler(this, difficulty);
	}
	
	//Creates the game nodes
	private void makeNodes(int gridSizeX, int gridSizeY)
	{
		for (int y = 1; y < gridSizeY; ++y)
		{
			for (int x = 0; x <= gridSizeX; ++x)
			{
				//No node in the 4 cornes
				if (x == 0 && y == 1) continue;
				if (x == 0 && y == gridSizeY - 1) continue;
				if (x == gridSizeX && y == 1) continue;
				if (x == gridSizeX && y == gridSizeY - 1) continue;

				//Check if wall
				if (x == 0 || x == gridSizeX )
					addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));
				
				//Check if top wall special case
				else if (x != gridSizeX / 2 && y == 1)
				{
					addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));
				}

				//Check if bottom wall special case
				else if (x != gridSizeX / 2 && y == gridSizeY - 1)
				{
					addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));
				}
				
				//Regular empty node
				else
				{
					addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Empty));
				}
			}
		}
		//Make the 2 goal nodes
		Node player1Goalnode = new Node(gridSizeX / 2, gridSizeY, NodeTypeEnum.Goal);
		addNodeToNodeMap(player1Goalnode);
		
		Node player2Goalnode = new Node(gridSizeX / 2, 0, NodeTypeEnum.Goal);
		addNodeToNodeMap(player2Goalnode);

		player1.goalNode = player1Goalnode;
		player2.goalNode = player2Goalnode;

		GenerateAllNeighbors();
	}

	public void GenerateAllNeighbors()
	{
		for (Node node : nodeHashMap.values()) {
			for (Node otherNode : nodeHashMap.values()) {
				if (node.id == otherNode.id) continue;

				double euclideanDistance = MathHelper.euclideanDistance(node.xCord, otherNode.xCord, node.yCord, otherNode.yCord);

				if (node.nodeType == NodeTypeEnum.Wall && otherNode.nodeType == NodeTypeEnum.Wall) {
					if (node.yCord != otherNode.yCord && otherNode.xCord != node.xCord && euclideanDistance < 2) {
						node.AddNeighborPair(otherNode);
					} else {
						continue;
					}
				}
				if (euclideanDistance < 2) node.AddNeighborPair(otherNode);
			}
		}
	}

	public HashSet<PossibleMove> allPossibleMovesFromNode(Node node)
	{
		HashSet<PossibleMove> possibleMoves = new HashSet<>();

		for (UUID uuid : node.neighbors) {
			Node otherNode = nodeHashMap.get(uuid);
			possibleMoves.add(new PossibleMove(node, otherNode));
		}

		return possibleMoves;
	}

	public void UpdateGameState()
	{
		gameActivity.UpdateDrawData();

		if(isGameOver())
		{
			winner(getWinner(ballNode));
			return;
		}
		if (currentPlayersTurn.isAi && !isMultiplayer)
		{
			aiTurn = true;
			gameAIHandler.MakeAIMove();
		};
	}

	public void PlayerMakeMove(Node node, Player player)
	{
		PartialMove partialMove = new PartialMove(ballNode, node, player);
		if (isPartialMoveLegal(partialMove, player) && !aiTurn)
		{
			MakeMove(partialMove);
			UpdateGameState();
		}
	}

	public void AIMakeMove(PartialMove move)
	{
		MakeMove(move);
		aiTurn = false;
		UpdateGameState();
	}

	public void MakeMove(PartialMove partialMove)
	{
		MakePartialMove(partialMove);

		if(ballNode.nodeType != NodeTypeEnum.Empty) {
			gameActivity.fxPlayer.playSound(R.raw.bounce);
		}
		else {
			gameActivity.fxPlayer.playSound(R.raw.soccerkick);
		}

		gameActivity.DrawPartialMove(partialMove, partialMove.madeTheMove.playerColor);
		++numberOfTurns;
	}

	public void UndoLastMove()
    {
		PartialMove partialMove = Iterables.getLast(allPartialMoves);

		partialMove.newNode.AddNeighborPair(partialMove.oldNode);

		nodeHashMap.get(partialMove.newNode.id).nodeType = partialMove.newNode.nodeType;

		ballNode = partialMove.oldNode;

		allPartialMoves.remove(partialMove);

		currentPlayersTurn = partialMove.madeTheMove;
    }

	public void MakePartialMove(PartialMove partialMove)
	{
		allPartialMoves.add(new PartialMove(new Node(partialMove.oldNode), new Node(partialMove.newNode), partialMove.madeTheMove));

		partialMove.newNode.RemoveNeighborPair(partialMove.oldNode);

		if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
			partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble;

		if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
			changeTurn();
		}

		ballNode = partialMove.newNode;
	}

	public boolean isGameOver()
	{
		if (getWinner(ballNode) != null)
		{
			return true;
		};
		return false;
	}

	public Victory getWinner(Node node)
	{
		if(node.nodeType == NodeTypeEnum.Goal)
		{
			if (node.id == currentPlayersTurn.goalNode.id)
			{
				return new Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.Goal);
			}
			return new Victory(currentPlayersTurn, VictoryConditionEnum.Goal);
		}	
		else if (node.neighbors.size() == 0)
		{
			return new Victory(getOpponent(currentPlayersTurn), VictoryConditionEnum.OpponentOutOfMoves);
		}
		return null;
	}
	
	//Let the activity know we have a winner
	public void winner(Victory victory)
	{
		victory.winner.score += 1;
		gameActivity.Winner(victory);
	}
		
	//Converts screen coordinates to node coordinates
	public Node coordsToNode(float x, float y)
	{
		x = (x+gameActivity.gameView.gridXdraw/2)/gameActivity.gameView.gridXdraw * gameActivity.gameView.gridXdraw;
		y = (y+gameActivity.gameView.gridYdraw/2)/gameActivity.gameView.gridYdraw * gameActivity.gameView.gridYdraw-gameActivity.gameView.topEdge;
		
		x = x/gameActivity.gameView.gridXdraw;
		y = y/gameActivity.gameView.gridYdraw;

		return findNodeByXY((int)x, (int)y);
	}
	
	//Returns the node with the XY coordinates
	public Node findNodeByXY(int x, int y)
	{
		for (Node n : nodeHashMap.values())
		{
			if (n.xCord == x && n.yCord == y)			
				return n;		
		}
		return null;
	}

	public void addNodeToNodeMap(Node node)
	{
		nodeHashMap.put(node.id, node);
	}

	public Player getOpponent(Player myPlayer)
	{
		if (myPlayer == player1) return player2;
		return player1;
	}

	public Player changeTurn()
	{
		currentPlayersTurn = getOpponent(currentPlayersTurn);
		return currentPlayersTurn;
	}

	public boolean isPartialMoveLegal(PartialMove partialMove, Player player)
	{
		HashSet<PossibleMove> test = allPossibleMovesFromNode(ballNode);
		return test.contains(new PossibleMove(partialMove.oldNode, partialMove.newNode)) && player == currentPlayersTurn;
	}
}
