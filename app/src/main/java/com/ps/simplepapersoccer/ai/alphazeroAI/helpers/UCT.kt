package com.ps.simplepapersoccer.ai.alphazeroAI.helpers

import MonteCarloNode
import kotlin.math.ln
import kotlin.math.sqrt

object UCT {
    fun uctValue(totalVisit: Int, nodeWinScore: Double, nodeVisit: Int): Double {
        return if (nodeVisit == 0) {
            Int.MAX_VALUE.toDouble()
        } else nodeWinScore / nodeVisit.toDouble() + 1.41 * sqrt(ln(totalVisit.toDouble()) / nodeVisit.toDouble())
    }

    fun findBestNodeWithUCT(node: MonteCarloNode): MonteCarloNode {
        val parentVisit: Int = node.state.visitCount
        return node.childArray.maxBy { uctValue(parentVisit, it.state.winScore, it.state.visitCount) }!!
    }
}