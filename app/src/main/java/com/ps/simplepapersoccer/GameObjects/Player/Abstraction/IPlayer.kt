package com.ps.simplepapersoccer.gameobjects.player.abstraction

import com.ps.simplepapersoccer.gameobjects.game.geometry.Goal


abstract class IPlayer(var playerName: String,
                       var playerNumber: Int,
                       var playerColor: Int,
                       var isAi: Boolean) {
    var goal: Goal? = null
    var score: Int = 0

    override fun toString(): String {
        return playerName
    }
}
