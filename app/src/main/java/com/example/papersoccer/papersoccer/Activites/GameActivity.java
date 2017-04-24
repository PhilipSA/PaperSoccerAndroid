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
import com.example.papersoccer.papersoccer.GameObjects.Node;
import com.example.papersoccer.papersoccer.GameObjects.Player;
import com.example.papersoccer.papersoccer.R;
import com.example.papersoccer.papersoccer.GameObjects.LinesToDraw;

public class GameActivity extends Activity {

	public GameView gameView;

	private TextView player1NameTextView;
	private TextView player2NameTextView;
	private TextView playerTurnTextView;

	private TextView player1ScoreTextView;
	private TextView player2ScoreTextView;

	private TextView playerWinnerTextView;

	private int player1Color = Color.BLUE;
	private int player2Color = Color.RED;

	private int screenHeight;
	private int screenWidth;

	private GameHandler gameHandler;
	
	public String myName;
	
	private Button playAgain;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String difficulty = sharedPreferences.getString("pref_difficultyLevel", "Medium");
		String playerName = sharedPreferences.getString("pref_playerName", "Player");

		boolean isMultiplayer = getIntent().getBooleanExtra("MULTIPLAYER_MODE", false);

		myName = playerName;
		
		gameView = (GameView)findViewById(R.id.gameview);
		gameView.SetValues(GameActivity.getWidth(this), GameActivity.getHeight(this), gameView.gridSizeX, gameView.gridSizeY);
		
		player1NameTextView = (TextView)findViewById(R.id.player1TextView);
		player2NameTextView = (TextView)findViewById(R.id.player2TextView);
		playerTurnTextView = (TextView)findViewById(R.id.playerTurnTextView);
		playerWinnerTextView = (TextView)findViewById(R.id.playerWinnerTextview);

		player1ScoreTextView = (TextView)findViewById(R.id.player1ScoreTextView);
		player2ScoreTextView = (TextView)findViewById(R.id.player2ScoreTextView);

		player1NameTextView.setTextColor(player1Color);
		player2NameTextView.setTextColor(player2Color);

		if (!isMultiplayer) player2NameTextView.setText("Ai"+difficulty);
		player1NameTextView.setText(playerName);
		
        final Player player1 = new Player(playerName, 1, player1Color);
        final Player player2 = new Player(player2NameTextView.getText().toString(), 2, player2Color);
		
        gameHandler = new GameHandler(this, gameView.gridSizeX, gameView.gridSizeY, DifficultyEnum.valueOf(difficulty), player1, player2, isMultiplayer);
        UpdateDrawData();
		
		playAgain = (Button)findViewById(R.id.playagainButton);

		gameView.setOnTouchListener(new View.OnTouchListener()
        {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
		    	{
		    		Node touchedNode = gameHandler.coordsToNode(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
		    		if (touchedNode != null)
		    		{
						gameHandler.PlayerMakeMove(touchedNode, gameHandler.currentPlayersTurn);
		    		}
		    	}
				return false;
			}
		});

	} //OnCreate

	public void UpdateDrawData()
	{
		playerTurnTextView.setText(String.format("It's %s turn", gameHandler.currentPlayersTurn.playerName));
		playerTurnTextView.setTextColor(gameHandler.currentPlayersTurn.playerColor);
		gameView.UpdateBallPosition(gameHandler.nodeToCoords(gameHandler.ballNode), gameHandler.currentPlayersTurn);
	}
	
	public void AddNewLineToDraw (float oldNodeCoords, float oldNodeCoords2, float newLineCoords, float newLineCoords2, int color)
	{
		gameView.drawLines.add(new LinesToDraw(oldNodeCoords, oldNodeCoords2, newLineCoords, newLineCoords2, color));
		UpdateDrawData();
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
		playerWinnerTextView.setText(String.format("%s scored a goal!", player.playerName));
		playerWinnerTextView.setVisibility(View.VISIBLE);
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
