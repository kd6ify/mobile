package com.futureconcepts.ax.model.dataset;

import android.net.Uri;

public class DataSetItem
{
	private String _query;
	private Uri _uri;
	private boolean _isIncidentTable;
	
	public DataSetItem(String query, Uri uri, boolean isIncidentTable)
	{
		_query = query;
		_uri = uri;
		_isIncidentTable = isIncidentTable;
	}
	public DataSetItem(Uri uri)
	{
		_query = uri.getPathSegments().get(0);
		_uri = uri;
		_isIncidentTable = false;
	}
	
	public String getQuery()
	{
		return _query;
	}
	
	public Uri getUri()
	{
		return _uri;
	}
	
	public boolean isIncidentTable()
	{
		return _isIncidentTable;
	}
}
