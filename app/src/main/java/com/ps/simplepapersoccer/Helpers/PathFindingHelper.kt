package com.ps.simplepapersoccer.helpers

import com.ps.simplepapersoccer.gameObjects.game.geometry.Node

object PathFindingHelper {
    fun findPath(startNode: Node, goalNode: Node): HashSet<Node> {
        val path = HashSet<Node>()
        val frontier = hashSetOf(startNode)
        val explored = HashSet<Node>()
        while (frontier.isNotEmpty()) {
            val node = frontier.sortedBy { x -> x.nodeValue }.first()
            frontier.remove(node)

            if (node == goalNode) {
                var backTrack = node
                while (backTrack != startNode) {
                    path.add(backTrack)
                    backTrack = backTrack.cameFrom!!
                }
                path.add(startNode)
                return path
            }
            explored.add(node)
            val nodeNeighbors = node.getNodeNeighbors()
            for (nextNode in nodeNeighbors) {
                val costSoFar = node.costSoFar + MathHelper.euclideanDistance(node.coords, nextNode.coords)

                if (explored.contains(nextNode)) {
                    continue
                }
                if (!frontier.contains(nextNode)) {
                    frontier.add(nextNode)
                }
                else if (costSoFar >= nextNode.costSoFar) continue

                nextNode.cameFrom = node
                nextNode.costSoFar = costSoFar
                nextNode.nodeValue = nextNode.costSoFar + MathHelper.euclideanDistance(nextNode.coords, goalNode.coords)
            }
        }
        return path
    }
}