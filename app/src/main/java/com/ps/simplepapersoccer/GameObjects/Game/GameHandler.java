package com.ps.simplepapersoccer.GameObjects.Game;

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
import com.ps.simplepapersoccer.GameObjects.Player;
import com.ps.simplepapersoccer.Helpers.MathHelper;
import com.ps.simplepapersoccer.R;
import com.google.common.collect.Iterables;

public class GameHandler {
	public Player player1;
	public Player player2;

	public Player currentPlayersTurn;

	public int numberOfTurns = 0;

	private GameActivity gameActivity;
	private GameAIHandler gameAIHandler;
	private GameBoard gameBoard;

	private boolean isMultiplayer;
	public boolean aiTurn = false;

	public Node ballNode() { return gameBoard.ballNode; }

	@Override
	public int hashCode() {
		return currentPlayersTurn.hashCode() ^ gameBoard.hashCode();
	}
	
	public GameHandler(final GameActivity gameActivity, int gridX, int gridY, DifficultyEnum difficulty, ArrayList<Player> players, boolean isMultiplayer)
	{
		this.player1 = players.get(0);
		this.player2 = players.get(1);

		currentPlayersTurn = players.get(0);

		this.isMultiplayer = isMultiplayer;

		gameBoard = new GameBoard(gridX, gridY);
		player1.goalNode = gameBoard.goalNode1;
		player2.goalNode = gameBoard.goalNode2;
		
		this.gameActivity = gameActivity;
		gameActivity.reDraw();

		gameAIHandler = new GameAIHandler(this, difficulty);
	}

	public HashSet<PossibleMove> allPossibleMovesFromNode(Node node)
	{
		return gameBoard.allPossibleMovesFromNode(node);
	}

	public void UpdateGameState()
	{
		gameActivity.UpdateDrawData();

		if(isGameOver())
		{
			winner(getWinner(ballNode()));
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
		PartialMove partialMove = new PartialMove(ballNode(), node, player);
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

		if(ballNode().nodeType != NodeTypeEnum.Empty) {
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
		currentPlayersTurn = gameBoard.UndoLastMove().madeTheMove;
    }

	public void MakePartialMove(PartialMove partialMove)
	{
		gameBoard.MakePartialMove(partialMove);

		if (partialMove.newNode.nodeType == NodeTypeEnum.Empty) {
			changeTurn();
		}
	}

	public boolean isGameOver()
	{
		if (getWinner(gameBoard.ballNode) != null)
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

		return gameBoard.findNodeByXY((int)x, (int)y);
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
		HashSet<PossibleMove> test = allPossibleMovesFromNode(ballNode());
		return test.contains(new PossibleMove(partialMove.oldNode, partialMove.newNode)) && player == currentPlayersTurn;
	}
}
