package com.ps.simplepapersoccer.ai.jonasAI

import com.ps.simplepapersoccer.gameobjects.game.geometry.Node
import com.ps.simplepapersoccer.gameobjects.move.PartialMove
import java.util.*

data class MoveSequence(val moveList: LinkedList<PartialMove>, val endNode: Node, var originIdentifier: UUID, var goal: Boolean)