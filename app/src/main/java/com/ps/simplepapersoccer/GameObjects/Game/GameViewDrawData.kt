package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.GameObjects.Player

class GameViewDrawData {
    var drawLine: LinesToDraw? = null
    var playerTurn: Player? = null
    var ballNodeX: Int = 0
    var ballNodeY: Int = 0

    constructor(drawLine: LinesToDraw?, playerTurn: Player?, ballNodeX: Int, ballNodeY: Int) {
        this.drawLine = drawLine
        this.playerTurn = playerTurn
        this.ballNodeX = ballNodeX
        this.ballNodeY = ballNodeY
    }
}
