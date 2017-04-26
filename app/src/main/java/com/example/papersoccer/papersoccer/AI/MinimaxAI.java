package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Move.PossibleMove;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;
import com.example.papersoccer.papersoccer.Helpers.Tree;

public class MinimaxAI implements IGameAI
{
    private int searchDepth = 7;
    private Tree<MoveData> MoveDataTree;

    private class MoveData
    {
        public double returnValue;
        public PartialMove returnMove;
        public String information;

        public MoveData(String playerName)
        {
            information = playerName;
            returnValue = 0;
        }

        public void insertValueContext(double value, String context)
        {
            returnValue += value;
            information += String.format("| %f = %s |", value, context);
        }

        @Override
        public String toString() {
            return String.format("TOTAL: %f | %s", returnValue, information);
        }
    }

    @Override
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        MoveDataTree = new Tree<>(null);
        MoveData bestMove = alphaBetaPruning(searchDepth, gameHandler, gameHandler.currentPlayersTurn, -50000, 50000, MoveDataTree);
        MoveDataTree = MoveDataTree.setAsParent(bestMove);
        System.out.printf(MoveDataTree.toString());
        return bestMove.returnMove;
    }

    private MoveData alphaBetaPruning(int currentDepth, GameHandler state, Player maximizingPlayer, double alpha, double beta, Tree<MoveData> tree)
    {
        if (currentDepth == 0 || state.getWinner(state.ballNode) != null)
        {
            return minmaxEvaluation(state, maximizingPlayer, tree);
        }

        tree = tree.addLeaf(new MoveData(state.currentPlayersTurn.playerName));

        MoveData returnMove;
        MoveData bestMove = null;
        if (maximizingPlayer == state.currentPlayersTurn)
        {
            for (PossibleMove possibleMove : state.allPossibleMovesFromNode(state.ballNode))
            {
                PartialMove partialMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, state.currentPlayersTurn);
                state.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta, tree);
                state.UndoLastMove();

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
            for (PossibleMove possibleMove : state.allPossibleMovesFromNode(state.ballNode))
            {
                PartialMove partialMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, state.currentPlayersTurn);
                partialMove.madeTheMove = state.currentPlayersTurn;
                state.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta, tree);
                state.UndoLastMove();

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

    private MoveData minmaxEvaluation(GameHandler clone, Player maximizingPlayer, Tree<MoveData> tree)
    {
        MoveData moveData = new MoveData(clone.currentPlayersTurn.playerName);

        if (clone.getWinner(clone.ballNode) == maximizingPlayer) moveData.insertValueContext(1000, "GOAL!!");

        moveData.insertValueContext(-clone.numberOfTurns, "Number of turns");

        Node opponentsGoal = clone.getOpponent(maximizingPlayer).goalNode;
        double distanceToOpponentsGoal = -MathHelper.distance(opponentsGoal.xCord, clone.ballNode.xCord, opponentsGoal.yCord, clone.ballNode.yCord);

        //Distance to opponents goal
        moveData.insertValueContext(distanceToOpponentsGoal, "Distance to opponents goal");

        tree.addLeaf(moveData);
        return moveData;
    }
}
