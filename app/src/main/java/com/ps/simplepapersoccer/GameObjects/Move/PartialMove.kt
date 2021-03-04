package com.ps.simplepapersoccer.gameobjects.move

import com.ps.simplepapersoccer.gameobjects.game.geometry.Node

data class PartialMove(var oldNode: Node, var newNode: Node, var madeTheMove: Int) {
    constructor(possibleMove: PossibleMove, madeTheMove: Int): this(possibleMove.oldNode, possibleMove.newNode, madeTheMove)
}
