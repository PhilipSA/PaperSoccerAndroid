package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;

public class MinMaxAI implements IGameAI {

    @Override
    public Node MakeMove(GameHandler gameHandler) {
        Node bestNode = null;
        int bestScore = Integer.MIN_VALUE;
        for (Node node : gameHandler.ballNode.neighbors)
        {
            GameHandler clone = new GameHandler(gameHandler);
            minmaxFakeMove(new Move(clone.ballNode, clone.findNodeById(node.id), clone.playerTurn), clone);
            int score = bestScoreForMaximizingPlayer(4, clone, gameHandler.playerTurn);
            if (score > bestScore)
            {
                bestNode = node;
                bestScore = score;
            }
        }
        return bestNode;
    }

    private int bestScoreForMaximizingPlayer(int currentDepth, GameHandler state, Player maximizingPlayer)
    {
        if (state.playerTurn.playerNumber == maximizingPlayer.playerNumber)
        {
            return max(currentDepth, state, maximizingPlayer);
        }
        else
        {
            return min(currentDepth, state, maximizingPlayer);
        }
    }

    private int min(int currentDepth, GameHandler state, Player maximizingPlayer)
    {
        if (currentDepth == 0) return minmaxEvaluation(state);
        --currentDepth;

        int worstScore = Integer.MAX_VALUE;
        int score;
        for (Node evaluateNode : state.ballNode.neighbors)
        {
            GameHandler clone = new GameHandler(state);
            minmaxFakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.playerTurn), clone);
            score = bestScoreForMaximizingPlayer(currentDepth, clone, maximizingPlayer);
            if (score < worstScore)
            {
                worstScore = score;
            }
        }
        return worstScore;
    }

    private int max(int currentDepth, GameHandler state, Player maximizingPlayer)
    {
        if (currentDepth == 0) return minmaxEvaluation(state);
        --currentDepth;

        int bestScore = Integer.MIN_VALUE;
        int score;
        for (Node evaluateNode : state.ballNode.neighbors)
        {
            GameHandler clone = new GameHandler(state);
            minmaxFakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.playerTurn), clone);
            score = bestScoreForMaximizingPlayer(currentDepth, clone, maximizingPlayer);
            if (score > bestScore)
            {
                bestScore = score;
            }
        }
        return bestScore;
    }

    private void minmaxFakeMove(Move move, GameHandler clone)
    {
        if (clone.isLegalMove(move))
        {
            move.oldNode.neighbors.remove(move.newNode);
            move.oldNode.nodeType = NodeTypeEnum.BounceAble;

            move.newNode.neighbors.remove(move.oldNode);
            clone.ballNode = move.newNode;

            if(clone.victoryCheck(move.newNode) != null)
                return;

            if (!(clone.ballNode.nodeType == NodeTypeEnum.BounceAble) && !(clone.ballNode.nodeType == NodeTypeEnum.Wall))
            {
                move.newNode.nodeType = NodeTypeEnum.BounceAble;
                clone.changeTurn();
            }
        }

    }

    private int minmaxEvaluation(GameHandler clone)
    {
        int evalScore = 0;

        if (clone.victoryCheck(clone.ballNode) == clone.playerTurn)
        {
            return Integer.MAX_VALUE;
        }
        else if (clone.victoryCheck(clone.ballNode) != null)
        {
            return Integer.MIN_VALUE;
        }

        //If bounceable
        if (clone.ballNode.nodeType == NodeTypeEnum.BounceAble)
            evalScore += 1;

        //If close to goal
        for (Node node : clone.ballNode.neighbors)
        {
            if (node.nodeType == NodeTypeEnum.Goal && node.id == clone.getOpponent(clone.playerTurn).goalNode.id) evalScore += 10;
        }

        //Distance to goal
        int manhattanDistance = Math.abs(clone.ballNode.xCord - clone.getOpponent(clone.playerTurn).goalNode.xCord) + Math.abs(clone.ballNode.yCord - clone.getOpponent(clone.playerTurn).goalNode.yCord);
        evalScore += manhattanDistance;

        return evalScore;
    }
}
