package com.ps.simplepapersoccer.ai.alphazeroAI.helpers

import MonteCarloNode
import com.ps.simplepapersoccer.gameObjects.game.GameBoard

class Tree {
    var root: MonteCarloNode

    constructor(gameBoard: GameBoard) {
        root = MonteCarloNode(gameBoard)
    }

    constructor(root: MonteCarloNode) {
        this.root = root
    }

    fun addChild(parent: MonteCarloNode, child: MonteCarloNode) {
        parent.childArray.add(child)
    }
}