package com.ps.simplepapersoccer.GameObjects.Player.Abstraction

import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Goal


abstract class IPlayer(var playerName: String, var playerNumber: Int, var playerColor: Int, var isAi: Boolean) {
    var goal: Goal? = null
    var score: Int = 0
}
