package com.example.papersoccer.papersoccer.GameObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.example.papersoccer.papersoccer.AI.GameAIHandler;
import com.example.papersoccer.papersoccer.Activites.GameActivity;
import com.example.papersoccer.papersoccer.Enums.DifficultyEnum;
import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;
import com.example.papersoccer.papersoccer.Enums.VictoryConditionEnum;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Move.PossibleMove;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;
import com.example.papersoccer.papersoccer.R;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class GameHandler implements Cloneable {

	public HashMap<UUID, Node> nodeHashMap = new HashMap<>();
	private Multimap<UUID, UUID> connectedNodes = HashMultimap.create();

	public Player player1;
	public Player player2;

	public Player currentPlayersTurn;

	public int numberOfTurns = 0;

	public Node ballNode;

	private ArrayList<PartialMove> allPartialMoves = new ArrayList<>();

	private GameActivity gameActivity;
	private GameAIHandler gameAIHandler;

	private boolean isMultiplayer;
	private boolean aiTurn = false;
	
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
	}

	public HashSet<PossibleMove> allPossibleMovesFromNode(Node currentBallNode)
	{
		HashSet<PossibleMove> possibleMoves = new HashSet<>();

		for (Node n1 : nodeHashMap.values())
		{
			if (existsNodeConnection(currentBallNode, n1)) continue;
			if (n1.id == currentBallNode.id) continue;

			double euclideanDistance = MathHelper.euclideanDistance(currentBallNode.xCord, n1.xCord, currentBallNode.yCord, n1.yCord);

			if(currentBallNode.nodeType == NodeTypeEnum.Wall && n1.nodeType == NodeTypeEnum.Wall)
			{
				if (currentBallNode.yCord != n1.yCord && n1.xCord != currentBallNode.xCord && euclideanDistance < 2)
				{
					possibleMoves.add(new PossibleMove(currentBallNode, n1));
				}
				else
				{
					continue;
				}
			}
			if (euclideanDistance < 2) possibleMoves.add(new PossibleMove(currentBallNode, n1));
		}
		return possibleMoves;
	}

	private boolean existsNodeConnection(Node node1, Node node2)
	{
		if (connectedNodes.get(node1.id).contains(node2.id)) return true;
		if (connectedNodes.get(node2.id).contains(node1.id)) return true;
		return false;
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

	public void DrawPartialMove(PartialMove move, int playerColor)
	{
		try {
			float[] newLineCoords = nodeToCoords(move.newNode);
			float[] oldNodeCoords = nodeToCoords(move.oldNode);
			gameActivity.AddNewLineToDraw(oldNodeCoords[0], oldNodeCoords[1], newLineCoords[0], newLineCoords[1], playerColor);

			gameActivity.UpdateDrawData();
			gameActivity.gameView.invalidate();
		}
		catch(Exception e)
		{
			return;
		}
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
			gameActivity.PlaySoundEffect(R.raw.bounce);
		}
		else {
			gameActivity.PlaySoundEffect(R.raw.soccerkick);
		}

		DrawPartialMove(partialMove, partialMove.madeTheMove.playerColor);
		++numberOfTurns;
	}

	public void UndoLastMove()
    {
		PartialMove partialMove = Iterables.getLast(allPartialMoves);

		connectedNodes.remove(partialMove.newNode.id, partialMove.oldNode.id);

		findNodeById(partialMove.newNode.id).nodeType = partialMove.newNode.nodeType;

		ballNode = partialMove.oldNode;

		allPartialMoves.remove(partialMove);

		currentPlayersTurn = partialMove.madeTheMove;
    }

	public void MakePartialMove(PartialMove partialMove)
	{
		allPartialMoves.add(new PartialMove(new Node(partialMove.oldNode), new Node(partialMove.newNode), partialMove.madeTheMove));

		connectedNodes.put(partialMove.newNode.id, partialMove.oldNode.id);

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
		else if (allPossibleMovesFromNode(node).size() == 0)
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

	public Node findNodeById(UUID id)
	{
		return nodeHashMap.get(id);
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
