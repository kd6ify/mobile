package com.futureconcepts.jupiter.layers;

import com.futureconcepts.jupiter.map.MapComponent;

public class TopographyLayer extends MapComponent.Layer
{
	public static final String ID = "20df11f3-e22d-4814-9b8d-bad82761a83a";
	
	public TopographyLayer(MapComponent component)
	{
		component.super();
		_id = ID;
		setEnabled(false);
	}
	
	@Override
	public String getName()
	{
		return "Topography";
	}
			
	@Override
	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		getMapComponent().enableLayers("Contour*", value);
	}
}
