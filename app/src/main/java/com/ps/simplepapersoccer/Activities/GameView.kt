package com.ps.simplepapersoccer.Activities

import java.util.ArrayList

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.ScaleDrawable
import android.util.AttributeSet
import android.view.View
import com.ps.simplepapersoccer.Enums.NodeTypeEnum
import com.ps.simplepapersoccer.GameObjects.Game.GameBoard
import com.ps.simplepapersoccer.GameObjects.Game.GameViewDrawData
import com.ps.simplepapersoccer.GameObjects.Game.Geometry.LinesToDraw

import com.ps.simplepapersoccer.GameObjects.Game.Geometry.Node
import com.ps.simplepapersoccer.R

class GameView : View {

    var drawLines: MutableList<LinesToDraw> = ArrayList()

    var gameViewDrawData: GameViewDrawData? = null

    private var gameActivity: GameActivity? = null
    private var gameBoard: GameBoard? = null

    private var canvas: Canvas? = null
    private var paint: Paint? = null

    private val nodeSize = 20

    var leftEdge = 50f
    var topEdge = 100f

    var gridXdraw: Float = 0.toFloat()
    var gridYdraw: Float = 0.toFloat()

    var gridSizeX = 0
    var gridSizeY = 0

    private val gridStrokeWidth = 4
    private val gridColor = Color.rgb(242, 244, 247)

    var leftEdgeMargin = 30f
    var rightEdgeMargin = 1.027f

    private val ballSize = 100
    private val sideLineStrokeWidth = 8

    var rightEdge: Float = 0.toFloat()
    var bottomEdge: Float = 0.toFloat()

    protected var middlePointX: Float = 0.toFloat()
    protected var middlePointY: Float = 0.toFloat()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    fun SetValues(screenWidth: Int, screenHeight: Int, gridSizeX: Int, gridSizeY: Int, gameActivity: GameActivity, gameBoard: GameBoard) {
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

        this.gameActivity = gameActivity
        this.gameBoard = gameBoard

        this.invalidate()
    }

    //Converts node coordinates to screen coordinates
    fun nodeToCoords(n: Node): FloatArray {
        val coords = FloatArray(2)
        coords[0] = n.coords.x * gridXdraw + leftEdge
        coords[1] = n.coords.y * gridYdraw + topEdge
        return coords
    }

    fun localNodeCoordsToCoords(coords: Point): FloatArray {
        val newCoords = FloatArray(2)
        newCoords[0] = coords.x * gridXdraw + leftEdge
        newCoords[1] = coords.y * gridYdraw + topEdge
        return newCoords
    }

    //Converts screen coordinates to node coordinates
    fun coordsToNode(x: Float, y: Float): FloatArray {
        var x = (x + gridXdraw.div(2)) / gridXdraw * gridXdraw
        var y = (y + gridYdraw.div(2)) / gridYdraw * gridYdraw - topEdge

        val coords = FloatArray(2)
        coords[0] = x / gridXdraw
        coords[1] = y / gridYdraw
        return coords
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        RedrawMap()
    }

    fun drawAsync(gameViewDrawData: GameViewDrawData){
        drawLines?.add(gameViewDrawData.drawLine!!)
        this.gameViewDrawData = gameViewDrawData
        gameActivity?.setPlayerTurnTextViewText()
        this.invalidate()
    }

    fun RedrawMap() {
        paint = Paint()
        paint?.textSize = 40f
        paint?.strokeWidth = gridStrokeWidth.toFloat()
        paintComponent(canvas!!, paint!!)
    }

    fun paintComponent(canvas: Canvas, paint: Paint) {
        //Clear before repaint
        canvas.clipRect(0, 0, width, height)

        this.middlePointX = (rightEdge + leftEdge) / 2
        this.middlePointY = (bottomEdge + topEdge) / 2

        paint.color = gridColor

        for (node in gameBoard?.nodeHashMap?.values!!) {
            for (neighborNodeId in node.neighbors) {
                val neighborNode = gameBoard?.nodeHashMap!![neighborNodeId]
                if (node.coords.x != neighborNode!!.coords.x && node.coords.y != neighborNode.coords.y) continue
                canvas.drawLine(nodeToCoords(node)[0], nodeToCoords(node)[1], nodeToCoords(neighborNode!!)[0], nodeToCoords(neighborNode)[1], paint)
            }
        }

        DrawGoalLine(topEdge, paint, Color.RED)
        DrawGoalLine(bottomEdge, paint, Color.BLUE)

        paint.color = Color.BLACK
        paint.strokeWidth = sideLineStrokeWidth.toFloat()

        DrawWalls(paint)

        paint.style = Paint.Style.FILL

        for (i in drawLines.indices) {
            paint.color = drawLines[i].color
            canvas.drawLine(drawLines[i].fromPoint.x, drawLines[i].fromPoint.y, drawLines[i].toPoint.x, drawLines[i].toPoint.y, paint)
        }

        DrawPossibleMoves()

        //Draw ball
        var image = resources.getDrawable(R.drawable.football)
        image = ScaleDrawable(image, 0, ballSize.toFloat(), ballSize.toFloat()).drawable
        val ballNodeCoords = localNodeCoordsToCoords(gameViewDrawData?.ballNode?.coords!!)
        image.setBounds(ballNodeCoords[0].toInt() - ballSize / 2, ballNodeCoords[1].toInt() - ballSize / 2, ballNodeCoords[0].toInt() + ballSize / 2, ballNodeCoords[1].toInt() + ballSize / 2)
        image.draw(canvas)
    }

    private fun DrawPossibleMoves() {
        paint?.color = gameViewDrawData?.currentPlayerTurn?.playerColor as Int
        if (gameViewDrawData?.currentPlayerTurn?.isAi!!) return
        gameViewDrawData?.nodeNeighbors!!
                .map { nodeToCoords(it) }
                .forEach { canvas?.drawCircle(it[0], it[1], 20f, paint) }
    }

    private fun DrawGoalLine(edge: Float, paint: Paint, color: Int) {
        paint.color = color
        canvas?.drawCircle(middlePointX, edge, nodeSize.toFloat(), paint)
    }

    private fun DrawWalls(paint: Paint) {
        gameBoard?.nodeHashMap?.values!!.stream().filter({ otherNode -> otherNode.nodeType == NodeTypeEnum.Wall }).forEach{
            it.coordNeighbors.stream().filter({ otherNode -> otherNode.nodeType == NodeTypeEnum.Wall }).forEach{
                otherNode -> canvas?.drawLine(nodeToCoords(it)[0], nodeToCoords(it)[1], nodeToCoords(otherNode)[0], nodeToCoords(otherNode)[1], paint) } }
    }
}
