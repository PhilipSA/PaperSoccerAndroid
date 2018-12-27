package com.ps.simplepapersoccer.gameObjects.game

import com.ps.simplepapersoccer.gameObjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer

data class GameViewDrawData(var drawLine: LinesToDraw?, var madeTheMove: IPlayer, var currentPlayerTurn: IPlayer, var ballNode: Node, var nodeNeighbors: HashSet<Node>)
