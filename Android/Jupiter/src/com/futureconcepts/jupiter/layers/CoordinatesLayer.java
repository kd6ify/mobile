package com.futureconcepts.jupiter.layers;

import com.futureconcepts.jupiter.map.MapComponent;

public class CoordinatesLayer extends MapComponent.Layer
{
	public static final String ID = "ddde2627-94fe-445a-9d59-a6460f6316c7";
	
	public CoordinatesLayer(MapComponent component)
	{
		component.super();
		_id = ID;
	}
	
	@Override
	public String getName()
	{
		return "Coordinates";
	}
}
