package com.futureconcepts.ax.trinity.geo;

public class LayerInfo
{
	private String _name;
	private Class<?> _layerClass;
	private int _iconId;
	private boolean _isSelected;

	public String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	
	public Class<?> getLayerClass()
	{
		return _layerClass;
	}
	
	public void setClass(Class<?> value)
	{
		_layerClass = value;
	}
	
	public int getIconId()
	{
		return _iconId;
	}
	
	public void setIconId(int value)
	{
		_iconId = value;
	}
	
	public boolean isSelected()
	{
		return _isSelected;
	}
	
	public void setSelected(boolean value)
	{
		_isSelected = value;
	}
}
