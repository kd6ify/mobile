package com.futureconcepts.ax.model.data;

import android.graphics.Color;

public class JournalEntryPriorityBinding
{
	public static final String PRIORITY_LOW = "Low";
	public static final String PRIORITY_HIGH = "High";
	public static final String PRIORITY_IMMEDIATE = "Immediate";

	public static int stringToInt(String value)
	{
		int result = 0;
		if (value.equals(PRIORITY_LOW))
		{
			result = 0;
		}
		else if (value.equals(PRIORITY_HIGH))
		{
			result = 1;
		}
		else if (value.equals(PRIORITY_IMMEDIATE))
		{
			result = 2;
		}
		return result;
	}
	public static String intToString(int value)
	{
		String result = PRIORITY_LOW;
		if (value == 0)
		{
			result = PRIORITY_LOW;
		}
		else if (value == 1)
		{
			result = PRIORITY_HIGH;
		}
		else if (value == 2)
		{
			result = PRIORITY_IMMEDIATE;
		}
		return result;
	}
	
	public static int getPriorityColor(String priorityString, int priorityInt)
	{
		if (PRIORITY_HIGH.equals(priorityString) || stringToInt(PRIORITY_HIGH) == priorityInt) {
			return Color.argb(255, 255, 153, 51);
		} else if (PRIORITY_IMMEDIATE.equals(priorityString) ||stringToInt(PRIORITY_IMMEDIATE)== priorityInt) {
			return Color.RED;
		} else {
			return Color.WHITE;
		}
	}
}
