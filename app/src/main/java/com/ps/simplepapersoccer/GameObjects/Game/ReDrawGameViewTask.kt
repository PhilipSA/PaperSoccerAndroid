package com.ps.simplepapersoccer.GameObjects.Game

import android.os.AsyncTask
import com.ps.simplepapersoccer.Activities.GameActivity
import com.ps.simplepapersoccer.Activities.GameView

class ReDrawGameViewTask internal constructor(private val gameView: GameView, private val gameViewDrawData: GameViewDrawData, val activity: GameActivity) : AsyncTask<Any, Void, Void>() {
    override fun doInBackground(vararg params: Any): Void? {
        activity.runOnUiThread({
            gameView.drawAsync(gameViewDrawData)
        })
        Thread.sleep(200)
        return null
    }

    override fun onPostExecute(result: Void?) {
        if (activity.drawCallQueue?.size != 0) activity.drawCallQueue?.poll()?.execute()
        else {
            activity.drawTaskRunning = false
        }
    }
}
