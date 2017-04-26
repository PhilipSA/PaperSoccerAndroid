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
import com.example.papersoccer.papersoccer.GameObjects.PlayerActivityData;
import com.example.papersoccer.papersoccer.R;
import com.example.papersoccer.papersoccer.GameObjects.LinesToDraw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

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

	public Map<Player, PlayerActivityData> playerActivityDataMap = new HashMap<>();
	
	private Button playAgain;

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(gameHandler.player1.playerName, gameHandler.player1.score);
		outState.putInt(gameHandler.player2.playerName, gameHandler.player2.score);
	}
	
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

		ArrayList<Player> players = new ArrayList<>();
		if (!isMultiplayer)
		{
			players = assignPlayerAndAi(difficulty, playerName);
		}
		else
		{
			players.add(new Player(playerName, 1, player1Color, false));
			players.add(new Player("Player2", 2, player2Color, false));
		}

		player1NameTextView.setText(players.get(0).playerName);
		player2NameTextView.setText(players.get(1).playerName);

		if (savedInstanceState != null) {
			players.get(0).score = savedInstanceState.getInt(players.get(0).playerName);
			players.get(1).score = savedInstanceState.getInt(players.get(1).playerName);
		}

		PlayerActivityData playerActivityData = new PlayerActivityData(player1NameTextView, player1ScoreTextView);
		playerActivityDataMap.put(players.get(0), playerActivityData);
		PlayerActivityData playerActivityData2 = new PlayerActivityData(player2NameTextView, player2ScoreTextView);
		playerActivityDataMap.put(players.get(1), playerActivityData2);

		SetScoreText(players.get(0));
		SetScoreText(players.get(1));

        gameHandler = new GameHandler(this, gameView.gridSizeX, gameView.gridSizeY, DifficultyEnum.valueOf(difficulty), players, isMultiplayer);
        UpdateDrawData();
		
		playAgain = (Button)findViewById(R.id.playagainButton);

		gameHandler.UpdateGameState();

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

	private ArrayList<Player> assignPlayerAndAi(String difficulty, String playerName)
	{
		ArrayList<Player> players = new ArrayList<>();
		Random random = new Random();
		if (random.nextBoolean()) {
			players.add(new Player(playerName, 1, player1Color, false));
			players.add(new Player("Ai" + difficulty, 2, player2Color, true));
		}
		else {
			players.add(new Player("Ai" + difficulty, 1, player1Color, true));
			players.add(new Player(playerName, 2, player2Color, false));
		}
		return players;
	}

	private void SetScoreText(Player player)
	{
		playerActivityDataMap.get(player).playerScoreTextView.setText(String.format("%s: %d", player.playerName, player.score));
	}

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
		SetScoreText(player);
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
