package com.futureconcepts.jupiter.map;

import android.graphics.drawable.Drawable;

public class PointLayerStyle extends LayerStyle
{
	com.osa.android.droyd.map.PointLayerStyle _droydPointLayerStyle;
	
	public PointLayerStyle()
	{
		_droydPointLayerStyle = new com.osa.android.droyd.map.PointLayerStyle();
		_droydLayerStyle = _droydPointLayerStyle;
	}
	
	public void setIconDrawable(Drawable drawable)
	{
		_droydPointLayerStyle.iconDrawable = drawable;
	}
	
	public Drawable getIconDrawable()
	{
		return _droydPointLayerStyle.iconDrawable;
	}
	
	public void setAnchorX(float x)
	{
		_droydPointLayerStyle.anchorX = x;
	}

	public void setAnchorY(float y)
	{
		_droydPointLayerStyle.anchorY = y;
	}
}
