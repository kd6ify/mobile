package com.futureconcepts.jupiter.layers;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.osa.android.droyd.map.AreaLayerStyle;

import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;

public class SolidBrush implements BaseBrush
{
	public int color = Color.BLACK;
	
	public SolidBrush(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("BrushColor"))
				{
					color = ColorParser.parse(xpp);
					xpp.next();
				}
			}
			eventType = xpp.next();
		}
	}

	@Override
	public void applyToDrawable(ShapeDrawable drawable)
	{
		drawable.getPaint().setColor(color);
		drawable.getPaint().setStyle(Style.STROKE);
	}

	@Override
	public void fillArea(AreaLayerStyle style)
	{
		com.osa.android.droyd.map.Color fillColor = new com.osa.android.droyd.map.Color();
		fillColor.red = (float)Color.red(color) / 255.0f;
		fillColor.green = (float)Color.green(color) / 255.0f;
		fillColor.blue = (float)Color.blue(color) / 255.0f;
		fillColor.alpha = (float)Color.alpha(color) / 255.0f;
		style.fillColor = fillColor;
	}
}
