import com.ps.simplepapersoccer.ai.alphazeroAI.helpers.State
import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import kotlin.collections.ArrayList

class MonteCarloNode {
    var state: State
    var parent: MonteCarloNode? = null
    var childArray: MutableList<MonteCarloNode>

    constructor(gameBoard: GameBoard) {
        state = State(gameBoard)
        childArray = ArrayList()
    }

    constructor(state: State) {
        this.state = state
        childArray = ArrayList()
    }

    constructor(state: State, parent: MonteCarloNode?, childArray: MutableList<MonteCarloNode>) {
        this.state = state
        this.parent = parent
        this.childArray = childArray
    }

    constructor(node: MonteCarloNode) {
        childArray = ArrayList()
        state = State(node.state)
        if (node.parent != null) parent = node.parent
        val childArray = node.childArray
        for (child in childArray) {
            this.childArray.add(MonteCarloNode(child))
        }
    }

    val randomChildNode: MonteCarloNode
        get() {
            val noOfPossibleMoves = childArray.size
            val selectRandom = (Math.random() * noOfPossibleMoves).toInt()
            return childArray[selectRandom]
        }

    val childWithMaxScore: MonteCarloNode?
        get() = childArray.maxBy { it.state.visitCount }
}