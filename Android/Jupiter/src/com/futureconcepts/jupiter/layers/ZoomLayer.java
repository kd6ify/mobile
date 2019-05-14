package com.futureconcepts.jupiter.layers;

import com.futureconcepts.jupiter.map.MapComponent;

public class ZoomLayer extends MapComponent.Layer
{
	public static final String ID = "f72e439f-2e1f-4e6a-8c7e-2708606e6670";
	
	public ZoomLayer(MapComponent component)
	{
		component.super();
		_id = ID;
	}
	
	@Override
	public String getName()
	{
		return "Zoom";
	}
}
