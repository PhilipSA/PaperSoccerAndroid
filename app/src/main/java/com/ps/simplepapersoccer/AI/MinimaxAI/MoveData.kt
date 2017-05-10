package com.ps.simplepapersoccer.AI.MinimaxAI

import com.ps.simplepapersoccer.GameObjects.Move.PartialMove

class MoveData : Comparable<MoveData> {
    var returnValue: Double = 0.toDouble()
    var returnMove: PartialMove? = null
    var depth: Int = 0

    constructor(returnValue: Double) {
        this.returnValue = returnValue
    }

    constructor(returnMove: PartialMove, depth: Int) {
        this.returnMove = returnMove
        this.depth = depth
    }

    override fun compareTo(item: MoveData): Int {
        if (this.returnValue < item.returnValue) {
            return -1
        } else if (this.returnValue > item.returnValue) {
            return 1
        }

        return 0
    }

    override fun equals(`object`: Any?): Boolean {
        if (`object` == null) return false
        if (`object`.javaClass != javaClass) return false
        val other = `object` as MoveData?
        if (returnMove != other!!.returnMove) return false
        if (depth == other.depth) return false
        return true
    }

    override fun hashCode(): Int {
        return returnMove?.hashCode()?.xor(depth) as Int
    }
}