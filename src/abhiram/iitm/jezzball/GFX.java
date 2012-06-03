package abhiram.iitm.jezzball;

import java.util.ArrayList;

import abhiram.iitm.jezzball.JezzView.JezzThread;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GFX extends Activity implements OnTouchListener
{
	JezzThread jJezzThread;
	JezzView jView;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    jView = new JezzView(this);
	    setContentView(jView);
	    jView.setOnTouchListener(this);
	    jJezzThread = jView.getThread();
	    jJezzThread.doStart();
	}
	 /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        jView.getThread().pause(); // pause game when Activity pauses
    }
	@Override
	protected void onResume() 
	{
		super.onResume();
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
				GameParameters.line.add( new Line(jView.getHolder(), jView.getContext(), new Handler(), GameParameters.getUserAction(), startX, startY));
				GameParameters.line.get(GameParameters.linesFixed).doStart();	
			}
			
		}
		return true;
	}

	

}
