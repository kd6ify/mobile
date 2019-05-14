package com.futureconcepts.jupiter.data;

import com.futureconcepts.jupiter.data.BaseTable;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaImages extends BaseTable
{
	public MediaImages(Cursor cursor)
	{
		super(cursor);
	}

	public String getIncidentId()
	{
		return getString(getColumnIndex(MediaStore.Images.ImageColumns.PICASA_ID));
	}

	public Uri getUri()
	{
		return Uri.parse(getString(getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
	}
	public String getTitle()
	{
		return getString(getColumnIndex(MediaStore.Images.ImageColumns.TITLE));
	}
	
	public String getDescription()
	{
		return getString(getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION));
	}
		
	public Double getLatitude()
	{
		return Double.parseDouble(getString(getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE)));
	}

	public Double getLongitude()
	{
		return Double.parseDouble(getString(getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE)));
	}
}
