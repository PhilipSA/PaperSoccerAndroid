package com.ps.simplepapersoccer.helpers

import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import kotlin.collections.HashSet

object PathFindingHelper {
    fun findPathAstar(startNode: Node, goalNode: Node): HashSet<Node> {
        val path = HashSet<Node>()
        val frontier = hashSetOf(startNode)
        val explored = HashSet<Node>()
        while (frontier.isNotEmpty()) {
            val node = frontier.minBy { x -> x.nodeValue }!!
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
            for (nextNode in node.openConnectionNodes) {
                val costSoFar = node.costSoFar + MathHelper.euclideanDistance(node.coords, nextNode.coords)

                if (explored.contains(nextNode)) {
                    continue
                }
                if (frontier.add(nextNode).not() && costSoFar >= nextNode.costSoFar) continue

                nextNode.cameFrom = node
                nextNode.costSoFar = costSoFar
                nextNode.nodeValue = nextNode.costSoFar + MathHelper.euclideanDistance(nextNode.coords, goalNode.coords)
            }
        }
        return path
    }

    fun findPathGreedyBestFirstSearchBiDirectional(startNode: Node, goalNode: Node): HashSet<Node> {
        val priorityQueueStart = hashSetOf(startNode)
        val visitedStart = HashSet<Node>()

        val priorityQueueEnd = hashSetOf(goalNode)
        val visitedEnd = HashSet<Node>()

        val path = HashSet<Node>()

        while (priorityQueueStart.isNotEmpty() || priorityQueueEnd.isNotEmpty()) {

            if (priorityQueueStart.isNotEmpty()) {
                val startEndNode = bestFirstSearchPathFinder(priorityQueueStart, visitedStart, visitedEnd, goalNode)
                if (startEndNode != null) {
                    path.addAll(visitedStart)
                    path.addAll(visitedEnd)
                    return path
                }
            }

            if (priorityQueueEnd.isNotEmpty()) {
                val endEndNode = bestFirstSearchPathFinder(priorityQueueEnd, visitedEnd, visitedStart, startNode)
                if (endEndNode != null) {
                    path.addAll(visitedStart)
                    path.addAll(visitedEnd)
                    return path
                }
            }
        }
        return path
    }

    private fun bestFirstSearchPathFinder(priorityQueue: HashSet<Node>, visitedCurrentSide: HashSet<Node>, visitedOtherSide: HashSet<Node>, endNode: Node): Node? {
        val node = priorityQueue.minBy { x -> x.nodeValue }!!
        priorityQueue.remove(node)

        if (node == endNode || visitedOtherSide.contains(node)) {
            return node
        }
        visitedCurrentSide.add(node)
        for (nextNode in node.openConnectionNodes) {
            val costSoFar = MathHelper.euclideanDistance(node.coords, nextNode.coords)

            if (visitedCurrentSide.contains(nextNode)) continue
            if (priorityQueue.add(nextNode).not() && costSoFar >= nextNode.costSoFar) continue

            nextNode.cameFrom = node
            nextNode.nodeValue = MathHelper.euclideanDistance(nextNode.coords, endNode.coords)
        }

        return null
    }
}