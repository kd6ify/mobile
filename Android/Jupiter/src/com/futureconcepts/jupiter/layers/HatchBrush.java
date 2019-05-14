package com.futureconcepts.jupiter.layers;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.osa.android.droyd.map.AreaLayerStyle;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.Shader;

public class HatchBrush implements BaseBrush
{
	private int _foreColor = Color.BLACK;
	private int _backColor = Color.WHITE;
	public String type = "";
	
	public HatchBrush(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		// for "type" see http://gis.thinkgeo.com/mapsuite3docs/silverlightedition/MapSuiteCore~ThinkGeo.MapSuite.Core.GeoHatchStyle.html
		// or http://gis.thinkgeo.com/mapsuite3docs/desktopedition/MapSuiteCore~ThinkGeo.MapSuite.Core.GeoHatchStyle.html
		type = xpp.getAttributeValue(null, "type");

		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("ForeColor"))
				{
					_foreColor = ColorParser.parse(xpp);
					xpp.next();
				}
				else if (xpp.getName().equals("BackColor"))
				{
					_backColor = ColorParser.parse(xpp);
				}
			}
			eventType = xpp.next();
		}
	}

	@Override
	public void applyToDrawable(ShapeDrawable drawable)
	{
		drawable.getPaint().setColor(_foreColor);
		int colors[] = new int[] { _foreColor, _backColor };
		drawable.getPaint().setShader(new LinearGradient(0, 0, 0, 0, colors, null, Shader.TileMode.REPEAT));
	}

	@Override
	public void fillArea(AreaLayerStyle style)
	{
		// TODO Auto-generated method stub
		
	}
}
