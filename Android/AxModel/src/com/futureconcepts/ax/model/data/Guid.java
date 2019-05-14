package com.futureconcepts.ax.model.data;

import java.util.UUID;

public class Guid
{
	private String _value;
	
	public Guid(String value)
	{
		_value = value.toUpperCase();
	}
	
	public static Guid newGuid()
	{
		return new Guid(UUID.randomUUID().toString());
	}
	
	@Override
	public String toString()
	{
		return _value;
	}
}
