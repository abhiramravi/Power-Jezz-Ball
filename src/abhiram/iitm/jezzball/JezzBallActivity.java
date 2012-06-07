package abhiram.iitm.jezzball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    } 
}