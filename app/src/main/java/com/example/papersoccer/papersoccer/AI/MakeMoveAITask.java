package com.example.papersoccer.papersoccer.AI;

import android.os.AsyncTask;

import com.example.papersoccer.papersoccer.AI.Abstraction.IGameAI;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;

public class MakeMoveAITask extends AsyncTask<Object, Void, Move>
{
    private IGameAI iGameAI;
    private GameHandler gameHandler;

    MakeMoveAITask(IGameAI iGameAI, GameHandler gameHandler)
    {
        this.iGameAI = iGameAI;
        this.gameHandler = gameHandler;
    }

    @Override
    protected Move doInBackground(Object... params) {
        return iGameAI.MakeMove(gameHandler);
    }

    protected void onPostExecute(Move result) {
        gameHandler.ProgressGame(result);
    }

}
