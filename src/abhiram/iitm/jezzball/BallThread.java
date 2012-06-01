package abhiram.iitm.jezzball;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.SurfaceHolder;

public class BallThread
{
	private float currentX;
	private float currentY;

	private double velocity;
	private double velocityX;
	private double velocityY;

	private Random rand;

	private Drawable jBallDrawable;
	private Bitmap jBallBitmap;

	private SurfaceHolder jSurfaceHolder;

	private int screenWidth;
	private int screenHeight;

	public BallThread(SurfaceHolder surfaceHolder, Context context,
			Handler handler)
	{
		// get handles to some important objects
		jSurfaceHolder = surfaceHolder;

		Resources res = context.getResources();
		// cache handles to our key sprites & other drawables
		jBallDrawable = context.getResources().getDrawable(R.drawable.ball);

		// load background image as a Bitmap instead of a Drawable b/c
		// we don't need to transform it and it's faster to draw this way
		jBallBitmap = BitmapFactory.decodeResource(res, R.drawable.ball);
	}
	
	public void doStart()
	{
		synchronized (jSurfaceHolder)
		{
			velocity = JezzView.JezzThread.PHYS_VEL;
			rand = new Random();

			// initial random location for the ball
			screenWidth = jSurfaceHolder.getSurfaceFrame().width();
			screenHeight = jSurfaceHolder.getSurfaceFrame().height();
			
			currentX = rand.nextInt(100);
			currentY = rand.nextInt(100);

			// start with a little random motion
			
			int randDegrees = rand.nextInt(10);

			velocityY = Math.cos(randDegrees) * JezzView.JezzThread.PHYS_VEL;
			velocityX = Math.sin(randDegrees) * JezzView.JezzThread.PHYS_VEL;
			//Make sure that the state is set to running after the dostart of all balls are called.
		}
	}

	public void doDraw(Canvas canvas)
	{
		//Not Clearing the screen
		//canvas.drawRGB(255, 255, 255);

		// Draw the Ball
		canvas.drawBitmap(jBallBitmap, currentX, currentY, null);
	}

	public void updatePhysics()
	{
		// screen parameters
		screenWidth = jSurfaceHolder.getSurfaceFrame().width();
		screenHeight = jSurfaceHolder.getSurfaceFrame().height();

		synchronized (jSurfaceHolder)
		{
			currentX += velocityX;
			currentY += velocityY;

			if (currentX >= screenWidth - jBallBitmap.getWidth()
					|| currentX <= 0)
				velocityX *= -1;
			if (currentY >= jSurfaceHolder.getSurfaceFrame().height()
					- jBallBitmap.getHeight()
					|| currentY <= 0)
				velocityY *= -1;

		}

	}

}
