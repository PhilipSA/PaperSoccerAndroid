package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;

public class MinimaxAI implements IGameAI
{
    private int callCount;
    private int searchDepth = 5;

    private class MoveValue {
        public double returnValue;
        public Move returnMove;

        public MoveValue() {
            returnValue = 0;
        }

        public MoveValue(double returnValue) {
            this.returnValue = returnValue;
        }

        public MoveValue(double returnValue, Move returnMove) {
            this.returnValue = returnValue;
            this.returnMove = returnMove;
        }

    }

    @Override
    public Move MakeMove(GameHandler gameHandler)
    {
        callCount = 0;
        Move bestMove = alphaBetaPruning(searchDepth, gameHandler, true, Double.MIN_VALUE, Double.MAX_VALUE).returnMove;
        System.out.println(callCount);
        return bestMove;
    }

    private MoveValue alphaBetaPruning(int currentDepth, GameHandler state, boolean maximizingPlayer, double alpha, double beta)
    {
        ++callCount;

        if (currentDepth == 0 || state.getWinner(state.ballNode) != null)
        {
            return new MoveValue(minmaxEvaluation(state));
        }

        MoveValue returnMove;
        MoveValue bestMove = null;
        if (maximizingPlayer)
        {
            double bestScore = alpha;

            for (Move currentMove : state.allAvailibleMoves())
            {
                GameHandler clone = new GameHandler(state);
                currentMove.madeTheMove = clone.currentPlayersTurn;
                clone.MakeMove(currentMove);
                maximizingPlayer = clone.extraTurn;
                returnMove = alphaBetaPruning(currentDepth - 1, clone, maximizingPlayer, bestScore, beta);
                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
        else
        {
            double worstScore = beta;

            for (Move currentMove : state.allAvailibleMoves())
            {
                GameHandler clone = new GameHandler(state);
                currentMove.madeTheMove = clone.currentPlayersTurn;
                clone.MakeMove(currentMove);
                maximizingPlayer = !clone.extraTurn;
                returnMove = alphaBetaPruning(currentDepth - 1, clone, maximizingPlayer, alpha, worstScore);

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }

    private double minmaxEvaluation(GameHandler clone)
    {
        double evalScore = 0;

        if (clone.getWinner(clone.ballNode) == clone.currentPlayersTurn)
        {
            evalScore += 1000;
        }
        else if (clone.getWinner(clone.ballNode) == clone.getOpponent(clone.currentPlayersTurn))
        {
            evalScore -= 1000;
        }

        evalScore -= clone.numberOfTurns;

        //If bounce able
        if (clone.ballNode.nodeType == NodeTypeEnum.BounceAble || clone.ballNode.nodeType == NodeTypeEnum.Wall)
            evalScore += 5;

        //If close to goal or bounceable
        for (Move move : clone.allAvailibleMoves())
        {
            if (clone.getWinner(move.newNode) == clone.currentPlayersTurn) evalScore += 1000;
            else if (clone.getWinner(move.newNode) == clone.getOpponent(clone.currentPlayersTurn)) evalScore -= 1000;

            Node opponentsGoal = clone.getOpponent(clone.currentPlayersTurn).goalNode;
            Node myGoal = clone.currentPlayersTurn.goalNode;

            if (MathHelper.euclideanDistance(move.newNode.xCord, opponentsGoal.xCord, move.newNode.yCord, opponentsGoal.yCord) == 1 && move.newNode.nodeType == NodeTypeEnum.BounceAble) evalScore += 1000;
            else if (MathHelper.euclideanDistance(move.newNode.xCord, myGoal.xCord, move.newNode.yCord, myGoal.yCord) == 1 && move.newNode.nodeType == NodeTypeEnum.BounceAble) evalScore -= 1000;

            if (move.newNode.nodeType == NodeTypeEnum.BounceAble) evalScore += 10;
        }

        //Distance to opponents goal
        evalScore += MathHelper.euclideanDistance(clone.ballNode.xCord, clone.getOpponent(clone.currentPlayersTurn).goalNode.xCord,
                clone.ballNode.yCord, clone.getOpponent(clone.currentPlayersTurn).goalNode.yCord);

        return evalScore;
    }
}
