package com.ps.simplepapersoccer.gameobjects.game

import androidx.lifecycle.MutableLiveData
import com.ps.simplepapersoccer.gameobjects.game.Victory
import com.ps.simplepapersoccer.gameobjects.move.PartialMove

interface IGameHandlerListener {
    val winnerLiveData: MutableLiveData<Victory>
    val reDrawLiveData: MutableLiveData<Boolean>
    val drawPartialMoveLiveData: MutableLiveData<PartialMove>
}