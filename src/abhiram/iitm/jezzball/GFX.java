package abhiram.iitm.jezzball;

import abhiram.iitm.jezzball.JezzView.JezzThread;
import android.app.Activity;
import android.os.Bundle;

public class GFX extends Activity
{
	JezzThread jJezzThread;
	JezzView customView;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    customView = new JezzView(this);
	    setContentView(customView);
	    
	    jJezzThread = customView.getThread();
	    jJezzThread.doStart();
	}
	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	

}
