package com.ps.simplepapersoccer.gameObjects.Move

import com.ps.simplepapersoccer.gameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.gameObjects.Player.Abstraction.IPlayer

class PartialMove(var oldNode: Node, var newNode: Node, var madeTheMove: IPlayer)
