package abhiram.iitm.jezzball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class JezzBallActivity extends Activity 
{
    /** Called when the activity is first created. */
    @Override	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);	
        
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(JezzBallActivity.this, LevelChooser.class));
				
			}
		});

        Button resume = (Button) findViewById(R.id.resume);
        resume.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				GameParameters.setParametersForLevel(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("levelReached", 1));
				startActivity(new Intent(JezzBallActivity.this, GFX.class));
				
			}
		});
        
        Button about = (Button) findViewById(R.id.about);
        about.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(JezzBallActivity.this, About.class));
				
			}
		});
        
        Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(true);
				
			}
		});
    }

	@Override
	public void onBackPressed()
	{
	} 
    
}