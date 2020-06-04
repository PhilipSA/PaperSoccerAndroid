import com.ps.simplepapersoccer.ai.alphazeroAI.helpers.Tree
import com.ps.simplepapersoccer.ai.alphazeroAI.helpers.UCT
import com.ps.simplepapersoccer.gameObjects.game.GameBoard

class MonteCarloTreeSearch {
    var level = 3
    private var opponent = 0

    private val millisForCurrentLevel: Int
        get() = 2 * (level - 1) + 1

    fun findNextMove(gameBoard: GameBoard, playerNo: Int): GameBoard {
        val start = System.currentTimeMillis()
        val end = start + 60 * millisForCurrentLevel
        opponent = 3 - playerNo
        val tree = Tree(gameBoard)
        val rootNode = tree.root
        rootNode.state.board = gameBoard
        rootNode.state.playerNo = opponent
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
        return winnerNode.state.board
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
            val newNode = MonteCarloNode(state)
            newNode.parent = node
            newNode.state.playerNo = node.state.opponent
            node.childArray.add(newNode)
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
        while (tempState.board.isGameOver.not()) {
            tempState.togglePlayer()
            tempState.randomPlay()
            boardStatus = tempState.board.currentPlayersTurn
        }
        return boardStatus
    }

    companion object {
        private const val WIN_SCORE = 10.0
    }

}