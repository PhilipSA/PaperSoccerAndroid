package com.ps.simplepapersoccer.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.ps.simplepapersoccer.gameObjects.game.geometry.TwoDimensionalPoint
import android.graphics.drawable.ScaleDrawable
import android.util.AttributeSet
import android.view.View
import com.ps.simplepapersoccer.data.enums.NodeTypeEnum
import com.ps.simplepapersoccer.gameObjects.game.GameBoard
import com.ps.simplepapersoccer.gameObjects.game.GameViewDrawData
import com.ps.simplepapersoccer.gameObjects.game.geometry.abstraction.IntegerLine
import com.ps.simplepapersoccer.gameObjects.game.geometry.LinesToDraw
import com.ps.simplepapersoccer.gameObjects.game.geometry.Node
import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.viewmodel.GameViewModel
import kotlin.collections.HashSet

class GameView : View {

    var drawLines: HashSet<LinesToDraw> = HashSet()

    var gameViewDrawData: GameViewDrawData? = null

    private var gameViewModel: GameViewModel? = null
    private var gameBoard: GameBoard? = null

    private var canvas: Canvas? = null
    private var paint = Paint()

    private var leftEdge = 50f
    private var topEdge = 100f

    private var gridXdraw: Float = 0.toFloat()
    private var gridYdraw: Float = 0.toFloat()

    var gridSizeX = 0
    var gridSizeY = 0

    private val gridStrokeWidth = 4
    private val gridColor = Color.rgb(242, 244, 247)

    private var leftEdgeMargin = 30f
    private var rightEdgeMargin = 1.027f

    private val ballSize = 100
    private val sideLineStrokeWidth = 8

    private var rightEdge: Float = 0.toFloat()
    private var bottomEdge: Float = 0.toFloat()

    private var middlePointX: Float = 0.toFloat()
    private var middlePointY: Float = 0.toFloat()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun init(screenWidth: Int, screenHeight: Int, gridSizeX: Int, gridSizeY: Int, gameViewModel: GameViewModel, gameBoard: GameBoard) {
        topEdge = (screenHeight / 9).toFloat()
        leftEdge = screenWidth / leftEdgeMargin

        this.gridSizeX = gridSizeX
        this.gridSizeY = gridSizeY

        bottomEdge = screenHeight / 1.2f
        rightEdge = screenWidth / rightEdgeMargin

        gridXdraw = (rightEdge - leftEdge) / gridSizeX
        gridYdraw = (bottomEdge - topEdge) / gridSizeY

        this.middlePointX = (rightEdge + leftEdge) / 2
        this.middlePointY = (bottomEdge + topEdge) / 2

        this.gameViewModel = gameViewModel
        this.gameBoard = gameBoard

        this.invalidate()
    }

    //Converts node coordinates to screen coordinates
    fun nodeToCoords(n: Node): FloatArray {
        val coords = FloatArray(2)
        coords[0] = n.getVisibleCoords.x * gridXdraw + leftEdge
        coords[1] = n.getVisibleCoords.y * gridYdraw + topEdge
        return coords
    }

    private fun pointsCoordsToCoords(point: TwoDimensionalPoint): FloatArray {
        val newCoords = FloatArray(2)
        newCoords[0] = point.x / 2 * gridXdraw + leftEdge
        newCoords[1] = point.y / 2 * gridYdraw + topEdge
        return newCoords
    }

    //Converts screen coordinates to node coordinates
    fun coordsToNode(argX: Float, argY: Float): FloatArray {
        val x = (argX + gridXdraw.div(2)) / gridXdraw * gridXdraw
        val y = (argY + gridYdraw.div(2)) / gridYdraw * gridYdraw - topEdge

        val coords = FloatArray(2)
        coords[0] = x / gridXdraw
        coords[1] = y / gridYdraw
        return coords
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        redrawMap()
    }

    fun drawAsync(gameViewDrawData: GameViewDrawData){
        drawLines.add(gameViewDrawData.drawLine!!)
        this.gameViewDrawData = gameViewDrawData
        gameViewModel?.updatePlayerTurnText()
        this.invalidate()
    }

    private fun redrawMap() {
        paint = Paint()
        paint.textSize = 40f
        paint.strokeWidth = gridStrokeWidth.toFloat()
        paintComponent(canvas!!, paint)
    }

