package com.ps.simplepapersoccer.GameObjects;

import android.widget.TextView;

/**
 * Created by Admin on 2017-04-25.
 */

public class PlayerActivityData
{
    public TextView playerNameTextView;
    public TextView playerScoreTextView;

    public PlayerActivityData(TextView playerNameTextView, TextView playerScoreTextView) {
        this.playerNameTextView = playerNameTextView;
        this.playerScoreTextView = playerScoreTextView;
    }
}
