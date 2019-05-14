package com.futureconcepts.ax.model.dataset;

import android.net.Uri;

public class DataSetTable
{
	private String _query;
	private Uri _uri;
	
	public DataSetTable(String query, Uri uri)
	{
		_query = query;
		_uri = uri;
	}
	public DataSetTable(Uri uri)
	{
		_query = uri.getPathSegments().get(0);
		_uri = uri;
	}
	
	public String getQuery()
	{
		return _query;
	}
	
	public Uri getUri()
	{
		return _uri;
	}
}
