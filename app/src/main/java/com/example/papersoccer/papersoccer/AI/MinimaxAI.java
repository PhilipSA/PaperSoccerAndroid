package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;

public class MinimaxAI implements IGameAI
{
    private int callCount;
    private int searchDepth = 2;
    private Node bestNode;

    @Override
    public Node MakeMove(GameHandler gameHandler)
    {
        callCount = 0;
        bestScoreForMaximizingPlayer(searchDepth, gameHandler, gameHandler.player2, 0, 0);
        return bestNode;
    }

    private int bestScoreForMaximizingPlayer(int currentDepth, GameHandler state, Player maximizingPlayer, int min, int max)
    {
        ++callCount;
        if (state.currentPlayersTurn == maximizingPlayer)
        {
            return max(currentDepth, state, maximizingPlayer, max);
        }
        else
        {
            return min(currentDepth, state, maximizingPlayer, min);
        }
    }

    private int max(int currentDepth, GameHandler state, Player maximizingPlayer, int max)
    {
        if (currentDepth == 0 || state.getWinner(state.ballNode) != null) return minmaxEvaluation(state);

        int bestScore = Integer.MIN_VALUE;
        int score = 0;
        for (Node evaluateNode : state.allAvailibleMoves())
        {
            GameHandler clone = new GameHandler(state);
            clone.MakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.currentPlayersTurn));
            score = bestScoreForMaximizingPlayer(currentDepth-1, clone, maximizingPlayer, score, max);
            if (score > bestScore)
            {
                if (currentDepth == searchDepth) bestNode = evaluateNode;
                bestScore = score;
            }
            if (score > max) return max;
        }
        return bestScore;
    }

    private int min(int currentDepth, GameHandler state, Player maximizingPlayer, int min)
    {
        if (currentDepth == 0 || state.getWinner(state.ballNode) != null) return minmaxEvaluation(state);

        int worstScore = Integer.MAX_VALUE;
        int score = 0;
        for (Node evaluateNode : state.allAvailibleMoves())
        {
            GameHandler clone = new GameHandler(state);
            clone.MakeMove(new Move(clone.ballNode, clone.findNodeById(evaluateNode.id), clone.currentPlayersTurn));
            score = bestScoreForMaximizingPlayer(currentDepth-1, clone, maximizingPlayer, min, score);
            if (score < worstScore)
            {
                if (currentDepth == searchDepth) bestNode = evaluateNode;
                worstScore = score;
            }
            if (score < min) return min;
        }
        return worstScore;
    }

    private int minmaxEvaluation(GameHandler clone)
    {
        int evalScore = 0;

/*        if (clone.getWinner(clone.ballNode) == clone.currentPlayersTurn)
        {
            return 1000;
        }
        else if (clone.getWinner(clone.ballNode) == clone.getOpponent(clone.currentPlayersTurn))
        {
            return -1000;
        }

        evalScore -= clone.numberOfTurns;

        //If bounce able
        if (clone.ballNode.nodeType == NodeTypeEnum.BounceAble || clone.ballNode.nodeType == NodeTypeEnum.Wall)
            evalScore += 5;

        //If close to goal or bounceable
        for (Node node : clone.ballNode.neighbors)
        {
            if (node.nodeType == NodeTypeEnum.Goal && node.id == clone.getOpponent(clone.currentPlayersTurn).goalNode.id) evalScore += 100;
            if (node.nodeType == NodeTypeEnum.Goal && node.id == clone.currentPlayersTurn.goalNode.id) evalScore -= 100;

            if (node.nodeType == NodeTypeEnum.Goal && clone.ballNode.nodeType == NodeTypeEnum.BounceAble && clone.getOpponent(clone.currentPlayersTurn).goalNode.id == node.id) return 1000;
            if (node.nodeType == NodeTypeEnum.Goal && clone.ballNode.nodeType == NodeTypeEnum.BounceAble && clone.currentPlayersTurn.goalNode.id == node.id) return -1000;

            if (node.nodeType == NodeTypeEnum.BounceAble) evalScore += 10;
        }*/

        //Distance to opponents goal
        int euclideanDistance = (int)Math.sqrt(Math.pow((clone.ballNode.xCord - clone.getOpponent(clone.currentPlayersTurn).goalNode.xCord), 2) +
                Math.pow((clone.ballNode.yCord - clone.getOpponent(clone.currentPlayersTurn).goalNode.yCord), 2));
        evalScore -= euclideanDistance;

        //Distance to own goal
/*        euclideanDistance = (int)Math.sqrt(Math.pow((clone.ballNode.xCord - clone.currentPlayersTurn.goalNode.xCord), 2) +
                Math.pow((clone.ballNode.yCord - clone.currentPlayersTurn.goalNode.yCord), 2));
        evalScore += euclideanDistance;*/

        return evalScore;
    }
}
