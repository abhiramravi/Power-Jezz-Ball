package abhiram.iitm.jezzball;

import java.util.ArrayList;

import abhiram.iitm.jezzball.JezzView.JezzThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class GFX extends Activity implements OnTouchListener
{
	JezzThread jJezzThread;
	JezzView jView;
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
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.lunar_layout);
	    jView = (JezzView) findViewById(R.id.jezzball);
	    
	    GameParameters.center = (TextView) findViewById(R.id.centertext);
	    GameParameters.level = (TextView) findViewById(R.id.leftbottomtext);
	    GameParameters.lives = (TextView) findViewById(R.id.centerbottomtext);
	    GameParameters.conquered = (TextView) findViewById(R.id.rightbottomtext);
	    
	    jView.setOnTouchListener(this);
	    jJezzThread = jView.getThread();
	    jJezzThread.doStart();
	}
	 /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
    	jJezzThread.interrupt();
        super.onPause();
        
    }
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	@Override
	public void onBackPressed() 
	{
		jJezzThread.pause();
		AlertDialog alert = new AlertDialog.Builder(GFX.this).create();
		alert.setTitle("Game Paused");
		alert.setButton("Resume", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				jJezzThread.unpause();
			}
		});
		alert.setButton2("Quit", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{}
		});
		alert.show();
	}
	
	
	
	float startX, startY, endX, endY;
	
	
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
