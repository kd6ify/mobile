package com.futureconcepts.jupiter.compass;

import android.util.Log;

public abstract class ParameterRamp
{
	private static String TAG = "Ramp";

	protected float current;
	protected float max;
	
	protected long totalTime;
	
	protected long startTime;
	
	protected boolean complete;
	
	public ParameterRamp(float start, float target, long totalTimeMS)
	{
		this.complete = false;
		
		this.startTime = 0;
		
		this.current = start;

		retarget(target, totalTimeMS);
	}
	
	public void retarget(float target, long totalTimeMS)
	{
		synchronized(this)
		{
			//Log.d(TAG, "retarget > " + target + " (currently at " + current + ")");
			this.complete = false;
			
			this.max = target;
			this.totalTime = totalTimeMS;
			
			onRetarget(target, totalTimeMS);
			
			startTime = System.currentTimeMillis();
		}
	}
	
	protected abstract void onRetarget(float target, long totalTimeMS);
		
	protected abstract float calculate(long elapsedMS);
	
	public float updateValue()
	{
		synchronized(this)
		{
			if(complete)
			{
			//	Log.d(TAG, "updateValue = " + max + "  --complete--");
				return max;
			}
			
			if(startTime == 0)
			{
				startTime = System.currentTimeMillis();
			}
	
			long curTime = System.currentTimeMillis();
			long elapsed = curTime - startTime;
			if(elapsed >= totalTime)
			{
				current = max;
				complete = true;
			}
			else
			{
				current = calculate(elapsed);
			}
	
			//Log.d(TAG, "updateValue = " + current + "  @" + elapsed + " / " + totalTime);
			
			return current;
		}
	}
	
	public float value()
	{
		return current;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
}
