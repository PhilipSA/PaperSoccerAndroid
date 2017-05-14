package com.ps.simplepapersoccer.GameObjects.Game

import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Move.PossibleMove
import com.ps.simplepapersoccer.Helpers.MathHelper

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.UUID

class GameBoard(gridSizeX: Int, gridSizeY: Int) {
    var nodeHashMap = HashMap<UUID, Node>()

    var ballNode: Node? = null

    var goalNode1: Node? = null
    var goalNode2: Node? = null

    private val allPartialMoves = ArrayList<PartialMove>()

    override fun hashCode(): Int {
        return allPartialMoves.hashCode() xor nodeHashMap.hashCode() xor ballNode?.hashCode() as Int
    }

    init {
        makeNodes(gridSizeX, gridSizeY)
        ballNode = findNodeByXY(gridSizeX / 2, gridSizeY / 2) as Node
    }

    private fun makeNodes(gridSizeX: Int, gridSizeY: Int) {
        for (y in 1..gridSizeY - 1) {
            for (x in 0..gridSizeX) {
                //No node in the 4 cornes
                if (x == 0 && y == 1) continue
                if (x == 0 && y == gridSizeY - 1) continue
                if (x == gridSizeX && y == 1) continue
                if (x == gridSizeX && y == gridSizeY - 1) continue

                //Check if wall
                if (x == 0 || x == gridSizeX)
                    addNodeToNodeMap(Node(x, y, NodeTypeEnum.Wall))
                else if (x != gridSizeX / 2 && y == 1) {
                    addNodeToNodeMap(Node(x, y, NodeTypeEnum.Wall))
                } else if (x != gridSizeX / 2 && y == gridSizeY - 1) {
                    addNodeToNodeMap(Node(x, y, NodeTypeEnum.Wall))
                } else {
                    addNodeToNodeMap(Node(x, y, NodeTypeEnum.Empty))
                }//Regular empty node
                //Check if bottom wall special case
                //Check if top wall special case
            }
        }
        //Make the 2 goal nodes
        goalNode1 = Node(gridSizeX / 2, gridSizeY, NodeTypeEnum.Goal)
        addNodeToNodeMap(goalNode1 as Node)

        goalNode2 = Node(gridSizeX / 2, 0, NodeTypeEnum.Goal)
        addNodeToNodeMap(goalNode2 as Node)


        GenerateAllNeighbors()
    }

    fun GenerateAllNeighbors() {
        for (node in nodeHashMap.values) {
            for (otherNode in nodeHashMap.values) {
                if (node.id === otherNode.id) continue

                val euclideanDistance = MathHelper.euclideanDistance(node.xCord, otherNode.xCord, node.yCord, otherNode.yCord)

                if (node.nodeType == NodeTypeEnum.Wall && otherNode.nodeType == NodeTypeEnum.Wall) {
                    if (node.yCord != otherNode.yCord && otherNode.xCord != node.xCord && euclideanDistance < 2) {
                        node.AddNeighborPair(otherNode)
                    } else {
                        continue
                    }
                }
                if (euclideanDistance < 2) node.AddNeighborPair(otherNode)
            }
        }
    }

    fun allPossibleMovesFromNode(node: Node): HashSet<PossibleMove> {
        val possibleMoves = HashSet<PossibleMove>()

        for (uuid in node.neighbors) {
            val otherNode = nodeHashMap[uuid]
            possibleMoves.add(PossibleMove(node, otherNode!!))
        }

        return possibleMoves
    }

    fun UndoLastMove(): PartialMove {
        val partialMove = allPartialMoves.last()

        partialMove.newNode.AddNeighborPair(partialMove.oldNode)

        nodeHashMap[partialMove.newNode.id]?.nodeType = partialMove.newNode.nodeType

        ballNode = partialMove.oldNode

        allPartialMoves.remove(partialMove)

        return partialMove
    }

    fun MakePartialMove(partialMove: PartialMove) {
        allPartialMoves.add(PartialMove(Node(partialMove.oldNode), Node(partialMove.newNode), partialMove.madeTheMove))

        partialMove.newNode.RemoveNeighborPair(partialMove.oldNode)

        if (partialMove.oldNode.nodeType == NodeTypeEnum.Empty)
            partialMove.oldNode.nodeType = NodeTypeEnum.BounceAble

        ballNode = partialMove.newNode
    }

    //Returns the node with the XY coordinates
    fun findNodeByXY(x: Int, y: Int): Node? {
        for (n in nodeHashMap.values) {
            if (n.xCord == x && n.yCord == y)
                return n
        }
        return null
    }

    fun addNodeToNodeMap(node: Node) {
        nodeHashMap.put(node.id, node)
    }
}
