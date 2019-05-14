package com.futureconcepts.jupiter.compass;

import java.util.Date;

public interface IWaypointView
{
	public boolean getShowWaypoint();
	public void setShowWaypoint(boolean show);
	
	public float getWaypointBearing();
	public void setWaypointBearing(float bearing);
	
	public float getWaypointDistance();
	public void setWaypointDistance(float distance);
	
	public Date getWaypointETA();
	public void setWaypointETA(Date time);
}
