package com.futureconcepts.ax.trinity;

import java.io.Closeable;
import java.io.IOException;

import com.futureconcepts.ax.model.data.BaseTable;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.widget.BaseAdapter;

public class DefaultContentObserverRegistrar implements Closeable
{
	private ContentResolver _contentResolver;
	private BaseAdapter _adapter;
	private ContentObserver _observer;
	private Uri _data;
	private BaseTable _model;
	
	public DefaultContentObserverRegistrar(ContentResolver contentResolver)
	{
		_contentResolver = contentResolver;
	}

	@Override
	public void close() throws IOException
	{
		if (_observer != null)
		{
			_contentResolver.unregisterContentObserver(_observer);
			_observer = null;
		}
	}
	
    protected void registerContentObserver(Uri uri, BaseTable model, BaseAdapter adapter)
	{
    	_data = uri;
    	_model = model;
		_adapter = adapter;
		_observer = new MyObserver(new Handler());
		_contentResolver.registerContentObserver(_data, false, _observer);
	}
	
	protected void registerContentObserver(Uri uri, boolean descendants, ContentObserver observer)
	{
		_data = uri;
		_contentResolver.registerContentObserver(_data, descendants, observer);
		_observer = observer;
	}
	
	protected void unregisterContentObserver(ContentObserver observer)
	{
		_contentResolver.unregisterContentObserver(observer);
		_observer = null;
	}
	
	private final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		@Override
		public void onChange(boolean selfChange)
		{
			if (_model != null)
			{
				_model.requery();
				if (_model.getCount() == 0)
				{
					
				}
				if (_model.getCount() == 1)
				{
					_model.moveToFirst();
				}
				if (_adapter != null)
				{
					_adapter.notifyDataSetChanged();
				}
			}
		}
	}
}
