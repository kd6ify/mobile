package com.futureconcepts.jupiter.layers;

import com.futureconcepts.jupiter.map.MapComponent;

public class ScaleBarLayer extends MapComponent.Layer
{
	public static final String ID = "104257f6-ae7b-4a14-af8f-3eb25219c58a";
	
	public ScaleBarLayer(MapComponent component)
	{
		component.super();
		_id = ID;
		setEnabled(false);
	}
	
	@Override
	public String getName()
	{
		return "ScaleBar";
	}
	
	@Override
	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		getMapComponent().enableLayer("ScaleBar", value);
	}
}
