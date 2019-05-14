package com.futureconcepts.jupiter.layers;

import com.osa.android.droyd.map.PointLayerStyle;

public class PointLayerStyleExtended extends PointLayerStyle
{
	private String _label;
	
	public String getLabel()
	{
		return _label;
	}
	
	public void setLabel(String value)
	{
		_label = value;
	}
}
