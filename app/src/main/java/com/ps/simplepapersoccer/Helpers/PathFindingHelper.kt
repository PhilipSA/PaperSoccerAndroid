package com.ps.simplepapersoccer.Helpers

import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node

object PathFindingHelper {
    class pathFindingNode(val node: Node) {
        var cameFrom: pathFindingNode? = null
        var nodeValue: Int = 0
        var costSoFar: Int = 0
    }

    fun nodeToPathNodes(allNodes: List<Node>): List<pathFindingNode> {
        var newList: MutableList<pathFindingNode> = mutableListOf()
        return allNodes.mapTo(newList) { pathFindingNode(it) }
    }

    fun findPath(startNode: Node, goalNode: Node, allNodes: List<Node>): List<pathFindingNode> {
        var allPathNode = nodeToPathNodes(allNodes)
        var gScore = 0
        var path = mutableListOf<pathFindingNode>()
        var frontier = mutableListOf(pathFindingNode(startNode))
        var explored = mutableListOf<pathFindingNode>()
        while (frontier.isNotEmpty()) {
            frontier.sortBy { x -> x.nodeValue }
            var node = frontier.first()
            frontier.removeAt(0)

            if (node.node.nodeType == NodeTypeEnum.Goal) {
                var backTrack = node
                while (backTrack.node.id != startNode.id) {
                    path.add(backTrack)
                    backTrack = backTrack.cameFrom!!
                }
                path.add(pathFindingNode(startNode))
                return path
            }
            explored.add(node)
            for (nextNode in node.node.neighbors) {
                var next = allPathNode.find { x -> x.node.id === nextNode }!!
                gScore = node.costSoFar + MathHelper.euclideanDistance(next!!.node.coords, node.node.coords).toInt()

                if (explored.contains(next)) {
                    continue
                }
                if (!frontier.contains(next)) {
                    frontier.add(next)
                }
                else if (gScore >= next.costSoFar) continue

                next.cameFrom = node
                next.costSoFar = gScore
                next.nodeValue = next.costSoFar + MathHelper.euclideanDistance(next!!.node.coords, goalNode.coords).toInt()
            }
        }
        return path
    }
}