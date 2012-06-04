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
	
	/* This is redundant - because the line itself is destroyed on ball touch - just kept for safekeeping */
	private boolean isNotDestroyed;
	
	/** boolean to determine fixature nature of the coordinates of the line */
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
						Line vl = getVertLineBetween(currentLeftX, currentLeftX - lineVelocity * elapsedTime, currentLeftY, currentLeftY);
						
						if(vl == null) currentLeftX -= lineVelocity * elapsedTime;
						else
						{
							currentLeftX -= lineVelocity * elapsedTime;
							currentLeftXFixed = true;
						}
						/*
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(!l.isHorizontalLine())
							{
								if(currentLeftX < l.getOriginX() + GameParameters.LINE_STROKE_WIDTH/2 )
									currentLeftXFixed = true;
							}
						}*/
						if(currentLeftX <= 0) currentLeftXFixed = true;
					}
					if(!currentRightXFixed)
					{
						Line vl = getVertLineBetween(currentRightX, currentRightX + lineVelocity * elapsedTime, currentRightY, currentRightY);
						
						if(vl == null) currentRightX += lineVelocity * elapsedTime;
						else
						{
							currentRightX += lineVelocity * elapsedTime;
							currentRightXFixed = true;
						}
						
						/*
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(!l.isHorizontalLine())
							{
								if(currentRightX  > l.originX - GameParameters.LINE_STROKE_WIDTH/2 )
									currentRightXFixed = true;
							}
						}*/
						if(currentRightX >= GameParameters.getScreenWidth()) currentRightXFixed = true;
					}
					
					
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightXFixed && currentLeftXFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
					}
				}
				
			}
			else
			{
				if(!thisLineIsFixed)
				{
					if(!currentLeftYFixed)
					{
						Line hl = getHorizLineBetween(currentLeftY, currentLeftY - lineVelocity * elapsedTime, currentLeftX, currentLeftX);
						
						if(hl == null) currentLeftY -= lineVelocity * elapsedTime;
						else
						{
							currentLeftY -= lineVelocity * elapsedTime;
							currentLeftYFixed = true;
						}
						
						/*
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(l.isHorizontalLine())
							{
								if(currentLeftY < l.originY + GameParameters.LINE_STROKE_WIDTH/2 )
									currentLeftYFixed = true;
							}
						}*/
						if(currentLeftY <= 0) currentLeftYFixed = true;
					}
					if(!currentRightYFixed)
					{
						Line hl = getHorizLineBetween(currentRightY, currentRightY + lineVelocity * elapsedTime, currentRightX, currentRightX);
						
						if(hl == null)currentRightY += lineVelocity * elapsedTime;
						else
						{
							currentRightY += lineVelocity * elapsedTime;
							currentRightYFixed = true;
						}
						
						/*
						for(int i = 0; i < GameParameters.linesFixed; i++)
						{
							Line l = GameParameters.line.get(i);
							if(l.isHorizontalLine())
							{
								if(currentRightY  > l.originY - GameParameters.LINE_STROKE_WIDTH/2 )
									currentRightYFixed = true;
							}
						}*/
						if(currentRightY >= GameParameters.getScreenHeight()) currentRightYFixed = true;
					}
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightYFixed && currentLeftYFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
					}
				}
			}
			
			/** Setting the last time now to now  :D */
			jLastTime = System.currentTimeMillis();
		}

	}
	public Line getHorizLineBetween(double y1, double y2, double x1, double x2)
	{
		for(int i = 0; i < GameParameters.linesOnScreen; i++)
		{
			Line l = GameParameters.line.get(i);
			//If l is a horizontal line 
			if(l.isHorizontalLine())
			{
				float y = l.getOriginY();
				if( y2 > y1 )
				{
					if( y1  < y - GameParameters.LINE_STROKE_WIDTH/2 
						&& y2 > y - GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) 
							//does the line actually touch the line being constructed? 
							if(x2 > l.getCurrentLeftX() && x2 < l.getCurrentRightX() )
								return l;
						
					}
				}
				else
				{
					if( y1 > y + GameParameters.LINE_STROKE_WIDTH/2 && y2 <y + GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) 
							//does the line actually touch the line being constructed? 
							if(x2 > l.getCurrentLeftX() && x2 < l.getCurrentRightX() )
								return l;
						
					}
				}
			}
		}
		return null;
	}
	public Line getVertLineBetween(double x1, double x2, double y1, double y2)
	{
		for(int i = 0; i < GameParameters.linesOnScreen; i++)
		{
			Line l = GameParameters.line.get(i);
			//If l is a vertical line
			if(!l.isHorizontalLine())
			{
				
				float x = l.getOriginX();
				if( x2 > x1 )
				{
					if( x1  < x - GameParameters.LINE_STROKE_WIDTH/2
						&& x2  > x - GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) 
							if( y2 > l.getCurrentLeftY() && y2 < l.getCurrentRightY() ) 
								return l;
						
					}
				}
				else
				{
					if( x1 > x + GameParameters.LINE_STROKE_WIDTH/2 && x2 < x + GameParameters.LINE_STROKE_WIDTH/2)
					{
						if(l.isThisLineIsFixed()) 
							if( y2 > l.getCurrentLeftY() && y2 < l.getCurrentRightY() ) 
								return l;
						
					}
				}
			}
		}
		return null;
	}
	/*---------------------------------------------------*/
	/*			OUTER ACCESS FUNCTIONS					 */
	/*---------------------------------------------------*/
	public boolean isHorizontalLine()
	{
		return horizontalLine;
	}
	public float getCurrentLeftX()
	{
		return currentLeftX;
	}
	public void setCurrentLeftX(float currentLeftX)
	{
		this.currentLeftX = currentLeftX;
	}
	public float getCurrentRightX()
	{
		return currentRightX;
	}
	public void setCurrentRightX(float currentRightX)
	{
		this.currentRightX = currentRightX;
	}
	public float getCurrentLeftY()
	{
		return currentLeftY;
	}
	public void setCurrentLeftY(float currentLeftY)
	{
		this.currentLeftY = currentLeftY;
	}
	public float getCurrentRightY()
	{
		return currentRightY;
	}
	public void setCurrentRightY(float currentRightY)
	{
		this.currentRightY = currentRightY;
	}
	public float getOriginX()
	{
		return originX;
	}
	public void setOriginX(float originX)
	{
		this.originX = originX;
	}
	public float getOriginY()
	{
		return originY;
	}
	public void setOriginY(float originY)
	{
		this.originY = originY;
	}
	public boolean isThisLineIsFixed()
	{
		return thisLineIsFixed;
	}
	public void setThisLineIsFixed(boolean thisLineIsFixed)
	{
		this.thisLineIsFixed = thisLineIsFixed;
	}
	public void setHorizontalLine(boolean horizontalLine)
	{
		this.horizontalLine = horizontalLine;
	}
}
