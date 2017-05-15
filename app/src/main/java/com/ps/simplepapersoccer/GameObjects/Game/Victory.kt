package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.Enums.VictoryConditionEnum
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer
import com.ps.simplepapersoccer.GameObjects.Player.Player

class Victory(var winner: IPlayer, var victoryConditionEnum: VictoryConditionEnum)
