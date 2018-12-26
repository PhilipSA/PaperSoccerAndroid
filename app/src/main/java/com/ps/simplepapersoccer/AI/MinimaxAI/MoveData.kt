package com.ps.simplepapersoccer.ai.MinimaxAI

import com.ps.simplepapersoccer.gameObjects.Move.PartialMove

class MoveData(var returnValue: Double) : Comparable<MoveData> {
    var returnMove: PartialMove? = null
    var depth: Int = 0

    override fun compareTo(other: MoveData): Int {
        if (this.returnValue < other.returnValue) {
            return -1
        } else if (this.returnValue > other.returnValue) {
            return 1
        }

        return 0
    }
}