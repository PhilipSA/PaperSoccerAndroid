package com.ps.simplepapersoccer.ai.jonasAI

import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.gameObjects.move.PartialMove
import java.util.*

data class MoveSequence(val moveList: LinkedList<PartialMove>, val endNode: Node, var originIdentifier: UUID, var goal: Boolean)