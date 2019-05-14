package com.futureconcepts.jupiter.compass;

import java.util.List;

public interface IGpsSatelliteView
{
	public List<GpsSatStatsSnapshot> getSatelliteLocations();
	public void setSatelliteLocations(List<GpsSatStatsSnapshot> satellites);
	
	public boolean getShowSatelliteLocations();
	public void setShowSatelliteLocations(boolean show);
}
