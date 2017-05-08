package com.ps.simplepapersoccer.GameObjects.Move;

import com.ps.simplepapersoccer.GameObjects.Game.Node;
import com.ps.simplepapersoccer.GameObjects.Player;

public class PartialMove
{
    public PartialMove(Node oldNode, Node newNode, Player madeTheMove)
    {
        this.oldNode = oldNode;
        this.newNode = newNode;
        this.madeTheMove = madeTheMove;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object.getClass() != getClass()) return false;
        PartialMove other = (PartialMove)object;
        if (!oldNode.id.equals(other.oldNode.id)) return false;
        if (!newNode.id.equals(other.newNode.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return oldNode.id.hashCode() ^ newNode.id.hashCode();
    }

    public Node oldNode;
    public Node newNode;
    public Player madeTheMove;
}
