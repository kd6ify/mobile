package com.futureconcepts.jupiter.layers;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.osa.android.droyd.map.AreaLayerStyle;
import com.osa.android.droyd.map.LineLayerStyle;

import android.graphics.Color;

public class Pen
{
	private int mColor = Color.BLACK;
	private float mWidth = 1.0f;
	private String mStyle = "Solid";
	//	Pen style is a .NET DashStyle: Solid, Dash, Dot, DashDot, DashDotDot, Custom
	
	public Pen(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		mWidth = Float.parseFloat(xpp.getAttributeValue(null, "width"));
		mStyle = xpp.getAttributeValue(null, "style");
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("PenColor"))
				{
					mColor = ColorParser.parse(xpp);
					xpp.next();
				}
			}
			eventType = xpp.next();
		}
	}
	
	public int getColor()
	{
		return mColor;
	}
	
	public float getWidth()
	{
		return mWidth;
	}
	
	public String getStyle()
	{
		return mStyle;
	}
	
	public void applyToLineLayerStyle(LineLayerStyle style)
	{
		style.width = getWidth();
		style.fillColor = new com.osa.android.droyd.map.Color(Color.red(mColor), Color.green(mColor), Color.blue(mColor));
	}
	
	public void applyBorder(AreaLayerStyle style)
	{
		style.borderColor = new com.osa.android.droyd.map.Color(Color.red(mColor), Color.green(mColor), Color.blue(mColor));
		style.borderWidth = mWidth;
	}
}
