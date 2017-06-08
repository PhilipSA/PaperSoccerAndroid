package com.ps.simplepapersoccer.AI.MinimaxAI

import com.ps.simplepapersoccer.GameObjects.Move.PartialMove

class MoveData : Comparable<MoveData> {
    var returnValue: Double = 0.toDouble()
    var returnMove: PartialMove? = null
    var depth: Int = 0

    constructor(returnValue: Double) {
        this.returnValue = returnValue
    }

    override fun compareTo(item: MoveData): Int {
        if (this.returnValue < item.returnValue) {
            return -1
        } else if (this.returnValue > item.returnValue) {
            return 1
        }

        return 0
    }
}