package abhiram.iitm.jezzball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
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
	private Paint q;
	
	/* This is redundant - because the line itself is destroyed on ball touch - just kept for safekeeping */
	private boolean isNotDestroyed;
	
	/** boolean to determine fixature nature of the coordinates of the line */
	private boolean currentLeftXFixed;
	private boolean currentRightXFixed;
	private boolean currentLeftYFixed;
	private boolean currentRightYFixed;
	
	private boolean thisLineIsFixed;
	
	/* Left fixed and top fixed are synonymous for horizontal and vertical lines 
	 * This is true for any left/top right/bottom synonimity */
	private boolean isLeftFixedToScreen;
	private boolean isRightFixedToScreen;
	
	private Line lineLeftFixedTo;
	private Line lineRightFixedTo;
	
	private boolean pendingColor;
	
	private Rectangle partitionTop;//partitionLeft syn
	private Rectangle partitionBottom;//partitionRight syn
	
	private boolean colorTop;//left syn
	private boolean colorBottom;//right syn

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
		synchronized (GameParameters.lock)
		{
			lineVelocity = GameParameters.LINE_VEL;

			// initial random location for the ball
			screenWidth = jSurfaceHolder.getSurfaceFrame().width();
			screenHeight = jSurfaceHolder.getSurfaceFrame().height();
			
			p = new Paint();
			p.setColor(Color.GRAY);
			p.setStrokeWidth(GameParameters.LINE_STROKE_WIDTH);
			
			q = new Paint();
			q.setColor(Color.GRAY);
			
			jLastTime = System.currentTimeMillis();
			
			isNotDestroyed = true;
			currentLeftXFixed = false;
			currentRightXFixed = false;
			currentLeftYFixed = false;
			currentRightYFixed = false;
			
			thisLineIsFixed = false;
			isLeftFixedToScreen = false;
			isRightFixedToScreen = false;
			lineLeftFixedTo = null;
			lineRightFixedTo = null;
			
			pendingColor = false;
			colorTop = false;
			colorBottom = false;
		}
	}
	
	public void doDraw(Canvas canvas)
	{
		if(isNotDestroyed) 
		{
			
			canvas.drawLine(currentLeftX, currentLeftY, currentRightX, currentRightY, p);
			if(colorTop)
			{
				float maxX = Math.max(partitionTop.x1, partitionTop.x2);
				float minX = Math.min(partitionTop.x1, partitionTop.x2);
				float maxY = Math.max(partitionTop.y1, partitionTop.y2);
				float minY = Math.min(partitionTop.y1, partitionTop.y2);
				canvas.drawRect(minX, minY, maxX, maxY, q);
				
			}
			if(colorBottom)
			{
				float maxX = Math.max(partitionBottom.x1, partitionBottom.x2);
				float minX = Math.min(partitionBottom.x1, partitionBottom.x2);
				float maxY = Math.max(partitionBottom.y1, partitionBottom.y2);
				float minY = Math.min(partitionBottom.y1, partitionBottom.y2);
				canvas.drawRect(minX, minY, maxX, maxY, q);
			}
			
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
							currentLeftX = vl.originX;
							currentLeftXFixed = true;
							
							lineLeftFixedTo = vl;
						}
						if(currentLeftX <= 0) 
						{
							currentLeftX = 0;
							currentLeftXFixed = true;
							isLeftFixedToScreen = true;
						}
					}
					if(!currentRightXFixed)
					{
						Line vl = getVertLineBetween(currentRightX, currentRightX + lineVelocity * elapsedTime, currentRightY, currentRightY);
						
						if(vl == null) currentRightX += lineVelocity * elapsedTime;
						else
						{
							currentRightX = vl.originX;
							currentRightXFixed = true;
							
							lineRightFixedTo = vl;
						}
						
						if(currentRightX >= GameParameters.getScreenWidth()) 
						{
							currentRightX = GameParameters.getScreenWidth();
							currentRightXFixed = true;
							isRightFixedToScreen = true;
						}
					}
					
					
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightXFixed && currentLeftXFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
						
						/** Now this fixed line has created a partition. One of them may need to be colored */
						float x1, y1, x2, y2;
						//If the current line is obstructed by another line to the right
						if(!isRightFixedToScreen)
						{
							x1 = lineRightFixedTo.currentLeftX;
							y1 = lineRightFixedTo.currentLeftY;
							
							
							x2 = lineRightFixedTo.currentRightX;
							y2 = lineRightFixedTo.currentRightY;
							
							//checkForInBetweenLinesAndUpdate y1,y2
							for(int i = 0; i < GameParameters.linesFixed; i++)
							{
								Line l = GameParameters.line.get(i);
								if(l.isHorizontalLine())
								{
									if(l.currentLeftX == this.currentLeftX && l.lineRightFixedTo.equals(this.lineRightFixedTo))
									{
										//leftY and rightY are same here 
										if(l.currentRightY > y1 && l.currentRightY < this.currentRightY)
											y1 = l.currentRightY;
										if(l.currentRightY > this.currentRightY && l.currentRightY < y2)
											y2 = l.currentRightY;
									}
									
								}
							}
							
							
							partitionTop = new Rectangle(this.currentLeftX, this.currentLeftY, x1, y1);
							partitionBottom = new Rectangle(this.currentLeftX, this.currentLeftY, x2, y2);
						}
						else
						{
							//if the line is obstructed by another line to the left
							if(!isLeftFixedToScreen)
							{
								x1 = lineLeftFixedTo.currentLeftX;
								y1 = lineLeftFixedTo.currentLeftY;
								x2 = lineLeftFixedTo.currentRightX;
								y2 = lineLeftFixedTo.currentRightY;
								
								//checkForInBetweenLinesAndUpdate y1,y2
								for(int i = 0; i < GameParameters.linesFixed; i++)
								{
									Line l = GameParameters.line.get(i);
									if(l.isHorizontalLine())
									{
										if(l.currentRightX == this.currentRightX && l.lineLeftFixedTo.equals(this.lineLeftFixedTo))
										{
											//gen replacing leftY for rightY - all the same y
											if(l.currentLeftY > y1 && l.currentLeftY < this.currentLeftY)
												y1 = l.currentLeftY;
											if(l.currentLeftY > this.currentLeftY && l.currentLeftY < y2)
												y2 = l.currentLeftY;
										}
										
									}
								}
								
								
								partitionTop = new Rectangle(this.currentRightX, this.currentRightY, x1, y1);
								partitionBottom = new Rectangle(this.currentRightX, this.currentRightY, x2, y2);
							}
							//if it touches the screen wall on both sides
							else
							{
								Log.d("Screenwidth", Float.toString(GameParameters.getScreenWidth()));
								partitionTop = new Rectangle(this.currentLeftX, this.currentLeftY, GameParameters.getScreenWidth(), 0);
								partitionBottom = new Rectangle(this.currentLeftX, this.currentLeftY, GameParameters.getScreenWidth(), GameParameters.getScreenHeight());
							}
						}
						//TODO : Check if there is a ball in each of these partitions - color otherwise
						colorTop = true;
						colorBottom = true;
						for(int i = 0; i < GameParameters.getNumberOfBalls(); i++)
						{
							Ball b = GameParameters.jezzBalls[i];
							float maxX = Math.max(partitionTop.x1, partitionTop.x2);
							float minX = Math.min(partitionTop.x1, partitionTop.x2);
							float maxY = Math.max(partitionTop.y1, partitionTop.y2);
							float minY = Math.min(partitionTop.y1, partitionTop.y2);
							
							if(minX < b.getCurrentX() && b.getCurrentX() < maxX
								&& minY < b.getCurrentY() && b.getCurrentY() < maxY)
								colorTop = false;
							
							maxX = Math.max(partitionBottom.x1, partitionBottom.x2);
							minX = Math.min(partitionBottom.x1, partitionBottom.x2);
							maxY = Math.max(partitionBottom.y1, partitionBottom.y2);
							minY = Math.min(partitionBottom.y1, partitionBottom.y2);
							
							if(minX < b.getCurrentX() && b.getCurrentX() < maxX
								&& minY < b.getCurrentY() && b.getCurrentY() < maxY)
								colorBottom = false;
						}
						
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
							currentLeftY = hl.originY;
							currentLeftYFixed = true;
							lineLeftFixedTo = hl;
						}
						
						if(currentLeftY <= 0) 
						{
							currentLeftY = 0;
							isLeftFixedToScreen = true;
							currentLeftYFixed = true;
						}
					}
					if(!currentRightYFixed)
					{
						Line hl = getHorizLineBetween(currentRightY, currentRightY + lineVelocity * elapsedTime, currentRightX, currentRightX);
						
						if(hl == null)currentRightY += lineVelocity * elapsedTime;
						else
						{
							currentRightY = hl.originY;
							currentRightYFixed = true;
							lineRightFixedTo = hl;
						}
						
						if(currentRightY >= GameParameters.getScreenHeight()) 
						{
							currentRightY = GameParameters.getScreenHeight();
							isRightFixedToScreen = true;
							currentRightYFixed = true;
						}
					}
					/** If both the left and right are fixed, then the total line is now fixed. Add 1 to linesFixed. It will take care of the rest */
					if(currentRightYFixed && currentLeftYFixed) 
					{
						thisLineIsFixed = true;
						GameParameters.linesFixed ++;
						
						/** Now this fixed line has created a partition. One of them may need to be colored */
						float x1, y1, x2, y2;
						//If the current line is obstructed by another line to the right
						if(!isRightFixedToScreen)
						{
							x1 = lineRightFixedTo.currentLeftX;
							y1 = lineRightFixedTo.currentLeftY;
							x2 = lineRightFixedTo.currentRightX;
							y2 = lineRightFixedTo.currentRightY;
							
							//updating x1 and x2 with inbetween lines 
							for(int i = 0; i < GameParameters.linesFixed; i++)
							{
								Line l = GameParameters.line.get(i);
								if(!l.isHorizontalLine())
								{
									if(l.currentLeftX == this.currentLeftX && l.lineRightFixedTo.equals(this.lineRightFixedTo))
									{
										//leftX and rightX are same here 
										if(l.currentRightX > x1 && l.currentRightX < this.currentRightX)
											x1 = l.currentRightX;
										if(l.currentRightX > this.currentRightX && l.currentRightX < x2)
											x2 = l.currentRightX;
									}
									
								}
							}
							
							partitionTop = new Rectangle(this.currentLeftX, this.currentLeftY, x1, y1);
							partitionBottom = new Rectangle(this.currentLeftX, this.currentLeftY, x2, y2);
						}
						else
						{
							//if the line is obstructed by another line to the left
							if(!isLeftFixedToScreen)
							{
								x1 = lineLeftFixedTo.currentLeftX;
								y1 = lineLeftFixedTo.currentLeftY;
								x2 = lineLeftFixedTo.currentRightX;
								y2 = lineLeftFixedTo.currentRightY;
								
								for(int i = 0; i < GameParameters.linesFixed; i++)
								{
									Line l = GameParameters.line.get(i);
									if(!l.isHorizontalLine())
									{
										if(l.currentRightX == this.currentRightX && l.lineLeftFixedTo.equals(this.lineLeftFixedTo))
										{
											//leftX and rightX are same here 
											if(l.currentLeftX > x1 && l.currentLeftX < this.currentLeftX)
												x1 = l.currentLeftX;
											if(l.currentLeftX > this.currentLeftX && l.currentLeftX < x2)
												x2 = l.currentLeftX;
										}
										
									}
								}
								
								partitionTop = new Rectangle(this.currentRightX, this.currentRightY, x1, y1);
								partitionBottom = new Rectangle(this.currentRightX, this.currentRightY, x2, y2);
							}
							//if it touches the screen wall on both sides
							else
							{
								
								partitionTop = new Rectangle(this.currentLeftX, this.currentLeftY, GameParameters.getScreenWidth(), GameParameters.getScreenHeight());
								partitionBottom = new Rectangle(this.currentLeftX, this.currentLeftY, 0, GameParameters.getScreenHeight());
							}
						}
						//TODO : Check if there is a ball in each of these partitions - color otherwise
						colorTop = true;
						colorBottom = true;
						for(int i = 0; i < GameParameters.getNumberOfBalls(); i++)
						{
							Ball b = GameParameters.jezzBalls[i];
							float maxX = Math.max(partitionTop.x1, partitionTop.x2);
							float minX = Math.min(partitionTop.x1, partitionTop.x2);
							float maxY = Math.max(partitionTop.y1, partitionTop.y2);
							float minY = Math.min(partitionTop.y1, partitionTop.y2);
							
							if(minX < b.getCurrentX() && b.getCurrentX() < maxX
								&& minY < b.getCurrentY() && b.getCurrentY() < maxY)
								colorTop = false;
							
							maxX = Math.max(partitionBottom.x1, partitionBottom.x2);
							minX = Math.min(partitionBottom.x1, partitionBottom.x2);
							maxY = Math.max(partitionBottom.y1, partitionBottom.y2);
							minY = Math.min(partitionBottom.y1, partitionBottom.y2);
							
							if(minX < b.getCurrentX() && b.getCurrentX() < maxX
								&& minY < b.getCurrentY() && b.getCurrentY() < maxY)
								colorBottom = false;
						}
						
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
class Rectangle
{
	public float x1, y1, x2, y2;
	Rectangle(float x1, float y1, float x2, float y2)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	

}