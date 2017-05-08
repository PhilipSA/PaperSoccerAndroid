package com.ps.simplepapersoccer.GameObjects.Game;

import com.google.common.collect.Iterables;
import com.ps.simplepapersoccer.Enums.NodeTypeEnum;
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove;
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove;
import com.ps.simplepapersoccer.Helpers.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GameBoard
{
    public HashMap<UUID, Node> nodeHashMap = new HashMap<>();

    public Node ballNode;

    public Node goalNode1;
    public Node goalNode2;

    private ArrayList<PartialMove> allPartialMoves = new ArrayList<>();

    @Override
    public int hashCode() {
        return allPartialMoves.hashCode() ^ nodeHashMap.hashCode() ^ ballNode.hashCode();
    }

    public GameBoard (int gridSizeX, int gridSizeY)
    {
        makeNodes(gridSizeX, gridSizeY);
        ballNode = findNodeByXY(gridSizeX/2, gridSizeY/2);
    }

    private void makeNodes(int gridSizeX, int gridSizeY)
    {
        for (int y = 1; y < gridSizeY; ++y)
        {
            for (int x = 0; x <= gridSizeX; ++x)
            {
                //No node in the 4 cornes
                if (x == 0 && y == 1) continue;
                if (x == 0 && y == gridSizeY - 1) continue;
                if (x == gridSizeX && y == 1) continue;
                if (x == gridSizeX && y == gridSizeY - 1) continue;

                //Check if wall
                if (x == 0 || x == gridSizeX )
                    addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));

                    //Check if top wall special case
                else if (x != gridSizeX / 2 && y == 1)
                {
                    addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));
                }

                //Check if bottom wall special case
                else if (x != gridSizeX / 2 && y == gridSizeY - 1)
                {
                    addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Wall));
                }

                //Regular empty node
                else
                {
                    addNodeToNodeMap(new Node(x,y, NodeTypeEnum.Empty));
                }
            }
        }
        //Make the 2 goal nodes
        goalNode1 = new Node(gridSizeX / 2, gridSizeY, NodeTypeEnum.Goal);
        addNodeToNodeMap(goalNode1);

        goalNode2 = new Node(gridSizeX / 2, 0, NodeTypeEnum.Goal);
        addNodeToNodeMap(goalNode2);


        GenerateAllNeighbors();
    }

    public void GenerateAllNeighbors()
    {
        for (Node node : nodeHashMap.values()) {
            for (Node otherNode : nodeHashMap.values()) {
                if (node.id == otherNode.id) continue;

                double euclideanDistance = MathHelper.euclideanDistance(node.xCord, otherNode.xCord, node.yCord, otherNode.yCord);

                if (node.nodeType == NodeTypeEnum.Wall && otherNode.nodeType == NodeTypeEnum.Wall) {
                    if (node.yCord != otherNode.yCord && otherNode.xCord != node.xCord && euclideanDistance < 2) {
                        node.AddNeighborPair(otherNode);
                    } else {
                        continue;
                    }
                }
                if (euclideanDistance < 2) node.AddNeighborPair(otherNode);
            }
        }
    }

    public HashSet<PossibleMove> allPossibleMovesFromNode(Node node)
    {
        HashSet<PossibleMove> possibleMoves = new HashSet<>();

        for (UUID uuid : node.neighbors) {
            Node otherNode = nodeHashMap.get(uuid);
            possibleMoves.add(new PossibleMove(node, otherNode));
        }

        return possibleMoves;
    }

    public PartialMove UndoLastMove()
    {
        PartialMove partialMove = Iterables.getLast(allPartialMoves);

        partialMove.newNode.AddNeighborPair(partialMove.oldNode);

        nodeHashMap.get(partialMove.newNode.id).nodeType = partialMove.newNode.nodeType;

        ballNode = partialMove.oldNode;

        allPartialMoves.remove(partialMove);

        return partialMove;
    }

    public void MakePartialMove(PartialMove partialMove)
    {
        allPartialMoves.add(new PartialMove(new Node(partialMove.oldNode), new Node(partialMove.newNode), partialMove.madeTheMove));

        partialMove.newNode.RemoveNeighborPair(partialMove.oldNode);

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble;

        ballNode = partialMove.newNode;
    }

    //Returns the node with the XY coordinates
    public Node findNodeByXY(int x, int y)
    {
        for (Node n : nodeHashMap.values())
        {
            if (n.xCord == x && n.yCord == y)
                return n;
        }
        return null;
    }

    public void addNodeToNodeMap(Node node)
    {
        nodeHashMap.put(node.id, node);
    }
}
