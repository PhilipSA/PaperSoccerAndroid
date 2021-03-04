package com.ps.simplepapersoccer.gameobjects.game

import com.ps.simplepapersoccer.gameobjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameobjects.game.geometry.Node
import com.ps.simplepapersoccer.gameobjects.player.abstraction.IPlayer

data class GameViewDrawData(var drawLine: LinesToDraw?, var madeTheMove: IPlayer, var currentPlayerTurn: IPlayer, var ballNode: Node, var nodeNeighbors: HashSet<Node>)
