package com.example.papersoccer.papersoccer.GameObjects;

public class Move
{
    public Move(Node oldNode, Node newNode, Player madeTheMove)
    {
        this.oldNode = oldNode;
        this.newNode = newNode;
        this.madeTheMove = madeTheMove;
    }

    public Node oldNode;
    public Node newNode;
    public Player madeTheMove;
}
