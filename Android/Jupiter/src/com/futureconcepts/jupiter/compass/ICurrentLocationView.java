package com.futureconcepts.jupiter.compass;

import android.location.Location;

public interface ICurrentLocationView
{
	public Location getCurrentLocation();
	public void setCurrentLocation(Location current);
}
