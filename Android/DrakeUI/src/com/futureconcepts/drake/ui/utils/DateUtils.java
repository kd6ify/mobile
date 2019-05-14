package com.futureconcepts.drake.ui.utils;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;

public class DateUtils
{
	public static String formatMessageTime(Context context, long millis)
	{
		DateFormat formatter = android.text.format.DateFormat.getTimeFormat(context);
		return formatter.format(new Date(millis));
	}
}
