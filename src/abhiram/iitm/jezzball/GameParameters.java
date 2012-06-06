package abhiram.iitm.jezzball;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class GameParameters
{
	/** UserAction is 1 if horizontal, 0 if vertical */
	public static final Object lock = new Object();
	
	private static boolean userAction;
	
	/*
	 * Difficulty setting constants
	 */
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_HARD = 50;
	public static final int DIFFICULTY_MEDIUM = 2;

	/*
	 * Physics constants
	 */
	public static final int PHYS_VEL = 130;
	public static final int LINE_VEL = 150;
	/*
	 * State-tracking constants
	 */
	public static final int STATE_LOSE = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;
	public static final int STATE_WIN = 5;
	public static int LIVES = 10;
	
	public static final int LINE_STROKE_WIDTH = 10;
	
	/** Make sure this is initialized first before anything to do with line is called. */
	private static Bitmap jBallBitmap; 
	private static double ballWidth;
	private static double ballHeight;
	
	private static int screenWidth;
	private static int screenHeight;
	
	private static int screenArea;
	private static float areaConquered;
	
	public static void resetAreaConquered()
	{
		areaConquered = 0;
	}
	public static float getAreaConquered()
	{
		return areaConquered;
	}
	public static void addToAreaConquered(float s)
	{
		areaConquered += s;
	}
	

	public static Ball[] jezzBalls;
	private static int numberOfBalls = 4;
	
	/** Currently only one line TODO: Allow multiple lines */
	public static ArrayList<Line> line = new ArrayList<Line>();
	/** The number of lines successfully completed, also indicated the number upto which the arraylist must be left unconcerned */
	public static int linesFixed = 0;
	public static int linesOnScreen = 0;

	public static int getNumberOfBalls()
	{
		return numberOfBalls;
	}

	public static void setNumberOfBalls(int numberOfBalls)
	{
		GameParameters.numberOfBalls = numberOfBalls;
	}

	public static boolean getUserAction()
	{
		return userAction;
	}

	public static void setUserAction(boolean userAction)
	{
		GameParameters.userAction = userAction;
	}
	public static void clearRecentLine()
	{
		synchronized(lock)
		{
			line.remove(line.size() - 1);
			linesOnScreen --;
		}
	}

	public static int getScreenWidth()
	{
		return screenWidth;
	}

	public static void setScreenWidth(int screenWidth)
	{
		GameParameters.screenWidth = screenWidth;
	}

	public static int getScreenHeight()
	{
		return screenHeight;
	}

	public static void setScreenHeight(int screenHeight)
	{
		GameParameters.screenHeight = screenHeight;
	}

	public static double getBallWidth()
	{
		return ballWidth;
	}

	public static void setBallWidth(double ballWidth)
	{
		GameParameters.ballWidth = ballWidth;
	}

	public static double getBallHeight()
	{
		return ballHeight;
	}

	public static void setBallHeight(double ballHeight)
	{
		GameParameters.ballHeight = ballHeight;
	}

	public static Bitmap getjBallBitmap()
	{
		return jBallBitmap;
	}

	public static void setjBallBitmap(Bitmap jBallBitmap)
	{
		GameParameters.jBallBitmap = jBallBitmap;
	}

	public static int getScreenArea()
	{
		return screenArea;
	}

	public static void setScreenArea(int screenArea)
	{
		GameParameters.screenArea = screenArea;
	}
	
	public static boolean isValidLineStart(float startX, float startY)
	{
		for(int i = 0; i < linesFixed; i++)
		{
			Line l = line.get(i);
			if(l.isHorizontalLine())
			{
				if(Math.abs(l.getOriginY() - startY) <= GameParameters.LINE_STROKE_WIDTH/2)
					if(startX > l.getCurrentLeftX() && startX < l.getCurrentRightX())
						return false;
			}
			else
			{
				if(Math.abs(l.getOriginX() - startX) <= GameParameters.LINE_STROKE_WIDTH/2)
					if(startY > l.getCurrentLeftY() && startY < l.getCurrentRightY())
						return false;
			}
			if(l.isColorTop())
			{
				float maxX = Math.max(l.getPartitionTop().x1, l.getPartitionTop().x2);
				float minX = Math.min(l.getPartitionTop().x1, l.getPartitionTop().x2);
				float maxY = Math.max(l.getPartitionTop().y1, l.getPartitionTop().y2);
				float minY = Math.min(l.getPartitionTop().y1, l.getPartitionTop().y2);
				
				if(minX < startX && startX < maxX && minY < startY && startY < maxY)
					return false;
			}
			if(l.isColorBottom())
			{
				float maxX = Math.max(l.getPartitionBottom().x1, l.getPartitionBottom().x2);
				float minX = Math.min(l.getPartitionBottom().x1, l.getPartitionBottom().x2);
				float maxY = Math.max(l.getPartitionBottom().y1, l.getPartitionBottom().y2);
				float minY = Math.min(l.getPartitionBottom().y1, l.getPartitionBottom().y2);
				
				if(minX < startX && startX < maxX && minY < startY && startY < maxY)
					return false;
			}
		}
		return true;
	}
}
