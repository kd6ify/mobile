package com.futureconcepts.jupiter.compass;

public interface ICompassView
{
	public float getHeading();
	public void setHeading(float degrees);
	
	public float[] getOrientation();
	public void setOrientation(float[] values);
}
