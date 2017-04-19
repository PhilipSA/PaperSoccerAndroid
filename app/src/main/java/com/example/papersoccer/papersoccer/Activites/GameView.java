package com.example.papersoccer.papersoccer.Activites;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.example.papersoccer.papersoccer.GameObjects.LinesToDraw;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.R;

public class GameView extends View {

 	public List<LinesToDraw> drawLines = new ArrayList<LinesToDraw>();
    
	private Canvas canvas;
	private Paint paint;

	private int nodeSize = 20;
	
	public float leftEdge = 50;
	public float topEdge = 100;
	
	public float gridXdraw;
	public float gridYdraw;
	
	public int gridSizeX = 8;
	public int gridSizeY = 10;

	public float leftEdgeMargin = 30;
	public float rightEdgeMargin = 1.027f;

	private int ballSize = 100;
	private int sideLineStrokeWidth = 8;
	
	public float rightEdge;
	public float bottomEdge;
	
	protected float middlePointX;
	protected float middlePointY;
	
	private Player playerTurn;
	private Player myPlayer;

	private float ballX;
	private float ballY;
		
	public GameView(Context context)
	{
		super(context);
	}
	
    public GameView(Context context, AttributeSet attrs)
	{
        super(context, attrs);
    }
    
	public void setMyPlayer(Player myPlayer)
	{
		this.myPlayer = myPlayer;
	}
    
    public void UpdateBallPosition(float[] ballCoords, Player playerTurn)
    {
    	this.ballX = ballCoords[0];
    	this.ballY = ballCoords[1];
    	this.playerTurn = playerTurn;
    }
    
    public void SetValues(int screenWidth, int screenHeight, int gridSizeX, int gridSizeY)
    {
        topEdge = screenHeight/9;
		leftEdge = screenWidth/leftEdgeMargin;
		
		this.gridSizeX = gridSizeX;
		this.gridSizeY = gridSizeY;
		
		bottomEdge = screenHeight/1.2f;
		rightEdge = screenWidth/rightEdgeMargin;
			
		gridXdraw = (rightEdge-leftEdge)/gridSizeX;
		gridYdraw = (bottomEdge-topEdge)/gridSizeY;
		
		this.middlePointX = (rightEdge+leftEdge)/2;
		this.middlePointY = (bottomEdge+topEdge)/2;
		
		this.invalidate();
    }

	@Override
    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        this.canvas = canvas;		   
	        RedrawMap();
    }
    
    public void RedrawMap()
    {
        paint = new Paint();
        paint.setTextSize(40);
		paint.setStrokeWidth(2);
    	paintComponent(canvas, paint);
    }
    
    public void paintComponent(Canvas canvas, Paint paint) {
		//Clear before repaint
		canvas.clipRect(0, 0, getWidth(), getHeight());
		
		this.middlePointX = (rightEdge+leftEdge)/2;
		this.middlePointY = (bottomEdge+topEdge)/2;

		paint.setColor(Color.BLACK);
		for(float outerLoop = 0; outerLoop <= gridSizeX; ++outerLoop)
		{
			for (float innerLoop = 1; innerLoop < gridSizeY; ++innerLoop)
			{
				//Vert
				canvas.drawLine(outerLoop*gridXdraw+leftEdge, topEdge+gridYdraw, outerLoop*gridXdraw+leftEdge, bottomEdge-gridYdraw, paint);

				//Horz
				canvas.drawLine(leftEdge, innerLoop*gridYdraw+topEdge, rightEdge, innerLoop*gridYdraw+topEdge, paint);
			}
		}


		DrawGoal(topEdge, paint, Color.RED);
		DrawGoal(bottomEdge, paint, Color.BLUE);
		
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(sideLineStrokeWidth);

		DrawSideline(leftEdge, paint);
		DrawSideline(rightEdge, paint);

		DrawGoalLines(topEdge, paint, topEdge+gridYdraw, 0, Color.RED);
		DrawGoalLines(bottomEdge, paint, bottomEdge-gridYdraw, 180, Color.BLUE);
		
		paint.setStyle(Paint.Style.FILL);
		
		for(int i = 0; i < drawLines.size(); ++i)
		{
			paint.setColor(drawLines.get(i).color);
			canvas.drawLine(drawLines.get(i).fromX,drawLines.get(i).fromY, drawLines.get(i).toX,drawLines.get(i).toY, paint);
		}

		DrawCurrentPlayersTurnText(playerTurn.playerColor, String.format("It's %s turn", playerTurn.playerName), paint);

		//Draw ball
		Drawable image = getResources().getDrawable(R.drawable.football);
		image = new ScaleDrawable(image, 0, ballSize, ballSize).getDrawable();
		image.setBounds((int)ballX-ballSize/2, (int)ballY-ballSize/2, (int)ballX+ballSize/2, (int)ballY+ballSize/2);
		image.draw(canvas);

		System.out.println("painted map");
	}

	private void DrawCurrentPlayersTurnText(int color, String text, Paint paint)
	{
		paint.setColor(color);
		canvas.drawText(text, leftEdge+200, bottomEdge+100, paint);
	}

	private void DrawGoalLines(float edge, Paint paint, float edgeMargin, float arcAngle, int color)
	{
		canvas.drawLine(leftEdge, edgeMargin, middlePointX-gridXdraw, edgeMargin, paint);
		canvas.drawLine(rightEdge, edgeMargin, middlePointX+gridXdraw, edgeMargin, paint);

		canvas.drawLine(middlePointX+gridXdraw, edge, middlePointX+gridXdraw, edgeMargin, paint);
		canvas.drawLine(middlePointX-gridXdraw, edge, middlePointX+gridXdraw, edge, paint);
		canvas.drawLine(middlePointX-gridXdraw, edgeMargin, middlePointX-gridXdraw, edge, paint);

		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawArc(new RectF(middlePointX-50, edge-50, middlePointX+50, edge+50), arcAngle, 180, false, paint);
		paint.setColor(Color.BLACK);
	}

	private void DrawGoal(float edge, Paint paint, int color)
	{
		paint.setColor(color);
		canvas.drawCircle(middlePointX, edge, nodeSize, paint);
	}

	private void DrawSideline(float edge, Paint paint)
	{
		canvas.drawLine(edge, topEdge+gridYdraw, edge, bottomEdge-gridYdraw, paint);
	}
}
