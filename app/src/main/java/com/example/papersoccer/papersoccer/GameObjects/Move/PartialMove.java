package com.example.papersoccer.papersoccer.GameObjects.Move;

import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;

public class PartialMove
{
    public PartialMove(Node oldNode, Node newNode)
    {
        this.oldNode = oldNode;
        this.newNode = newNode;
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
