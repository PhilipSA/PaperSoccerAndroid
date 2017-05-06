package com.ps.simplepapersoccer.AI.MinimaxAI;

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI;
import com.ps.simplepapersoccer.Enums.SortOrderEnum;
import com.ps.simplepapersoccer.GameObjects.GameHandler;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;
import com.ps.simplepapersoccer.GameObjects.Node;
import com.ps.simplepapersoccer.GameObjects.Player;
import com.ps.simplepapersoccer.Helpers.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MinimaxAI implements IGameAI
{
    private int searchDepth = 7;

    public MinimaxAI(int searchDepth)
    {
        this.searchDepth = searchDepth;
    }

    @Override
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        long time = System.nanoTime();
        MoveData bestMove = alphaBetaPruning(searchDepth, gameHandler, gameHandler.currentPlayersTurn, -50000, 50000);
        long stopTime = System.nanoTime() - time;
        return bestMove.returnMove;

    }

    private MoveData alphaBetaPruning(int currentDepth, GameHandler state, Player maximizingPlayer, double alpha, double beta)
    {
        double alphaOrig = alpha;

        if (currentDepth == 0 || state.getWinner(state.ballNode) != null)
        {
            return new MoveData(minmaxEvaluation(state, maximizingPlayer));
        }

        MoveData returnMove;
        MoveData bestMove = null;
        if (maximizingPlayer == state.currentPlayersTurn)
        {
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer, currentDepth);
            for (MoveData possibleMove : possibleMoves)
            {
                state.MakePartialMove(possibleMove.returnMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta);
                state.UndoLastMove();

                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = possibleMove.returnMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    break; // pruning
                }
            }

            return bestMove;
        }
        else
        {
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer, currentDepth);
            for (MoveData possibleMove : possibleMoves)
            {
                state.MakePartialMove(possibleMove.returnMove);
                returnMove = alphaBetaPruning(currentDepth - 1, state, maximizingPlayer, alpha, beta);
                state.UndoLastMove();

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = possibleMove.returnMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    break; // pruning
                }
            }

            return bestMove;
        }
    }

    private ArrayList<MoveData> sortPossibleMovesByScore(SortOrderEnum sortOrder, GameHandler state, Player maximzingPlayer, int depth)
    {
        ArrayList<MoveData> newPossibleMoves = new ArrayList<>();
        for (PossibleMove possibleMove : state.allPossibleMovesFromNode(state.ballNode))
        {
            PartialMove partialMove = new PartialMove(possibleMove.oldNode, possibleMove.newNode, state.currentPlayersTurn);
            partialMove.madeTheMove = state.currentPlayersTurn;
            state.MakePartialMove(partialMove);

            MoveData moveData = new MoveData(minmaxEvaluation(state, maximzingPlayer));
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

    private double minmaxEvaluation(GameHandler state, Player maximizingPlayer)
    {
        double score = 0;

        if (state.isGameOver() && state.getWinner(state.ballNode).winner == maximizingPlayer) score = 1000;
        if (state.isGameOver() && state.getWinner(state.ballNode).winner != maximizingPlayer) score = -1000;

        score += -state.numberOfTurns;

        Node opponentsGoal = state.getOpponent(maximizingPlayer).goalNode;
        score += -MathHelper.distance(opponentsGoal.xCord, state.ballNode.xCord, opponentsGoal.yCord, state.ballNode.yCord);

        return score;
    }
}
