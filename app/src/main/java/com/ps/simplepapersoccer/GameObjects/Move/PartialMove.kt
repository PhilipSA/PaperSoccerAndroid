package com.ps.simplepapersoccer.GameObjects.Move

import com.ps.simplepapersoccer.GameObjects.Game.Node
import com.ps.simplepapersoccer.GameObjects.Player

class PartialMove(var oldNode: Node, var newNode: Node, var madeTheMove: Player) {

    override fun equals(`object`: Any?): Boolean {
        if (`object` == null) return false
        if (`object`.javaClass != javaClass) return false
        val other = `object` as PartialMove?
        if (oldNode.id != other!!.oldNode.id) return false
        if (newNode.id != other.newNode.id) return false
        return true
    }

    override fun hashCode(): Int {
        return oldNode.id.hashCode() xor newNode.id.hashCode()
    }
}
