package com.ps.simplepapersoccer.GameObjects

import com.ps.simplepapersoccer.GameObjects.Game.Node

class Player(var playerName: String, var playerNumber: Int, var playerColor: Int, var isAi: Boolean) {
    var goalNode: Node? = null
    var score: Int = 0
}
