package com.futureconcepts.jupiter.layers;

import com.osa.android.droyd.map.PointFeature;

import android.net.Uri;

public class ContentPointFeature extends PointFeature
{
	private Uri _uri;
	
	public ContentPointFeature(Uri uri)
	{
		_uri = uri;
	}
	
	public Uri getUri()
	{
		return _uri;
	}
}
