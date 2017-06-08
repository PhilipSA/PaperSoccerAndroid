package com.ps.simplepapersoccer.GameObjects.Move

import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.GameObjects.Player.Abstraction.IPlayer

class PartialMove(var oldNode: Node, var newNode: Node, var madeTheMove: IPlayer)
