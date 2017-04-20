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
            clone.MakeMove(new Move(clone.ballNode, clone.findNodeById(node.id), clone.playerTurn));
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
        if (currentDepth == 0 || state.victoryCheck(state.ballNode) != null) return minmaxEvaluation(state);
        --currentDepth;

        int worstScore = Integer.MAX_VALUE;
        int score;
        for (Node evaluateNode : state.ballNode.neighbors)
        {
            GameHandler clone = new GameHandler(state);
            clone.MakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.playerTurn));
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
        if (currentDepth == 0 || state.victoryCheck(state.ballNode) != null) return minmaxEvaluation(state);
        --currentDepth;

        int bestScore = Integer.MIN_VALUE;
        int score;
        for (Node evaluateNode : state.ballNode.neighbors)
        {
            GameHandler clone = new GameHandler(state);
            clone.MakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.playerTurn));
            score = bestScoreForMaximizingPlayer(currentDepth, clone, maximizingPlayer);
            if (score > bestScore)
            {
                bestScore = score;
            }
        }
        return bestScore;
    }

    private int minmaxEvaluation(GameHandler clone)
    {
        int evalScore = 0;

        if (clone.victoryCheck(clone.ballNode) == clone.playerTurn)
        {
            return 1000;
        }
        else if (clone.victoryCheck(clone.ballNode) != clone.getOpponent(clone.playerTurn))
        {
            return -1000;
        }

        evalScore -= clone.numberOfTurns;

        //If bounceable
        if (clone.ballNode.nodeType == NodeTypeEnum.BounceAble || clone.ballNode.nodeType == NodeTypeEnum.Wall)
            evalScore += 5;

        //If close to goal
        for (Node node : clone.ballNode.neighbors)
        {
            if (node.nodeType == NodeTypeEnum.Goal && node.id == clone.getOpponent(clone.playerTurn).goalNode.id) evalScore += 20;
            if (node.nodeType == NodeTypeEnum.Goal && node.id == clone.playerTurn.goalNode.id) evalScore -= 20;
        }

        //Distance to opponents goal
        int euclideanDistance = (int)Math.sqrt(Math.pow((clone.ballNode.xCord - clone.getOpponent(clone.playerTurn).goalNode.xCord), 2) +
                Math.pow((clone.ballNode.yCord - clone.getOpponent(clone.playerTurn).goalNode.yCord), 2));
        evalScore -= euclideanDistance*2;

        //Distance to own goal
        euclideanDistance = (int)Math.sqrt(Math.pow((clone.ballNode.xCord - clone.playerTurn.goalNode.xCord), 2) +
                Math.pow((clone.ballNode.yCord - clone.playerTurn.goalNode.yCord), 2));
        evalScore += euclideanDistance*2;

        return evalScore;
    }
}
