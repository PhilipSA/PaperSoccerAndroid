package com.ps.simplepapersoccer.AI

import android.os.AsyncTask

import com.ps.simplepapersoccer.AI.Abstraction.IGameAI
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove

class MakeMoveAITask internal constructor(private val iGameAI: IGameAI, private val gameHandler: GameHandler) : AsyncTask<Any, Void, PartialMove>() {

    override fun doInBackground(vararg params: Any): PartialMove {
        return iGameAI.MakeMove(gameHandler)
    }

    override fun onPostExecute(result: PartialMove) {
        gameHandler.AIMakeMove(result)
    }

}
