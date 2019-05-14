package com.futureconcepts.ax.trinity;

import android.app.Activity;
import android.content.Context;

import com.futureconcepts.gqueue.MercurySettings;

public class CheckIncidentNotNull {
	
	
	public static void destroyActivityIfIncidentIsNull(Context context, boolean destroy)
	{
		if(isIncidentNull(context) && destroy)
		{
			((Activity)context).finish();
		}
	}
	
	public static boolean isIncidentNull(Context context)
	{
		if (MercurySettings.getCurrentIncidentId(context) != null)
		{
			String incidentName = Config.getCurrentIncidentName(context);
			if (incidentName != null && !"PLEASE RESET DATA".equals(incidentName))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return true;
		}
	}

}
