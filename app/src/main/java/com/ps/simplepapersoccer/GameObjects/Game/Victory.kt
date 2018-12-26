package com.ps.simplepapersoccer.gameObjects.Game

import com.ps.simplepapersoccer.enums.VictoryConditionEnum
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer

class Victory(var winner: IPlayer, var victoryConditionEnum: VictoryConditionEnum)
