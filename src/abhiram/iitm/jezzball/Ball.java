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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Ball
{
	/** The current x and y positions of the ball */
	private float currentX;
	private float currentY;

	/** The current velocity of the ball - will depend upon the level - initialized based on PHYS_VEL
	 * the speed of the ball remains contant throughout the level, only the values of velX and velY 
	 * change based on collision with the walls */
	private double velocity;
	private double velocityX;
	private double velocityY;

	/** Very own random number generator */
	private Random rand;

	/** The drawable and bitmap for the ball */
	private Drawable jBallDrawable;
	private Bitmap jBallBitmap;

	/** The surfaceholder used to draw to, obtained from constructor while ball is being constructed */
	private SurfaceHolder jSurfaceHolder;
	private Context jContext;
	
	
	
	private long jLastTime;
	
	public float getCurrentX()
	{
		return this.currentX;
	}
	public float getCurrentY()
	{
		return this.currentY;
	}
	
	public Ball(SurfaceHolder surfaceHolder, Context context,
			Handler handler)
	{
		// get handles to some important objects
		jSurfaceHolder = surfaceHolder;
		jContext = context;
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
			velocity = GameParameters.PHYS_VEL;
			rand = new Random();

			// initial random location for the ball
			GameParameters.setScreenWidth(jSurfaceHolder.getSurfaceFrame().width());
			GameParameters.setScreenHeight(jSurfaceHolder.getSurfaceFrame().height());
			
			currentX = rand.nextInt(100);
			currentY = rand.nextInt(100);

			// start with a little random motion
			
			int randDegrees = rand.nextInt(10);

			velocityY = Math.cos(randDegrees) * GameParameters.PHYS_VEL;
			velocityX = Math.sin(randDegrees) * GameParameters.PHYS_VEL;
			//Make sure that the state is set to running after the dostart() of all balls are called.
			jLastTime = System.currentTimeMillis() + 3000;
		}
	}

	public void doDraw(Canvas canvas)
	{
		// Draw the Ball - NOTE : We don't clear the screen 
		canvas.drawBitmap(jBallBitmap, currentX, currentY, null);
	}

	public void updatePhysics()
	{
		Long now = System.currentTimeMillis();
		
		/** This delays the start of the physics engine by 100 milliseconds or whatever*/
		if(jLastTime > now) return;
		
		double elapsedTime = (now - jLastTime)/1000.0;
		// screen parameters
		GameParameters.setScreenWidth(jSurfaceHolder.getSurfaceFrame().width());
		GameParameters.setScreenHeight(jSurfaceHolder.getSurfaceFrame().height());

		synchronized (jSurfaceHolder)
		{
			currentX += velocityX * elapsedTime;
			currentY += velocityY * elapsedTime;

			if (currentX >= GameParameters.getScreenWidth() - jBallBitmap.getWidth() && velocityX > 0) velocityX *= -1;
			if (currentX <= 0 && velocityX < 0) velocityX *= -1;
			if (currentY <= 0 && velocityY < 0) velocityY *= -1;
			if (currentY >= GameParameters.getScreenHeight() - jBallBitmap.getHeight() && velocityY > 0) velocityY *= -1;
			
			/** Setting the last time now to now  :D */
			jLastTime = System.currentTimeMillis();
		}

	}
	/*---------------------------------------------------*/
	/*			OUTER ACCESS FUNCTIONS					 */
	/*---------------------------------------------------*/
	public void negateVelocity(boolean x)
	{
		if(x) this.velocityX *= -1;
		else this.velocityY *= -1;
	}

}
