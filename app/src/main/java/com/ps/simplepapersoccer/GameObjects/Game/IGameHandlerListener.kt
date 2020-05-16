package com.ps.simplepapersoccer.gameObjects.game

import androidx.lifecycle.MutableLiveData
import com.ps.simplepapersoccer.gameObjects.move.PartialMove

interface IGameHandlerListener {
    val winnerLiveData: MutableLiveData<Victory>
    val reDrawLiveData: MutableLiveData<Boolean>
    val drawPartialMoveLiveData: MutableLiveData<PartialMove>
}