package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ContentBase
{
	protected Context _context;
	protected ContentValues _values;
    private PropertyChangedListener _propertyChangedListener;
		
	protected ContentBase(Context context)
	{
		_context = context;
		_values = new ContentValues();
	}

	protected void initialize(String deviceId)
	{
		_values.put("ID", Guid.newGuid().toString());
		_values.put("Owner", deviceId);
	}

	public void prepareForUpload(String action)
	{
		_values.put(BaseTable.UPLOAD, action);
	}
	
	public void prepareForCommit()
	{
		DateTime now = DateTime.now();
		String nowString = now.toString();
		_values.put("LastModified", nowString);
	}

	public Uri insert(Uri uri)
	{
		return _context.getContentResolver().insert(uri, _values);
	}

	public int update(Uri uri)
	{
		return _context.getContentResolver().update(uri, _values, null, null);
	}
	
	public void setPropertyChangedListener(PropertyChangedListener listener)
	{
		_propertyChangedListener = listener;
	}

	public void notifyPropertyChanged(String propertyName, Object value)
	{
		if (_propertyChangedListener != null)
		{
			_propertyChangedListener.propertyChanged(propertyName, value);
		}
	}
}	
