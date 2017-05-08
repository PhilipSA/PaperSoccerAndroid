package com.ps.simplepapersoccer.AI.MinimaxAI;

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI;
import com.ps.simplepapersoccer.Enums.NodeTypeEnum;
import com.ps.simplepapersoccer.Enums.SortOrderEnum;
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;
import com.ps.simplepapersoccer.GameObjects.Game.Node;
import com.ps.simplepapersoccer.GameObjects.Player;
import com.ps.simplepapersoccer.Helpers.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MinimaxAI implements IGameAI
{
    private int searchDepth = 7;
    private static final int TIME_LIMIT_MILLIS = 2000;
    private static final int EVALS_PER_SECOND = 100;
    private static final int winCutoff = 900;
    private static boolean searchCutoff = false;

    private HashMap<Integer, TranspositionData> transpositionsMap = new HashMap<>();

    public MinimaxAI(int searchDepth)
    {
        this.searchDepth = searchDepth;
    }

    @Override
    public PartialMove MakeMove(GameHandler gameHandler)
    {
        long time = System.nanoTime();
        MoveData bestMove = chooseMove(gameHandler);
        long stopTime = System.nanoTime() - time;
        return bestMove.returnMove;

    }

    private MoveData chooseMove(GameHandler state) {
        long startTime = System.currentTimeMillis();
        double maxScore = Integer.MIN_VALUE;
        MoveData bestMove = null;
        Player maximPlayer = state.currentPlayersTurn;

        ArrayList<MoveData> moves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, state.currentPlayersTurn);

        for (MoveData move : moves) {

            state.MakePartialMove(move.returnMove);

            //
            // Compute how long to spend looking at each move
            //
            long searchTimeLimit = ((TIME_LIMIT_MILLIS - 1000) / (moves.size()));

            double score = iterativeDeepeningSearch(state, searchTimeLimit, maximPlayer);

            state.UndoLastMove();

            //
            // If the search finds a winning move
            //
            if (score >= winCutoff) {
                return move;
            }

            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    //
    // Run an iterative deepening search on a game state, taking no longer than the given time limit
    //
    private double iterativeDeepeningSearch(GameHandler state, long timeLimit, Player maximPlayer) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeLimit;
        int depth = 1;
        double score = 0;
        searchCutoff = false;

        while (true) {
            long currentTime = System.currentTimeMillis();

            if (currentTime >= endTime) {
                break;
            }

            double searchResult = alphaBetaPruning(state, depth, maximPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE, currentTime, endTime - currentTime);

            //
            // If the search finds a winning move, stop searching
            //
            if (searchResult >= winCutoff) {
                return searchResult;
            }

            if (!searchCutoff) {
                score = searchResult;
            }

            depth++;
        }

        return score;
    }

    private double alphaBetaPruning(GameHandler state, int currentDepth, Player maximizingPlayer, double alpha, double beta, long startTime, long timeLimit)
    {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - startTime);

        if (elapsedTime >= timeLimit) {
            searchCutoff = true;
        }

        double storedAlpha = alpha;
        double storedBeta = beta;

        TranspositionData transpositionData = transpositionsMap.get(state.hashCode());
        if (transpositionData != null && transpositionData.depth >= currentDepth) {
            if (transpositionData.scoreTypeEnum == ScoreTypeEnum.EXACT){
                return transpositionData.score;
            }
            else if (transpositionData.scoreTypeEnum == ScoreTypeEnum.UPPER){
                beta = Math.min(beta, transpositionData.score);
            }
            else if (transpositionData.scoreTypeEnum == ScoreTypeEnum.LOWER){
                alpha = Math.max(alpha, transpositionData.score);
            }
            if(alpha >= beta){
                return transpositionData.score;
            }
        }

        if (searchCutoff || currentDepth == 0 || state.getWinner(state.ballNode()) != null)
        {
            double value = minmaxEvaluation(state, maximizingPlayer);
            if(value <= alpha) {
                transpositionsMap.put(state.hashCode(), new TranspositionData(currentDepth, value, ScoreTypeEnum.LOWER));
            }
            else if(value >= beta) {
                transpositionsMap.put(state.hashCode(), new TranspositionData(currentDepth, value, ScoreTypeEnum.UPPER));
            }
            else {
                transpositionsMap.put(state.hashCode(), new TranspositionData(currentDepth, value, ScoreTypeEnum.EXACT));
            }
            return value;
        }

        double bestScore = 0;

        if (maximizingPlayer == state.currentPlayersTurn)
        {
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Descending, state, maximizingPlayer);
            for (MoveData possibleMove : possibleMoves)
            {
                state.MakePartialMove(possibleMove.returnMove);

                alpha = Math.max(alpha, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit));

                state.UndoLastMove();

                if (beta <= alpha) {
                    break; // pruning
                }
            }
            bestScore = alpha;
        }
        else
        {
            ArrayList<MoveData> possibleMoves = sortPossibleMovesByScore(SortOrderEnum.Ascending, state, maximizingPlayer);
            for (MoveData possibleMove : possibleMoves)
            {
                state.MakePartialMove(possibleMove.returnMove);

                beta = Math.min(beta, alphaBetaPruning(state, currentDepth - 1, maximizingPlayer, alpha, beta, startTime, timeLimit));

                state.UndoLastMove();

                if (beta <= alpha) {
                    break; // pruning
                }
            }

            bestScore = beta;
        }

        TranspositionData next = new TranspositionData();
        next.score = bestScore;
        next.depth = currentDepth;
        if(bestScore <= storedAlpha){
            next.scoreTypeEnum = ScoreTypeEnum.UPPER;
        }else if(bestScore >= storedBeta){
            next.scoreTypeEnum = ScoreTypeEnum.LOWER ;
        }else{
            next.scoreTypeEnum = ScoreTypeEnum.EXACT;
        }
        transpositionsMap.put(state.hashCode(), next);

        return bestScore;
    }

    private ArrayList<MoveData> sortPossibleMovesByScore(SortOrderEnum sortOrder, GameHandler state, Player maximzingPlayer)
    {
        ArrayList<MoveData> newPossibleMoves = new ArrayList<>();
        for (PossibleMove possibleMove : state.allPossibleMovesFromNode(state.ballNode()))
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

        if (state.isGameOver() && state.getWinner(state.ballNode()).winner == maximizingPlayer) score = 1000;
        if (state.isGameOver() && state.getWinner(state.ballNode()).winner != maximizingPlayer) score = -1000;

        score += -state.numberOfTurns;

        Node opponentsGoal = state.getOpponent(maximizingPlayer).goalNode;
        score += -MathHelper.distance(opponentsGoal.xCord, state.ballNode().xCord, opponentsGoal.yCord, state.ballNode().yCord);

        Node myGoal = maximizingPlayer.goalNode;

        if (MathHelper.distance(opponentsGoal.xCord, state.ballNode().xCord, opponentsGoal.yCord, state.ballNode().yCord) == 1 &&
                state.ballNode().nodeType == NodeTypeEnum.Wall &&
                state.currentPlayersTurn == maximizingPlayer) score = 1000;

        if (MathHelper.distance(myGoal.xCord, state.ballNode().xCord, myGoal.yCord, state.ballNode().yCord) == 1 &&
                state.ballNode().nodeType == NodeTypeEnum.Wall &&
                state.currentPlayersTurn != maximizingPlayer) score = -1000;

        return score;
    }
}
