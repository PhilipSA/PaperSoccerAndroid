package com.example.papersoccer.papersoccer.GameObjects;

import com.example.papersoccer.papersoccer.Enums.VictoryConditionEnum;

/**
 * Created by Admin on 2017-04-26.
 */

public class Victory
{
    public Player winner;
    public VictoryConditionEnum victoryConditionEnum;

    public Victory(Player winner, VictoryConditionEnum victoryConditionEnum) {
        this.winner = winner;
        this.victoryConditionEnum = victoryConditionEnum;
    }
}
