package com.ps.simplepapersoccer.gameobjects.move

import com.ps.simplepapersoccer.data.enums.NodeTypeEnum

data class StoredMove(val partialMove: PartialMove, val oldNodeTypeEnum: NodeTypeEnum, val newNodeTypeEnum: NodeTypeEnum)
