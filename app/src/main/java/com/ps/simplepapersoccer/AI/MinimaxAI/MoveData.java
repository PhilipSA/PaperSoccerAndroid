package com.ps.simplepapersoccer.AI.MinimaxAI;

import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;

public class MoveData implements Comparable<MoveData>
{
    public double returnValue;
    public PartialMove returnMove;
    public int depth;

    public MoveData(double returnValue)
    {
        this.returnValue = returnValue;
    }

    public MoveData(PartialMove returnMove, int depth) {
        this.returnMove = returnMove;
        this.depth = depth;
    }

    @Override
    public int compareTo(MoveData item) {
        if (this.returnValue < item.returnValue) {
            return -1;
        }
        else if(this.returnValue > item.returnValue){
            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object.getClass() != getClass()) return false;
        MoveData other = (MoveData)object;
        if (!returnMove.equals(other.returnMove)) return false;
        if (!(depth != other.depth)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return returnMove.hashCode() ^ depth;
    }
}