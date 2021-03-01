package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer

data class Victory(val winner: IPlayer, val victoryConditionEnum: VictoryConditionEnum)
