package com.ps.simplepapersoccer.gameobjects.game

import androidx.lifecycle.MutableLiveData
import com.ps.simplepapersoccer.gameobjects.game.Victory
import com.ps.simplepapersoccer.gameobjects.move.PartialMove

interface IGameHandlerListener {
    fun winner(victory: Victory)
    fun reDraw()
    suspend fun drawPartialMove(partialMove: PartialMove): Boolean
}