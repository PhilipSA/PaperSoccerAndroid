package com.ps.simplepapersoccer.gameObjects.Game

import com.ps.simplepapersoccer.gameObjects.Game.Geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer

class GameViewDrawData {
    var drawLine: LinesToDraw? = null
    var madeTheMove: IPlayer? = null
    var currentPlayerTurn: IPlayer? = null
    var ballNode: Node? = null
    var nodeNeighbors: MutableList<Node> = mutableListOf()

    constructor(drawLine: LinesToDraw?, madeTheMove: IPlayer?, currentPlayerTurn: IPlayer?, ballNode: Node?, nodeNeighbors: MutableList<Node>) {
        this.drawLine = drawLine
        this.madeTheMove = madeTheMove
        this.currentPlayerTurn = currentPlayerTurn
        this.ballNode = ballNode
        this.nodeNeighbors = nodeNeighbors
    }
}
