package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;

public class IncidentStatus extends IndexedType
{
	public static final String ARCHIVED = "99C16C1E-47FD-0252-3E1A-F5F4F58B7BAE";
	
	public IncidentStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
}
