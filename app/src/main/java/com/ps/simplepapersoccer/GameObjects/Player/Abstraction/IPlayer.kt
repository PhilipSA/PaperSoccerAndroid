package com.ps.simplepapersoccer.GameObjects.Player.Abstraction

import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node

abstract class IPlayer(var playerName: String, var playerNumber: Int, var playerColor: Int, var isAi: Boolean) {
    var goalNode: Node? = null
    var score: Int = 0
}
