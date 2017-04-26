package com.example.papersoccer.papersoccer.AI;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.Enums.SortOrderEnum;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move.PartialMove;
import com.example.papersoccer.papersoccer.GameObjects.Move.PossibleMove;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.Helpers.MathHelper;
import com.example.papersoccer.papersoccer.Helpers.Tree;
import com.google.common.hash.HashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class MinimaxAI implements IGameAI
{
    private int searchDepth = 7;

    private class MoveData implements Comparable<MoveData>
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
        public int compareTo(MoveData item) {
            if (this.returnValue < item.returnValue) {
                return -1;
            }
            else if(this.returnValue > item.returnValue){
                return 1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return String.format("TOTAL: %f | %s", returnValue, information);
        }
    }

    public MinimaxAI(int searchDepth)
    {
        this.searchDepth = searchDepth;
    }

    @Override
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        MoveData bestMove = alphaBetaPruning(searchDepth, gameHandler, gameHandler.currentPlayersTurn, -50000, 50000);
        return bestMove.returnMove;
    }

    private MoveData alphaBetaPruning(int currentDepth, GameHandler state, Player maximizingPlayer, double alpha, double beta)
    {
        if (currentDepth == 0 || state.getWinner(state.ballNode) != null)
        {
            return minmaxEvaluation(state, maximizingPlayer);
        }

        MoveData returnMove;
        MoveData bestMove = null;
        if (maximizingPlayer == state.currentPlayersTurn)
        {
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer);
            for (MoveData possibleMove : possibleMoves)
            {
                PartialMove partialMove = new PartialMove(possibleMove.returnMove.oldNode, possibleMove.returnMove.newNode, state.currentPlayersTurn);
                state.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta);
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
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer);
            for (MoveData possibleMove : possibleMoves)
            {
                PartialMove partialMove = new PartialMove(possibleMove.returnMove.oldNode, possibleMove.returnMove.newNode, state.currentPlayersTurn);
                partialMove.madeTheMove = state.currentPlayersTurn;
                state.MakePartialMove(partialMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta);
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

    private ArrayList<MoveData> sortPossibleMovesByScore(SortOrderEnum sortOrder, GameHandler state, Player maximzingPlayer)
    {
        ArrayList<MoveData> newPossibleMoves = new ArrayList<>();
        for (PossibleMove possibleMove : state.allPossibleMovesFromNode(state.ballNode))
        {
            PartialMove partialMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, state.currentPlayersTurn);
            partialMove.madeTheMove = state.currentPlayersTurn;
            state.MakePartialMove(partialMove);

            MoveData moveData = minmaxEvaluation(state, maximzingPlayer);
            moveData.returnMove = partialMove;

            newPossibleMoves.add(moveData);

            state.UndoLastMove();
        }

        if (sortOrder == SortOrderEnum.Ascending) {
            Collections.sort(newPossibleMoves);
        }
        else {
            Collections.sort(newPossibleMoves, Collections.reverseOrder());
        }
        return newPossibleMoves;
    }

    private MoveData minmaxEvaluation(GameHandler clone, Player maximizingPlayer)
    {
        MoveData moveData = new MoveData(clone.currentPlayersTurn.playerName);

        if (clone.isGameOver() && clone.getWinner(clone.ballNode).winner == maximizingPlayer) moveData.insertValueContext(1000, "GOAL!!");
        //if (clone.isGameOver() && clone.getWinner(clone.ballNode).winner != maximizingPlayer) moveData.insertValueContext(-1000, "NOOOO!!");

        moveData.insertValueContext(-clone.numberOfTurns, "Number of turns");

        Node opponentsGoal = clone.getOpponent(maximizingPlayer).goalNode;
        double distanceToOpponentsGoal = -MathHelper.distance(opponentsGoal.xCord, clone.ballNode.xCord, opponentsGoal.yCord, clone.ballNode.yCord);

        //Distance to opponents goal
        moveData.insertValueContext(distanceToOpponentsGoal, "Distance to opponents goal");

        return moveData;
    }
}
