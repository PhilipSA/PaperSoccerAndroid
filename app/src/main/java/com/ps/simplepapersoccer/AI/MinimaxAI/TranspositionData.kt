package com.ps.simplepapersoccer.AI.MinimaxAI

/**
 * Created by Admin on 2017-05-08.
 */

class TranspositionData {
    var depth: Int = 0
    var score: Double = 0.toDouble()
    var scoreTypeEnum: ScoreTypeEnum? = null

    constructor() {}

    constructor(depth: Int, score: Double, scoreTypeEnum: ScoreTypeEnum) {
        this.depth = depth
        this.score = score
        this.scoreTypeEnum = scoreTypeEnum
    }
}
