package com.futureconcepts.jupiter.map;

import android.graphics.Color;

public class LineLayerStyle extends LayerStyle
{
	com.osa.android.droyd.map.LineLayerStyle _droydLineLayerStyle;
	
	public LineLayerStyle()
	{
		_droydLineLayerStyle = new com.osa.android.droyd.map.LineLayerStyle();
		_droydLayerStyle = _droydLineLayerStyle;
	}
	
	public void setFillColor(int color)
	{
		_droydLineLayerStyle.fillColor = new com.osa.android.droyd.map.Color((float)Color.red(color)/255.0f, (float)Color.green(color) /255.0f, (float)Color.blue(color)/255.0f, (float)Color.alpha(color)/255.0f);
	}

	public void setWidth(int width)
	{
		_droydLineLayerStyle.width = width;
	}
}
