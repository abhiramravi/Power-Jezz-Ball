package abhiram.iitm.jezzball;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class JezzView extends SurfaceView implements SurfaceHolder.Callback
{
	class JezzThread extends Thread
	{
		
		/*
		 * Initial positions of the ball in the game
		 */
		/** Message handler used by thread to interact with TextView */
		private Handler jHandler;

		private Random rand;
		/** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
		public int jMode = GameParameters.STATE_PAUSE;

		public static final int INIT_X = 50;
		public static final int INIT_Y = 50;


		/** Indicate whether the surface has been created & is ready to draw */
		public boolean jRun = false;
		private SurfaceHolder jSurfaceHolder;
		
		public int numberOfBalls;

		private int jDifficulty;

		
		private int color;
		public SurfaceHolder getJSurfaceHolder()
		{
			return this.jSurfaceHolder;
		}
		
		public JezzThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler)
		{
			// get handles to some important objects
			jSurfaceHolder = surfaceHolder;

			jHandler = handler;
			jContext = context;

			Resources res = context.getResources();
			// cache handles to our key sprites & other drawables

			// load background image as a Bitmap instead of a Drawable b/c
			// we don't need to transform it and it's faster to draw this way
		
			GameParameters.setjBallBitmap(BitmapFactory.decodeResource(res, R.drawable.ball));
			GameParameters.setBallWidth(GameParameters.getjBallBitmap().getWidth());
			GameParameters.setBallHeight(GameParameters.getjBallBitmap().getHeight());
			
			GameParameters.setScreenArea(GameParameters.getScreenHeight() * GameParameters.getScreenWidth());
			GameParameters.resetAreaConquered();
			
			// TODO : Initialize paints for speedometer
			jDifficulty = GameParameters.DIFFICULTY_MEDIUM;
			
			//TODO : Must make this change based on level of game
			numberOfBalls = GameParameters.getNumberOfBalls();
			color = Color.WHITE;

		}

		/**
		 * Starts the game, setting parameters for the current difficulty.
		 */
		public void doStart()
		{
			synchronized (jSurfaceHolder)
			{
				
				setState(GameParameters.STATE_RUNNING);
			}
		}

		@Override
		public void run()
		{
			while (jRun)
			{
				Canvas c = null;
				try
				{
					c = jSurfaceHolder.lockCanvas(null);
					synchronized (jSurfaceHolder)
					{
						if (jMode == GameParameters.STATE_RUNNING)
							updatePhysics();
						doDraw(c);
					}
				} finally
				{
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null)
					{
						jSurfaceHolder.unlockCanvasAndPost(c);
						GFX.mHandler.post(GFX.mUpdateResults);
					}
				}
			}
		}

		

		private void doDraw(Canvas canvas)
		{
			synchronized(GameParameters.lock)
			{
				canvas.drawColor(color);
				for(int i = 0; i < GameParameters.linesOnScreen; i++ )
				{
					GameParameters.line.get(i).doDraw(canvas);
				}
				for(int i = 0; i < numberOfBalls; i++)
				{
					GameParameters.jezzBalls[i].doDraw(canvas);
				}
				
			}
		}

		private void updatePhysics()
		{
			if(GameParameters.LIVES <= 0)
			{
				color = Color.rgb(230, 100, 74);
			}
			if(GameParameters.getAreaConquered()/GameParameters.getScreenArea() > 0.75)
			{
				color = Color.rgb(127, 190, 108);
				Log.d("WIN!", Float.toString(GameParameters.getAreaConquered()) + " " + Float.toString(GameParameters.getScreenArea()));
			}
			
			synchronized(GameParameters.lock)
			{
				
				for(int i = 0; i < GameParameters.linesOnScreen; i++ )
				{
					GameParameters.line.get(i).updatePhysics();
				}
				for(int i = 0; i < numberOfBalls; i++)
				{
					GameParameters.jezzBalls[i].updatePhysics();
				}
				
			}
		}

		/**
		 * Sets the current difficulty.
		 * 
		 * @param difficulty
		 */
		public void setDifficulty(int difficulty)
		{
			synchronized (jSurfaceHolder)
			{
				jDifficulty = difficulty;
			}
		}

		/**
		 * Pauses the physics update & animation.
		 */
		public void pause()
		{
			synchronized (jSurfaceHolder)
			{
				if (jMode == GameParameters.STATE_RUNNING)
					setState(GameParameters.STATE_PAUSE);
			}
		}

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 * 
		 * @param b
		 *            true to run, false to shut down
		 */
		public void setRunning(boolean b)
		{
			jRun = b;
		}

		public void setState(int mode)
		{
			synchronized (jSurfaceHolder)
			{
				jMode = mode;
			}
		}

		/**
		 * Resumes from a pause.
		 */
		public void unpause()
		{
			setState(GameParameters.STATE_RUNNING);
		}
	}

	private static Context jContext;
	/** The thread that actually draws the animation */
	private JezzThread thread;

	public JezzView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		
		// create thread only; it's started in surfaceCreated()
		thread = new JezzThread(holder, context, new Handler());

		setFocusable(true); // make sure we get key events
	}
	public JezzView(Context context)
	{
		super(context);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		
		// create thread only; it's started in surfaceCreated()
		thread = new JezzThread(holder, context, new Handler());

		setFocusable(true); // make sure we get key events
	}

	/**
	 * Fetches the animation thread corresponding to this LunarView.
	 * 
	 * @return the animation thread
	 */
	public JezzThread getThread()
	{
		return thread;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		holder.addCallback(this);

				// create thread only; it's started in surfaceCreated()
		thread = new JezzThread(holder, jContext, new Handler());
		thread.start();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		GameParameters.setScreenWidth(holder.getSurfaceFrame().width());
		GameParameters.setScreenHeight(holder.getSurfaceFrame().height());
		GameParameters.setScreenArea(GameParameters.getScreenHeight() * GameParameters.getScreenWidth());
		Log.d(Integer.toString(holder.getSurfaceFrame().width()), Integer.toString(holder.getSurfaceFrame().height()));
		
		//Initializing the jezzballs
		GameParameters.jezzBalls = new Ball[GameParameters.getNumberOfBalls()];
		// register our interest in hearing about changes to our surface
		for(int i = 0; i < GameParameters.getNumberOfBalls(); i++)
		{
			GameParameters.jezzBalls[i] = new Ball(holder, jContext, new Handler());
			GameParameters.jezzBalls[i].doStart();
		}
		
		thread.setRunning(true);
		thread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		thread.setRunning(false);
		while (retry)
		{
			try
			{
				thread.join();
				retry = false;
			} catch (InterruptedException e)
			{
			}
		}

	}

}
