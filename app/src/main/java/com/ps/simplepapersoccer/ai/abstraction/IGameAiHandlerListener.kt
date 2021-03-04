package com.ps.simplepapersoccer.ai.abstraction

import com.ps.simplepapersoccer.gameobjects.move.PartialMove

interface IGameAiHandlerListener {
    fun aiMove(partialMove: PartialMove?, timedOut: Boolean)
}