package com.ps.simplepapersoccer.Activities

import java.util.ArrayList

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ScaleDrawable
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import com.ps.simplepapersoccer.GameObjects.Game.GameViewDrawData
import com.ps.simplepapersoccer.GameObjects.Game.LinesToDraw

import com.ps.simplepapersoccer.GameObjects.Game.Node
import com.ps.simplepapersoccer.GameObjects.Player
import com.ps.simplepapersoccer.R
import java.util.concurrent.CopyOnWriteArrayList

class GameView : View {

    var drawLines: MutableList<LinesToDraw> = ArrayList()

    var gameViewDrawData: GameViewDrawData? = null

    private var gameActivity: GameActivity? = null

    private var canvas: Canvas? = null
    private var paint: Paint? = null

    private val nodeSize = 20

    var leftEdge = 50f
    var topEdge = 100f

    var gridXdraw: Float = 0.toFloat()
    var gridYdraw: Float = 0.toFloat()

    var gridSizeX = 8
    var gridSizeY = 10

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

    private var playersTurn: Player? = null
    private var neighbors: MutableList<Node>? = ArrayList()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    fun SetValues(screenWidth: Int, screenHeight: Int, gridSizeX: Int, gridSizeY: Int, gameActivity: GameActivity) {
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

        this.invalidate()
    }

    fun setCurrentTurnData(player: Player, neighborList: CopyOnWriteArrayList<Node>)
    {
        playersTurn = player
        neighbors = neighborList
    }

    //Converts node coordinates to screen coordinates
    fun nodeToCoords(n: Node): FloatArray {
        val coords = FloatArray(2)
        coords[0] = n.xCord * gridXdraw + leftEdge
        coords[1] = n.yCord * gridYdraw + topEdge
        return coords
    }

    fun localNodeCoordsToCoords(x: Int, y: Int): FloatArray {
        val coords = FloatArray(2)
        coords[0] = x * gridXdraw + leftEdge
        coords[1] = y * gridYdraw + topEdge
        return coords
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
        for (outerLoop in 0..gridSizeX) {
            for (innerLoop in 1..gridSizeY - 1) {
                //Vert
                canvas.drawLine(outerLoop * gridXdraw + leftEdge, topEdge + gridYdraw, outerLoop * gridXdraw + leftEdge, bottomEdge - gridYdraw, paint)

                //Horz
                canvas.drawLine(leftEdge, innerLoop * gridYdraw + topEdge, rightEdge, innerLoop * gridYdraw + topEdge, paint)
            }
        }

        DrawGoal(topEdge, paint, Color.RED)
        DrawGoal(bottomEdge, paint, Color.BLUE)

        paint.color = Color.BLACK
        paint.strokeWidth = sideLineStrokeWidth.toFloat()

        DrawSideline(leftEdge, paint)
        DrawSideline(rightEdge, paint)

        DrawGoalLines(topEdge, paint, topEdge + gridYdraw, Color.RED)
        DrawGoalLines(bottomEdge, paint, bottomEdge - gridYdraw, Color.BLUE)

        paint.style = Paint.Style.FILL

        for (i in drawLines.indices) {
            paint.color = drawLines[i].color
            canvas.drawLine(drawLines[i].fromX, drawLines[i].fromY, drawLines[i].toX, drawLines[i].toY, paint)
        }

        DrawPossibleMoves()

        //Draw ball
        var image = resources.getDrawable(R.drawable.football)
        image = ScaleDrawable(image, 0, ballSize.toFloat(), ballSize.toFloat()).drawable
        val ballNodeCoords = localNodeCoordsToCoords(gameViewDrawData?.ballNode?.xCord!!, gameViewDrawData?.ballNode?.yCord!!)
        image.setBounds(ballNodeCoords[0].toInt() - ballSize / 2, ballNodeCoords[1].toInt() - ballSize / 2, ballNodeCoords[0].toInt() + ballSize / 2, ballNodeCoords[1].toInt() + ballSize / 2)
        image.draw(canvas)
    }

    private fun DrawPossibleMoves() {
        if (playersTurn != null) {
            paint?.color = playersTurn?.playerColor as Int
            if (playersTurn?.isAi!!) return
            neighbors!!
                    .map { nodeToCoords(it) }
                    .forEach { canvas?.drawCircle(it[0], it[1], 20f, paint) }
        }
    }

    private fun DrawGoalLines(edge: Float, paint: Paint, edgeMargin: Float, color: Int) {
        canvas?.drawLine(leftEdge, edgeMargin, middlePointX - gridXdraw, edgeMargin, paint)
        canvas?.drawLine(rightEdge, edgeMargin, middlePointX + gridXdraw, edgeMargin, paint)

        canvas?.drawLine(middlePointX + gridXdraw, edge, middlePointX + gridXdraw, edgeMargin, paint)
        canvas?.drawLine(middlePointX - gridXdraw, edge, middlePointX + gridXdraw, edge, paint)
        canvas?.drawLine(middlePointX - gridXdraw, edgeMargin, middlePointX - gridXdraw, edge, paint)

        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
    }

    private fun DrawGoal(edge: Float, paint: Paint, color: Int) {
        paint.color = color
        canvas?.drawCircle(middlePointX, edge, nodeSize.toFloat(), paint)
    }

    private fun DrawSideline(edge: Float, paint: Paint) {
        canvas?.drawLine(edge, topEdge + gridYdraw, edge, bottomEdge - gridYdraw, paint)
    }
}
