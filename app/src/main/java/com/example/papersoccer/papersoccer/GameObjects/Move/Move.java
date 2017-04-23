package com.example.papersoccer.papersoccer.GameObjects.Move;

import com.example.papersoccer.papersoccer.Enums.NodeTypeEnum;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;

import java.util.ArrayList;

public class Move
{
    public ArrayList<PartialMove> Moves = new ArrayList<>();
    public boolean completedMove;
    public Player madeTheMove;

    public Move(Player madeTheMove)
    {
        this.madeTheMove = madeTheMove;
    }

    public Move(Move move)
    {
        Moves = new ArrayList<>(move.Moves);
    }

    public Move(PartialMove partialMove)
    {
        Moves.add(partialMove);
        isFinished();
    }

    private void isFinished()
    {
        completedMove = endNode().nodeType == NodeTypeEnum.Empty;
    }

    public Node endNode()
    {
        return Moves.get(Moves.size() - 1).newNode;
    }

    public void addPartialMove(PartialMove partialMove)
    {
        Moves.add(partialMove);
        isFinished();
    }
}
