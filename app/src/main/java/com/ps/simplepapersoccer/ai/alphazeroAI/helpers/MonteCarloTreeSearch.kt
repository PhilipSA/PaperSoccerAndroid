import com.ps.simplepapersoccer.ai.alphazeroAI.helpers.Tree
import com.ps.simplepapersoccer.ai.alphazeroAI.helpers.UCT
import com.ps.simplepapersoccer.gameobjects.game.GameBoard
import com.ps.simplepapersoccer.gameobjects.move.PartialMove

class MonteCarloTreeSearch {
    var level = 3
    private var opponent = 0

    private val millisForCurrentLevel: Int
        get() = 2 * (level - 1) + 1

    fun findNextMove(gameBoard: GameBoard): PartialMove {
        val start = System.currentTimeMillis()
        val end = start + 60 * millisForCurrentLevel
        val tree = Tree(gameBoard)
        val rootNode = tree.root
        rootNode.state.board = gameBoard
        while (System.currentTimeMillis() < end) {
            // Phase 1 - Selection
            val promisingNode: MonteCarloNode = selectPromisingNode(rootNode)
            // Phase 2 - Expansion
            if (promisingNode.state.board.isGameOver.not()) expandNode(promisingNode)

            // Phase 3 - Simulation
            var nodeToExplore: MonteCarloNode = promisingNode
            if (promisingNode.childArray.size > 0) {
                nodeToExplore = promisingNode.randomChildNode
            }
            val playoutResult = simulateRandomPlayout(nodeToExplore)
            // Phase 4 - Update
            backPropogation(nodeToExplore, playoutResult)
        }
        val winnerNode = rootNode.childWithMaxScore
        tree.root = winnerNode!!
        return winnerNode.state.move
    }

    private fun selectPromisingNode(rootNode: MonteCarloNode): MonteCarloNode {
        var node = rootNode
        while (node.childArray.size != 0) {
            node = UCT.findBestNodeWithUCT(node)
        }
        return node
    }

    private fun expandNode(node: MonteCarloNode) {
        val possibleStates = node.state.allPossibleStates
        possibleStates.forEach { state ->
            state.board.makePartialMove(state.move)
            val newNode = MonteCarloNode(state)
            newNode.parent = node
            node.childArray.add(newNode)
            state.board.undoLastMove()
        }
    }

    private fun backPropogation(nodeToExplore: MonteCarloNode, playerNo: Int) {
        var tempNode: MonteCarloNode? = nodeToExplore
        while (tempNode != null) {
            tempNode.state.incrementVisit()
            if (tempNode.state.playerNo == playerNo) tempNode.state.addScore(WIN_SCORE)
            tempNode = tempNode.parent
        }
    }

    private fun simulateRandomPlayout(node: MonteCarloNode): Int {
        val tempNode = MonteCarloNode(node)
        val tempState = tempNode.state
        var boardStatus = tempState.board.currentPlayersTurn
        if (boardStatus == opponent) {
            tempNode.parent?.state?.winScore = Double.MIN_VALUE
            return boardStatus
        }

        var numberOfMoves = 0

        while (tempState.board.isGameOver.not()) {
            tempState.randomPlay()
            ++numberOfMoves
            boardStatus = tempState.board.currentPlayersTurn
        }

        for (x in 1..numberOfMoves) {
            tempState.board.undoLastMove()
        }

        return boardStatus
    }

    companion object {
        private const val WIN_SCORE = 10.0
    }

}