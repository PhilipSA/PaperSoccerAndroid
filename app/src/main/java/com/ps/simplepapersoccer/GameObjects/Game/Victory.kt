package com.ps.simplepapersoccer.gameobjects.game

import com.ps.simplepapersoccer.data.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameobjects.player.abstraction.IPlayer

data class Victory(val winner: IPlayer, val victoryConditionEnum: VictoryConditionEnum)
