package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer
import java.util.concurrent.CopyOnWriteArrayList

class GameViewDrawData {
    var drawLine: LinesToDraw? = null
    var madeTheMove: IPlayer? = null
    var ballNode: Node? = null
    var nodeNeighbors: CopyOnWriteArrayList<Node> = CopyOnWriteArrayList()

    constructor(drawLine: LinesToDraw?, madeTheMove: IPlayer?, ballNode: Node?, nodeNeighbors: CopyOnWriteArrayList<Node>) {
        this.drawLine = drawLine
        this.madeTheMove = madeTheMove
        this.ballNode = ballNode
        this.nodeNeighbors = nodeNeighbors
    }
}
