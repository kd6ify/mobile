package com.futureconcepts.jupiter.map;

import android.graphics.Color;

public class AreaLayerStyle extends LayerStyle
{
	com.osa.android.droyd.map.AreaLayerStyle _droydAreaLayerStyle;
	
	public AreaLayerStyle()
	{
		_droydAreaLayerStyle = new com.osa.android.droyd.map.AreaLayerStyle();
		_droydLayerStyle = _droydAreaLayerStyle;
	}
	
	public void setFillColor(int value)
	{
		_droydAreaLayerStyle.fillColor = new com.osa.android.droyd.map.Color((float)Color.red(value)/255.0f, (float)Color.green(value) /255.0f, (float)Color.blue(value)/255.0f, (float)Color.alpha(value)/255.0f);
	}
	
	public void setBorderColor(int value)
	{
		_droydAreaLayerStyle.borderColor = new com.osa.android.droyd.map.Color((float)Color.red(value)/255.0f, (float)Color.green(value) /255.0f, (float)Color.blue(value)/255.0f, (float)Color.alpha(value)/255.0f);
	}
	
	public void setBorderWidth(float value)
	{
		_droydAreaLayerStyle.borderWidth = value;
	}
}
