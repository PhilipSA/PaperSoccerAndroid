package com.ps.simplepapersoccer.GameObjects.Game;

import com.ps.simplepapersoccer.Enums.VictoryConditionEnum;
import com.ps.simplepapersoccer.GameObjects.Player;

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
