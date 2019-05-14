package com.futureconcepts.jupiter.data;

import com.futureconcepts.jupiter.data.BaseTable;

import android.database.Cursor;
import android.provider.MediaStore;

public class MediaVideo extends BaseTable
{
	public MediaVideo(Cursor cursor)
	{
		super(cursor);
	}

	public String getIncidentId()
	{
		return getString(getColumnIndex(MediaStore.Video.VideoColumns.TAGS));
	}
	
	public String getTitle()
	{
		return getString(getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
	}
	
	public String getDescription()
	{
		return getString(getColumnIndex(MediaStore.Video.VideoColumns.DESCRIPTION));
	}
		
	public Double getLatitude()
	{
		return Double.parseDouble(getString(getColumnIndex(MediaStore.Video.VideoColumns.LATITUDE)));
	}

	public Double getLongitude()
	{
		return Double.parseDouble(getString(getColumnIndex(MediaStore.Video.VideoColumns.LONGITUDE)));
	}
}
