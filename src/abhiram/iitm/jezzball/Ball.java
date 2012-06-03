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
			Line hl = getHorizLineBetween(currentY, currentY + velocityY * elapsedTime, currentX, currentX + velocityX * elapsedTime );
			Line vl = getVertLineBetween(currentX, currentX + velocityX * elapsedTime, currentY, currentY + velocityY * elapsedTime );
			
			if( hl == null )
			{
				currentX += velocityX * elapsedTime;
			}
			else
			{
				negateVelocity(false);
				if(!hl.isThisLineIsFixed())
				{
					GameParameters.clearRecentLine();
				}
			}
			if( vl == null )
			{
				currentY += velocityY * elapsedTime;
			}
			else
			{
				negateVelocity(true);
				if(!vl.isThisLineIsFixed())
				{
					GameParameters.clearRecentLine();
				}
			}

			if (currentX >= GameParameters.getScreenWidth() - jBallBitmap.getWidth() && velocityX > 0) velocityX *= -1;
			if (currentX <= 0 && velocityX < 0) velocityX *= -1;
			if (currentY <= 0 && velocityY < 0) velocityY *= -1;
			if (currentY >= GameParameters.getScreenHeight() - jBallBitmap.getHeight() && velocityY > 0) velocityY *= -1;
			
			/** Setting the last time now to now  :D */
			jLastTime = System.currentTimeMillis();
		}

	}
	
	/** This function retrieves a line if it exists in the between the current point of the ball and the next point of the ball
	 * @Params : The current and next coordinates of the ball 
	 * @return : Line if it exists/ null otherwise */
	
	public Line getHorizLineBetween(double y1, double y2, double x1, double x2)
	{
		for(int i = 0; i < GameParameters.linesOnScreen; i++)
		{
			Line l = GameParameters.line.get(i);
			//If l is a horizontal line 
			if(l.isHorizontalLine())
			{
				float y = l.getOriginY();
				/* 
				
									BALL_INIT_CASE_1
					Top stroke ---------------------------------------------------
									BALL_FINAL_CASE_1 - here or below
					Origin Line --------------------------------------------------
															BALL_FINAL_CASE_2 - here or above
					Bottom stroke ------------------------------------------------
															BALL_INIT_CASE_2
															
					# If any one of the above two cases is true, we return the line if the line is fixed.
					# If the line is being constructed, whether the ball(new coordinate) actually touches the line 
					is checked, and is returned only if so
			
				 */
				if( y2 > y1 )
				{
					if( y1 + GameParameters.getBallHeight() < y - GameParameters.LINE_STROKE_WIDTH/2 
						&& y2 + GameParameters.getBallHeight() > y - GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) return l;
						else
						{
							//does the ball actually touch the line being constructed? 
							if(x2 > l.getCurrentLeftX() && x2 < l.getCurrentRightX() )
								return l;
						}
					}
				}
				else
				{
					if( y1 > y + GameParameters.LINE_STROKE_WIDTH/2 && y2 <y + GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) return l;
						else
						{
							//does the ball actually touch the line being constructed? 
							if(x2 > l.getCurrentLeftX() && x2 < l.getCurrentRightX() )
								return l;
						}
					}
				}
			}
		}
		return null;
	}
	
	/** This function retrieves a line if it exists in the between the current point of the ball and the next point of the ball
	 * @Params : The current and next coordinates of the ball 
	 * @return : Line if it exists / null otherwise */
	
	public Line getVertLineBetween(double x1, double x2, double y1, double y2)
	{
		for(int i = 0; i < GameParameters.linesOnScreen; i++)
		{
			Line l = GameParameters.line.get(i);
			//If l is a vertical line
			if(!l.isHorizontalLine())
			{
				/*
				 # View the diagram below sideways to understand the working 
				
								BALL_INIT_CASE_1
				Top stroke ---------------------------------------------------
								BALL_FINAL_CASE_1 - here or below
				Origin Line --------------------------------------------------
														BALL_FINAL_CASE_2 - here or above
				Bottom stroke ------------------------------------------------
														BALL_INIT_CASE_2
														
														
				# If any one of the above two cases is true, we return the line if the line is fixed.
				# If the line is being constructed, whether the ball(new coordinate) actually touches the line 
				is checked, and is returned only if so
				# similar to the horizontal line procedure, just changing x's and y's
				*/
				
				
				float x = l.getOriginX();
				if( x2 > x1 )
				{
					if( x1 + GameParameters.getBallWidth() < x - GameParameters.LINE_STROKE_WIDTH/2
						&& x2 + GameParameters.getBallWidth() > x - GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) return l;
						else
						{
							if( y2 > l.getCurrentLeftY() && y2 < l.getCurrentRightY() ) 
								return l;
						}
					}
				}
				else
				{
					if( x1 > x + GameParameters.LINE_STROKE_WIDTH/2 && x2 < x + GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) return l;
						else
						{
							if( y2 > l.getCurrentLeftY() && y2 < l.getCurrentRightY() ) 
								return l;
						}
					}
				}
			}
		}
		return null;
	}
	/*---------------------------------------------------*/
	/*			OUTER ACCESS FUNCTIONS					 */
	/*---------------------------------------------------*/
	public void negateVelocity(boolean x)
	{
		if(x) this.velocityX *= -1;
		else this.velocityY *= -1;
	}

	public double getVelocityX()
	{
		return velocityX;
	}
	public void setVelocityX(double velocityX)
	{
		this.velocityX = velocityX;
	}
	public double getVelocityY()
	{
		return velocityY;
	}
	public void setVelocityY(double velocityY)
	{
		this.velocityY = velocityY;
	}
	public float getCurrentX()
	{
		return this.currentX;
	}
	public float getCurrentY()
	{
		return this.currentY;
	}
	
}
