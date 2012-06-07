package abhiram.iitm.jezzball;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class LevelChooser extends ListActivity
{
	MyLevelAdapter customAdapter;
	public static String levelChosen;
	public static String[] levels = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.levelchooser);
	    
	    levelChosen = "null";
	    getListView().setDividerHeight(0);
	    getListView().setFadingEdgeLength(10);
	    customAdapter = new MyLevelAdapter(this, android.R.layout.simple_list_item_1, R.id.level, levels);
	    setListAdapter(customAdapter);
	}
	@Override
	protected void onPause()
	{
		levelChosen = "null";
		super.onPause();
	}
	@Override
	protected void onResume()
	{
		levelChosen = "null";
		new nextActivity().execute();
		super.onResume();
	}
	class nextActivity extends AsyncTask<String, Integer, String>
	{

		@Override
		protected void onPreExecute()
		{
			levelChosen = "null";
		}

		@Override
		protected String doInBackground(String... params)
		{
			while(levelChosen.equals("null"));
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			Intent i = new Intent(LevelChooser.this, GFX.class);
			i.putExtra("levelChosen", levelChosen);
			startActivity(i);
		}
		
	}
}

class MyLevelAdapter extends ArrayAdapter<String>
{

	public MyLevelAdapter(Context context, int resource, int textViewResourceId, String[] objects) 
	{
		super(context, resource, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		/* First inflate list_item and put it as a row */
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.level_list_item, parent, false);
		row.setClickable(true);
	    row.setFocusable(true);
	    
	    /* For the anonymous inner classes */
	    final int i = position;
	    
	    /* Setting up the level buttons for modification */
		Button level = (Button) row.findViewById(R.id.level);
		
		/* Setting the Level */
		level.setText("Level "+ LevelChooser.levels[position]);
		
		
		level.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				GameParameters.setParametersForLevel(i+1);
				
				LevelChooser.levelChosen = Integer.toString(i);
			}
		});

		return row;
	}
	
	
}