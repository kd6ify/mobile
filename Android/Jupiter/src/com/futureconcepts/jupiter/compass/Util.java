package com.futureconcepts.jupiter.compass;

public class Util 
{
	private Util() { }
	
	public final static String degreesSymbol = "°";
    
	public static float wrapTo360(float value)
	{
        if (value >= 360)
            return value % 360;
        else if (value < 0)
            return 360 + (value % 360);

        return value;
	}
}
