package abhiram.iitm.jezzball;

import abhiram.iitm.jezzball.JezzView.JezzThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class GFX extends Activity implements OnTouchListener
{
	/** The static JezzThread that will handle all the physics and drawing */
	static JezzThread jJezzThread;
	
	/** The static JezzView that is used as the base surface view for the canvas */
	static JezzView jView;
	
	//Alert Dialogs for respective alerting NOTE: Using same static alert boxes and setting different buttons at different times does not work.
	public static AlertDialog losealert;
	public static AlertDialog winalert;
	public static AlertDialog pausealert;
	
	// Need handler for callbacks to the UI thread
    public static final Handler mHandler = new Handler();

    public Handler getmHandler()
	{
		return mHandler;
	}
	public Runnable getmUpdateResults()
	{
		return mUpdateResults;
	}


	// Create runnable for posting
    public static final Runnable mUpdateResults = new Runnable() {
        public void run() {
            GameParameters.updateResultsInUi();
        }
    };
    // Another runnable for posting the win/lose results - could actually merge both runnables into one
    public static final Runnable jWinOrLose = new Runnable() {
        public void run() {
            gameWinOrLose(GameParameters.CURRENT_GAME_STATE);
        }

    };
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    /*Initializing the layout*/
	    setContentView(R.layout.lunar_layout);
	    
	    /* Getting the JezzView from the layout */
	    jView = (JezzView) findViewById(R.id.jezzball);
	    
	    /* Setting the textviews for score display */
	    GameParameters.center = (TextView) findViewById(R.id.centertext);
	    GameParameters.level = (TextView) findViewById(R.id.leftbottomtext);
	    GameParameters.lives = (TextView) findViewById(R.id.centerbottomtext);
	    GameParameters.conquered = (TextView) findViewById(R.id.rightbottomtext);
	    
	    /* Initializing the dialog boxes here itself (can't be done in a static method) */
	    losealert = new AlertDialog.Builder(GFX.this).create();
	    winalert = new AlertDialog.Builder(GFX.this).create();
	    pausealert = new AlertDialog.Builder(GFX.this).create();
	    
	    /* Setting the onTouchListener for the surface view to be the one defined here */
	    jView.setOnTouchListener(this);
	    
	    
	    
	}
	 /**
     * Invoked when the user clicks the home button 
     */
    @Override
    protected void onPause() {
    	boolean retry = true;
		jJezzThread.setRunning(false);
		while (retry)
		{
			try
			{
				jJezzThread.join();
				retry = false;
			} catch (InterruptedException e)
			{
			}
		}
        super.onPause();
        
    }
    /** No idea when this is invoked */
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	/** Invoked when the back button is pressed 
	 * 
	 * The game is paused - balls are stuck in the air and so are any dynamic lines
	 * On unpausing, the activity resumes as usual */
	@Override
	public void onBackPressed() 
	{
		jJezzThread.pause();
		
		pausealert.setTitle("Game Paused");
		pausealert.setButton("Resume", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				jJezzThread.unpause();
			}
		});
		pausealert.setButton2("Quit", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{ 
				//TODO : Save all the data of current game
				startActivity(new Intent(GFX.this, JezzBallActivity.class));
			}
		});
		pausealert.show();
	}
	
	/** The method defining the runnable that is being posted by another thread */
	private static void gameWinOrLose(int state)
	{
		if(state == GameParameters.STATE_WIN)
		{
			losealert.setTitle("You Win!");
			losealert.setButton("Next Level", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					GameParameters.setParametersForLevel(GameParameters.CURRENT_LEVEL + 1);
					jJezzThread.doStart();
				}
			});
			losealert.setButton2("Quit", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{ 
					//TODO : Save all the data of current game
					//startActivity(new Intent(GFX.this, JezzBallActivity.class));
				}
			});
			losealert.show();
		}
		if(state == GameParameters.STATE_LOSE)
		{
			winalert.setTitle("You Lose!");
			winalert.setButton("Try Again", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					
					GameParameters.setParametersForLevel(GameParameters.CURRENT_LEVEL);
					jJezzThread.doStart();
				}
			});
			winalert.setButton2("Quit", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{ 
					//TODO : Save all the data of current game
					//startActivity(new Intent(GFX.this, JezzBallActivity.class));
				}
			});
			winalert.show();
		}
	}
	
	float startX, startY, endX, endY;
	
	/** The onTouchListener that determines if the user gesture was representing 
	 * a horizontal line or vertical line */
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		/** deciding if the user action is for a horizontal/vertical separator */
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			startX = event.getX();
			startY = event.getY();
		}
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			endX = event.getX();
			endY = event.getY();
			
			if(Math.abs(endX - startX) > Math.abs(endY - startY)) GameParameters.setUserAction(true);
			else GameParameters.setUserAction(false);
			
			Log.d("UserAction", Boolean.toString(GameParameters.getUserAction()));
			
			synchronized(GameParameters.lock)
			{
				if(GameParameters.linesFixed == GameParameters.linesOnScreen)
				{
					if(GameParameters.isValidLineStart(startX, startY))
					{
						GameParameters.line.add( new Line(jView.getHolder(), jView.getContext(), new Handler(), GameParameters.getUserAction(), startX, startY));
						GameParameters.line.get(GameParameters.linesFixed).doStart();
						GameParameters.linesOnScreen++;
					}
				}
			}
			
		}
		return true;
	}
	 /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        jJezzThread.saveState(outState);
        Log.w(this.getClass().getName(), "SIS called");
    }
    
}
