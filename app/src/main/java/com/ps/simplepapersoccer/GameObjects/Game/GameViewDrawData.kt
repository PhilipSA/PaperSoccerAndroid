package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.GameObjects.Player

class GameViewDrawData {
    var drawLine: LinesToDraw? = null
    var playerTurn: Player? = null
    var ballNode: Node? = null

    constructor(drawLine: LinesToDraw?, playerTurn: Player?, ballNode: Node?) {
        this.drawLine = drawLine
        this.playerTurn = playerTurn
        this.ballNode = ballNode
    }
}
