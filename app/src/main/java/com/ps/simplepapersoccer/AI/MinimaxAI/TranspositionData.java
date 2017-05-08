package com.ps.simplepapersoccer.AI.MinimaxAI;

/**
 * Created by Admin on 2017-05-08.
 */

public class TranspositionData
{
    public int depth;
    public double score;
    public ScoreTypeEnum scoreTypeEnum;

    public TranspositionData() {};

    public TranspositionData(int depth, double score, ScoreTypeEnum scoreTypeEnum) {
        this.depth = depth;
        this.score = score;
        this.scoreTypeEnum = scoreTypeEnum;
    }
}
