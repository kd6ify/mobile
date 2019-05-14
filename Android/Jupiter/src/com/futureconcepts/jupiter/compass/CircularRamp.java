package com.futureconcepts.jupiter.compass;

public class CircularRamp extends LinearRamp
{
	public CircularRamp(float start, float target, long totalTimeMS)
	{
		super(start, target, totalTimeMS);
	}
	
	@Override
	protected void onRetarget(float target, long totalTimeMS)
	{
		super.onRetarget(target, totalTimeMS);
		
		float maxSlope = 180.0f / (float)totalTimeMS;

		if(slope < (-1 * maxSlope))
		{
			slope = ((target) + (360.0f - current)) / (float)totalTimeMS;
		}
		else if(super.slope > maxSlope)
		{
			slope = (Util.wrapTo360(current) - Util.wrapTo360(target)) / (float)totalTimeMS;
		}
	}

	
	@Override
	protected float calculate(long elapsedMS) 
	{
		return Util.wrapTo360(super.calculate(elapsedMS));
	}
}
