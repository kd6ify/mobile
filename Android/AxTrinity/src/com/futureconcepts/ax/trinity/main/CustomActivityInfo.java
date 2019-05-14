package com.futureconcepts.ax.trinity.main;

import android.content.pm.ActivityInfo;

public class CustomActivityInfo {
	
	
	private ActivityInfo activityInfo;
	private int disabledIconResource;

	public CustomActivityInfo(ActivityInfo activityInfo,int disabledIconResource)
	{
		this.activityInfo = activityInfo;
		this.disabledIconResource =disabledIconResource;
	}
	
	public ActivityInfo getActivityInfo() {
		return activityInfo;
	}

	public void setActivityInfo(ActivityInfo activityInfo) {
		this.activityInfo = activityInfo;
	}

	public int getDisabledIconResource() {
		return disabledIconResource;
	}

	public void setDisabledIconResource(int disabledIconResource) {
		this.disabledIconResource = disabledIconResource;
	}

}
