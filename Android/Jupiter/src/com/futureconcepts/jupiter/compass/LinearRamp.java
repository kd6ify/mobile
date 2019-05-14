package com.futureconcepts.jupiter.compass;

public class LinearRamp extends ParameterRamp {

	protected float slope;
	protected float offset;
	
	public LinearRamp(float start, float target, long totalTimeMS)
	{
		super(start, target, totalTimeMS);
	}

	@Override
	protected void onRetarget(float target, long totalTimeMS)
	{
		this.slope = (target - this.current) / (float)totalTimeMS;
		this.offset = this.current;
	}

	@Override
	protected float calculate(long elapsedMS)
	{
		return slope * (float)elapsedMS + offset;
	}
}
