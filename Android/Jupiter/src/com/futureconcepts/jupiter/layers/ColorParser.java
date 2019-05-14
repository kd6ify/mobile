package com.futureconcepts.jupiter.layers;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Color;

public class ColorParser
{
	public static int parse(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int a = Integer.parseInt(xpp.getAttributeValue(null, "a"));
		int r = Integer.parseInt(xpp.getAttributeValue(null, "r"));
		int g = Integer.parseInt(xpp.getAttributeValue(null, "g"));
		int b = Integer.parseInt(xpp.getAttributeValue(null, "b"));
		return Color.argb(a, r, g, b);
	}
}
