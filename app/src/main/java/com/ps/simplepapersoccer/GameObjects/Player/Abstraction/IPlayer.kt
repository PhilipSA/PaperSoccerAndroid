package com.ps.simplepapersoccer.gameObjects.Player.Abstraction

import com.ps.simplepapersoccer.gameObjects.Game.Geometry.Goal


abstract class IPlayer(var playerName: String, var playerNumber: Int, var playerColor: Int, var isAi: Boolean) {
    var goal: Goal? = null
    var score: Int = 0
}
