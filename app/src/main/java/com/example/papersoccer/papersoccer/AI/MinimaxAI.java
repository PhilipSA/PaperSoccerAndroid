package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;

import java.util.HashSet;

public class MinimaxAI implements IGameAI
{
    private int callCount;
    private int searchDepth = 1;
    private HashSet<MoveData> MoveDataTree;

    private class MoveData {
        public double returnValue;
        public PartialMove returnMove;
        public String information;


        public MoveData() {
            returnValue = 0;
        }

        public void insertValueContext(double value, String context)
        {
            returnValue += value;
            information += String.format("| %f = %s |", value, context);
        }

    }

    @Override
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        MoveDataTree = new HashSet<>();
        callCount = 0;
        MoveData bestMove = alphaBetaPruning(searchDepth, gameHandler, gameHandler.currentPlayersTurn, Double.MIN_VALUE, Double.MAX_VALUE);
        System.out.println(callCount);
        return bestMove.returnMove;
    }

    private MoveData alphaBetaPruning(int currentDepth, GameHandler state, Player maximizingPlayer, double alpha, double beta)
    {
        ++callCount;

        if (currentDepth == 0 || state.getWinner(state.ballNode) != null)
        {
            return minmaxEvaluation(state);
        }

        MoveData returnMove;
        MoveData bestMove = null;
        if (maximizingPlayer == state.currentPlayersTurn)
        {
            double bestScore = alpha;

            for (PartialMove partialMove : state.allAvailiblePartialMovesFromNode(state.ballNode))
            {
                GameHandler clone = new GameHandler(state);
                partialMove.madeTheMove = clone.currentPlayersTurn;
                clone.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, clone, maximizingPlayer, bestScore, beta);
                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = partialMove;
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

            for (PartialMove partialMove : state.allAvailiblePartialMovesFromNode(state.ballNode))
            {
                GameHandler clone = new GameHandler(state);
                partialMove.madeTheMove = clone.currentPlayersTurn;
                clone.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, clone, maximizingPlayer, alpha, worstScore);

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = partialMove;
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

    private MoveData minmaxEvaluation(GameHandler clone)
    {
        MoveData moveData = new MoveData();

        if (clone.getWinner(clone.ballNode) == clone.player2) moveData.insertValueContext(1000, "GOAL!!");

        Node opponentsGoal = clone.getOpponent(clone.player2).goalNode;
        double distanceToOpponentsGoal = -MathHelper.distance(opponentsGoal.xCord, clone.ballNode.xCord, opponentsGoal.yCord, clone.ballNode.yCord);

        //Distance to opponents goal
        moveData.insertValueContext(distanceToOpponentsGoal, "Distance to opponents goal");

        MoveDataTree.add(moveData);
        return moveData;
    }
}
