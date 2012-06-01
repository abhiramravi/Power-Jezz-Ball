package abhiram.iitm.jezzball;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class JezzView extends SurfaceView implements SurfaceHolder.Callback
{
	class JezzThread extends Thread
	{
		/*
		 * Difficulty setting constants
		 */
		public static final int DIFFICULTY_EASY = 0;
		public static final int DIFFICULTY_HARD = 1;
		public static final int DIFFICULTY_MEDIUM = 2;

		/*
		 * Physics constants
		 */
		public static final int PHYS_VEL = 35;
		/*
		 * State-tracking constants
		 */
		public static final int STATE_LOSE = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_READY = 3;
		public static final int STATE_RUNNING = 4;
		public static final int STATE_WIN = 5;
		/*
		 * Initial positions of the ball in the game
		 */
		/** Message handler used by thread to interact with TextView */
		private Handler jHandler;

		private Random rand;
		/** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
		private int jMode = STATE_PAUSE;

		public static final int INIT_X = 50;
		public static final int INIT_Y = 50;

		private Drawable jBallDrawable;
		private Bitmap jBallBitmap;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean jRun = false;

		private SurfaceHolder jSurfaceHolder;

		private double jBallVelocity;
		private double jBallVelocityX;
		private double jBallVelocityY;

		private int jDifficulty;

		private int screenWidth;
		private int screenHeight;

		private float jBallX;
		private float jBallY;

		public JezzThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler)
		{
			// get handles to some important objects
			jSurfaceHolder = surfaceHolder;

			jHandler = handler;
			jContext = context;

			Resources res = context.getResources();
			// cache handles to our key sprites & other drawables
			jBallDrawable = context.getResources().getDrawable(R.drawable.ball);

			// load background image as a Bitmap instead of a Drawable b/c
			// we don't need to transform it and it's faster to draw this way
			jBallBitmap = BitmapFactory.decodeResource(res, R.drawable.ball);

			// TODO : Initialize paints for speedometer
			jDifficulty = DIFFICULTY_MEDIUM;

		}

		/**
		 * Starts the game, setting parameters for the current difficulty.
		 */
		public void doStart()
		{
			synchronized (jSurfaceHolder)
			{
				// First set the game for Medium difficulty
				jDifficulty = DIFFICULTY_EASY;
				jBallVelocity = PHYS_VEL;

				// TODO : Adjust difficulty params for EASY/HARD

				// initial location for the ball
				jBallX = INIT_X;
				jBallY = INIT_Y;

				// start with a little random motion
				rand = new Random();
				int randDegrees = rand.nextInt(10);

				jBallVelocityY = Math.cos(randDegrees) * PHYS_VEL;
				jBallVelocityX = Math.sin(randDegrees) * PHYS_VEL;

				setState(STATE_RUNNING);
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
						if (jMode == STATE_RUNNING)
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
					}
				}
			}
		}

		private void doDraw(Canvas canvas)
		{
			// Clearing the screen
			canvas.drawRGB(255, 255, 255);

			// Draw the Ball
			canvas.drawBitmap(jBallBitmap, jBallX, jBallY, null);
		}

		private void updatePhysics()
		{
			// screen parameters
			screenWidth = jSurfaceHolder.getSurfaceFrame().width();
			screenHeight = jSurfaceHolder.getSurfaceFrame().height();

			synchronized (jSurfaceHolder)
			{
				jBallX += jBallVelocityX;
				jBallY += jBallVelocityY;

				if (jBallX >= screenWidth - jBallBitmap.getWidth()
						|| jBallX <= 0)
					jBallVelocityX *= -1;
				if (jBallY >= jSurfaceHolder.getSurfaceFrame().height()
						- jBallBitmap.getHeight()
						|| jBallY <= 0)
					jBallVelocityY *= -1;

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
				if (jMode == STATE_RUNNING)
					setState(STATE_PAUSE);
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
			setState(STATE_RUNNING);
		}
	}

	private Context jContext;
	/** The thread that actually draws the animation */
	private JezzThread thread;

	public JezzView(Context context)
	{
		super(context);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new JezzThread(holder, context, new Handler()
		{
			@Override
			public void handleMessage(Message m)
			{
			}
		});

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
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
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
