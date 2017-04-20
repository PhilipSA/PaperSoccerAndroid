package com.example.papersoccer.papersoccer.Activites;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.papersoccer.papersoccer.Enums.DifficultyEnum;
import com.example.papersoccer.papersoccer.GameObjects.GameHandler;
import com.example.papersoccer.papersoccer.GameObjects.Move;
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.R;
import com.example.papersoccer.papersoccer.GameObjects.LinesToDraw;

public class GameActivity extends Activity {

	public GameView gameView;

	protected TextView player1Name;
	protected TextView player2Name;
	
	protected int screenHeight;
	protected int screenWidth;
	
	protected GameHandler gameHandler;
	
	public String myName;
	public boolean isMultiplayer;
	
	private Button playAgain;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String difficulty = sharedPreferences.getString("pref_difficultyLevel", "Medium");
		String playerName = sharedPreferences.getString("pref_playerName", "Player");

		isMultiplayer = getIntent().getBooleanExtra("MULTIPLAYER_MODE", false);

		myName = playerName;
		
		gameView = (GameView)findViewById(R.id.gameview);
		gameView.SetValues(GameActivity.getWidth(this), GameActivity.getHeight(this), gameView.gridSizeX, gameView.gridSizeY);
		
		player1Name = (TextView)findViewById(R.id.player1Textview);
		player2Name = (TextView)findViewById(R.id.player2Textview);
		
		player1Name.setTextColor(Color.BLUE);
		player2Name.setTextColor(Color.RED);
		
        final Player p1 = new Player(playerName, 1, Color.BLUE);
        final Player p2 = new Player("Player 2", 2, Color.RED);
		
        gameHandler = new GameHandler(this, gameView.gridSizeX, gameView.gridSizeY, DifficultyEnum.valueOf(difficulty), p1, p2);
        UpdateBallPosition();
		
		playAgain = (Button)findViewById(R.id.playagainButton);

		gameView.setMyPlayer(p1);
		if (!isMultiplayer) player2Name.setText("Ai"+difficulty);
		player1Name.setText(playerName);
		
		gameView.setOnTouchListener(new View.OnTouchListener()
        {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
		    	{
		    		Node n = gameHandler.coordsToNode(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
		    		if (n != null)
		    		{
			    		if (isMultiplayer) gameHandler.ProgressGame(new Move(gameHandler.ballNode, n, gameHandler.currentPlayersTurn));
						else {
							gameHandler.ProgressGame(new Move(gameHandler.ballNode, n, p1));
						}
						UpdateBallPosition();
			    		gameView.invalidate();
		    		}
		    	}
				return false;
			}
		});

	} //OnCreate

	public void UpdateBallPosition()
	{
		gameView.UpdateBallPosition(gameHandler.nodeToCoords(gameHandler.ballNode), gameHandler.currentPlayersTurn);
	}
	
	public void AddNewLineToDraw (float oldNodeCoords, float oldNodeCoords2, float newLineCoords, float newLineCoords2, int color)
	{
		gameView.drawLines.add(new LinesToDraw(oldNodeCoords, oldNodeCoords2, newLineCoords, newLineCoords2, color));
		UpdateBallPosition();
		gameView.invalidate();
	}
	
	//Compability
	public static int getWidth(Context mContext)
    {
	    int width=0;
	    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();
	    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.HONEYCOMB){                   
	        Point size = new Point();
	        display.getSize(size);
	        width = size.x;
	    }
	    else
        {
	        width = display.getWidth();  // deprecated
	    }
	    return width;
	}
	
	//Compability
	public static int getHeight(Context mContext)
    {
	    int height=0;
	    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();
	    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.HONEYCOMB){               
	        Point size = new Point();
	        display.getSize(size);
	        height = size.y;
	    }
	    else
        {
	        height = display.getHeight();  // deprecated
	    }
	    return height;      
	}
	
	public void Winner(Player player)
	{
		playAgain.setVisibility(View.VISIBLE);
		gameView.setEnabled(false);
	}
	
	public void ResetGame(View view)
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
		     public void run() {
		 		playAgain.setVisibility(View.INVISIBLE);
				recreate();
		     }
		});		
	}
	
	public void Quit(View view)
	{
		finish();
	}

	public void reDraw() {
		gameView.invalidate();		
	}
}