    private fun paintComponent(canvas: Canvas, paint: Paint) {
        //Clear before repaint
        canvas.clipRect(0, 0, width, height)

        this.middlePointX = (rightEdge + leftEdge) / 2
        this.middlePointY = (bottomEdge + topEdge) / 2

        paint.color = gridColor

        for (node in gameBoard?.nodesHashSet!!) {
            node.coordNeighbors
                    .filterNot { node.coords.x != it.coords.x && node.coords.y != it.coords.y }
                    .forEach { canvas.drawLine(nodeToCoords(node)[0], nodeToCoords(node)[1], nodeToCoords(it)[0], nodeToCoords(it)[1], paint) }
        }

        drawGoalLine(topEdge, paint, Color.RED, gameBoard!!.topGoalLines.goalLine)
        drawGoalLine(bottomEdge, paint, Color.BLUE, gameBoard!!.bottomGoalLines.goalLine)

        paint.color = Color.BLACK
        paint.strokeWidth = sideLineStrokeWidth.toFloat()

        drawWalls(paint)
        drawGoalPosts(paint, gameBoard!!.topGoalLines.leftPost, gameBoard!!.topGoalLines.rightPost)
        drawGoalPosts(paint, gameBoard!!.bottomGoalLines.leftPost, gameBoard!!.bottomGoalLines.rightPost)

        paint.style = Paint.Style.FILL

        drawLines.forEach {
            paint.color = it.color
            canvas.drawLine(it.fromPoint.x, it.fromPoint.y, it.toPoint.x, it.toPoint.y, paint)
        }

        drawPossibleMoves()

        var image = resources.getDrawable(R.drawable.football)
        image = ScaleDrawable(image, 0, ballSize.toFloat(), ballSize.toFloat()).drawable
        val ballNodeCoords = pointsCoordsToCoords(gameViewDrawData?.ballNode?.coords!!)
        image.setBounds(ballNodeCoords[0].toInt() - ballSize / 2, ballNodeCoords[1].toInt() - ballSize / 2, ballNodeCoords[0].toInt() + ballSize / 2, ballNodeCoords[1].toInt() + ballSize / 2)
        image.draw(canvas)
    }

    private fun drawPossibleMoves() {
        paint.color = gameViewDrawData?.currentPlayerTurn?.playerColor as Int
        if (gameViewDrawData?.currentPlayerTurn?.isAi!!) return
        gameViewDrawData?.nodeNeighbors!!
                .map { nodeToCoords(it) }
                .forEach {
                    canvas?.drawCircle(it[0], it[1], 20f, paint)
                }
    }

    private fun drawGoalLine(edge: Float, paint: Paint, color: Int, line: IntegerLine) {
        paint.color = color
        paint.strokeWidth = sideLineStrokeWidth.toFloat()
        val startPoint = pointsCoordsToCoords(line.fromPoint)
        val endPoint = pointsCoordsToCoords(line.toPoint)
        canvas?.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1], paint)
    }

    private fun drawGoalPosts(paint: Paint, leftLine: IntegerLine, rightLine: IntegerLine){
        var startPoint = pointsCoordsToCoords(leftLine.fromPoint)
        var endPoint = pointsCoordsToCoords(leftLine.toPoint)
        canvas?.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1], paint)
        startPoint = pointsCoordsToCoords(rightLine.fromPoint)
        endPoint = pointsCoordsToCoords(rightLine.toPoint)
        canvas?.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1], paint)
    }

    private fun drawWalls(paint: Paint) {
        gameBoard?.nodesHashSet?.filter { otherNode -> otherNode.nodeType == NodeTypeEnum.Wall }?.forEach{
            it.coordNeighbors.filter { otherNode -> otherNode.nodeType == NodeTypeEnum.Wall }.forEach {
                otherNode -> if (!it.isDiagonalNeighbor(otherNode)) canvas?.drawLine(nodeToCoords(it)[0], nodeToCoords(it)[1], nodeToCoords(otherNode)[0], nodeToCoords(otherNode)[1], paint)
            }
        }

        gameBoard?.nodesHashSet?.filter { otherNode -> otherNode.nodeType == NodeTypeEnum.Wall }?.forEach{
            it.coordNeighbors.filter { otherNode -> otherNode.nodeType == NodeTypeEnum.Post }.forEach{
                otherNode -> if (!it.isDiagonalNeighbor(otherNode)) canvas?.drawLine(nodeToCoords(it)[0], nodeToCoords(it)[1], nodeToCoords(otherNode)[0], nodeToCoords(otherNode)[1], paint)
            }
        }
    }
}
