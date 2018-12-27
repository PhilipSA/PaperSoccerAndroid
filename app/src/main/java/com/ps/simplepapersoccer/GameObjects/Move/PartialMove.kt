package com.ps.simplepapersoccer.gameObjects.move

import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.player.abstraction.IPlayer

data class PartialMove(var oldNode: Node, var newNode: Node, var madeTheMove: IPlayer)
