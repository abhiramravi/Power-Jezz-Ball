package abhiram.iitm.jezzball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceHolder;

public class Line
{
	/** The points from which the line will be built */
	private float originX;
	private float originY;
	
	/** The boolean representing if the line being drawn is a horizontal/vertical line */
	private boolean horizontalLine;
	
	public boolean isHorizontalLine()
	{
		return horizontalLine;
	}
	public void setHorizontalLine(boolean horizontalLine)
	{
		this.horizontalLine = horizontalLine;
	}
	/** The current positions of the ends of the line */
	private float currentLeftX;
	private float currentRightX;
	private float currentLeftY;
	private float currentRightY;

	/** The surface holder and the context */
	private SurfaceHolder jSurfaceHolder;
	private Context jContext;
	
	/** The velocity with which the line will be built */
	private double lineVelocity;
	
	private double screenWidth;
	private double screenHeight;
	
	private Long jLastTime;
	
	/** The paint variable that defines the style of the line */
	private Paint p;
	
	private boolean isNotDestroyed;
	
	private boolean currentLeftXFixed;
	private boolean currentRightXFixed;
	private boolean currentLeftYFixed;
	private boolean currentRightYFixed;
	
	private boolean thisLineIsFixed;

	public Line(SurfaceHolder surfaceHolder, Context context, Handler handler, boolean userAction, float x, float y)
	{
		// get handles to some important objects
		jSurfaceHolder = surfaceHolder;
		jContext = context;
		
		originX = currentLeftX = currentRightX = x;
		originY = currentLeftY = currentRightY = y;
	
		horizontalLine = userAction;
		
	}
	public void doStart()
	{
		synchronized (jSurfaceHolder)
		{
			lineVelocity = GameParameters.LINE_VEL;

			// initial random location for the ball
			screenWidth = jSurfaceHolder.getSurfaceFrame().width();
			screenHeight = jSurfaceHolder.getSurfaceFrame().height();
			
			p = new Paint();
			p.setColor(Color.GRAY);
			p.setStrokeWidth(GameParameters.LINE_STROKE_WIDTH);
			
			jLastTime = System.currentTimeMillis();
			
			isNotDestroyed = true;
			currentLeftXFixed = false;
			currentRightXFixed = false;
			currentLeftYFixed = false;
			currentRightYFixed = false;
			
			thisLineIsFixed = false;
		}
	}
	
	public void doDraw(Canvas canvas)
	{
		if(isNotDestroyed) 
		{
			canvas.drawLine(currentLeftX, currentLeftY, currentRightX, currentRightY, p);
		}
	}
	public void updatePhysics()
	{
		Long now = System.currentTimeMillis();
		
		
		double elapsedTime = (now - jLastTime)/1000.0;
		// screen parameters
		screenWidth = jSurfaceHolder.getSurfaceFrame().width();
		screenHeight = jSurfaceHolder.getSurfaceFrame().height();

		synchronized (jSurfaceHolder)
		{
			if(horizontalLine) 
			{
				if(!thisLineIsFixed)
				{
					if(!currentLeftXFixed)
					{
						currentLeftX -= lineVelocity * elapsedTime;
						
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(!l.isHorizontalLine())
							{
								if(currentLeftX < l.originX + GameParameters.LINE_STROKE_WIDTH/2 )
									currentLeftXFixed = true;
							}
						}
						if(currentLeftX <= 0) currentLeftXFixed = true;
					}
					if(!currentRightXFixed)
					{
						currentRightX += lineVelocity * elapsedTime;
						
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(!l.isHorizontalLine())
							{
								if(currentRightX  > l.originX - GameParameters.LINE_STROKE_WIDTH/2 )
									currentRightXFixed = true;
							}
						}
						if(currentRightX >= GameParameters.getScreenWidth()) currentRightXFixed = true;
					}
					
					
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightXFixed && currentLeftXFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
					}
				}
				
				/** For each ball, we check if it has collided with the line or not */
				for(int i = 0; i < GameParameters.jezzBalls.length; i++ )
				{
					float ballX = GameParameters.jezzBalls[i].getCurrentX();
					float ballY = GameParameters.jezzBalls[i].getCurrentY();
					
					
					if( currentLeftX < ballX + GameParameters.getBallWidth() && currentRightX > ballX )
						if( (ballY > originY && (ballY - originY) < GameParameters.LINE_STROKE_WIDTH/2) 
							|| (ballY <= originY && originY - ballY - GameParameters.getBallHeight() < GameParameters.LINE_STROKE_WIDTH/2))
						{
							GameParameters.jezzBalls[i].negateVelocity(false);
							if(!thisLineIsFixed) 
							{
								GameParameters.clearRecentLine();
								isNotDestroyed = false;
							}
							break;
						}
				
				}
			}
			else
			{
				if(!thisLineIsFixed)
				{
					if(!currentLeftYFixed)
					{
						currentLeftY -= lineVelocity * elapsedTime;
						
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(l.isHorizontalLine())
							{
								if(currentLeftY < l.originY + GameParameters.LINE_STROKE_WIDTH/2 )
									currentLeftYFixed = true;
							}
						}
						if(currentLeftY <= 0) currentLeftYFixed = true;
					}
					if(!currentRightYFixed)
					{
						currentRightY += lineVelocity * elapsedTime;
						
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(l.isHorizontalLine())
							{
								if(currentRightY  > l.originY - GameParameters.LINE_STROKE_WIDTH/2 )
									currentRightYFixed = true;
							}
						}
						if(currentRightY >= GameParameters.getScreenHeight()) currentRightYFixed = true;
					}
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightYFixed && currentLeftYFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
					}
				}
				for(int i = 0; i < GameParameters.jezzBalls.length; i++ )
				{
					float ballX = GameParameters.jezzBalls[i].getCurrentX();
					float ballY = GameParameters.jezzBalls[i].getCurrentY();
					
					
					if( currentLeftY < ballY + GameParameters.getBallHeight() && currentRightY > ballY )
						if( (ballX > originX && (ballX - originX) < GameParameters.LINE_STROKE_WIDTH/2) 
							|| (ballX <= originX && originX - ballX - GameParameters.getBallWidth() < GameParameters.LINE_STROKE_WIDTH/2))
						{
							GameParameters.jezzBalls[i].negateVelocity(true);
							if(!thisLineIsFixed) 
							{
								GameParameters.clearRecentLine();
								isNotDestroyed = false;
							}
							break;
						}
				
				}
			}
			
			/** Setting the last time now to now  :D */
			jLastTime = System.currentTimeMillis();
		}

	}

	
}
