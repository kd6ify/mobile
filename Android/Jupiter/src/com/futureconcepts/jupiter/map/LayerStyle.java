package com.futureconcepts.jupiter.map;

import android.graphics.Color;

public class LayerStyle
{
	com.osa.android.droyd.map.LayerStyle _droydLayerStyle;
	
	public void setLabelEnabled(boolean value)
	{
		_droydLayerStyle.labelEnabled = value;
	}
	
	public void setLabelTextColor(int color)
	{
		_droydLayerStyle.labelTextColor = new com.osa.android.droyd.map.Color((float)Color.red(color)/255.0f, (float)Color.green(color) /255.0f, (float)Color.blue(color)/255.0f, (float)Color.alpha(color)/255.0f);
	}

	public void setLabelHaloColor(int color)
	{
		_droydLayerStyle.labelHaloColor = new com.osa.android.droyd.map.Color((float)Color.red(color)/255.0f, (float)Color.green(color) /255.0f, (float)Color.blue(color)/255.0f, (float)Color.alpha(color)/255.0f);
	}
	
	public void setLabelFontSize(float value)
	{
		_droydLayerStyle.labelFontSize = value;
	}
}
