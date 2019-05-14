package com.futureconcepts.ax.trinity;

import com.futureconcepts.ax.model.data.BaseTable;

import com.google.android.maps.MapActivity;

import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ModelMapActivity extends MapActivity implements IContentObserverRegistrar
{
	protected Uri _data;
	protected BaseTable _model;
	protected DefaultContentObserverRegistrar _observerRegistrar;
	private ConnectivityManager _connectivityManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        _data = getIntent().getData();
        _observerRegistrar = new DefaultContentObserverRegistrar(getContentResolver());
    }
        
    @Override
    public void onDestroy()
    {
		try
		{
			_observerRegistrar.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    	super.onDestroy();
    }
        
    public Uri getData()
    {
    	return _data;
    }
    
    public void setData(Uri value)
    {
    	_data = value;
    }
    
    public void startManagingModel(BaseTable model)
    {
    	if (model != null)
    	{
    		super.startManagingCursor(model);
    	}
    	_model = model;
    }

    protected int getCount()
    {
    	if (_model != null)
    	{
    		return _model.getCount();
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    protected void moveToFirst()
    {
    	if (_model != null)
    	{
    		_model.moveToFirst();
    	}
    }
    
    protected ConnectivityManager getConnectivityManager()
	{
		if (_connectivityManager == null)
		{
			_connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		}
		return _connectivityManager;
	}
    
    protected void setTextView(int resId, String value)
    {
        TextView textView = (TextView)findViewById(resId);
        textView.setText(value);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

    // IContentObserverRegistrar
    
	@Override
	public void registerContentObserver(BaseAdapter adapter)
	{
		if (_observerRegistrar != null)
		{
			try
			{
				_observerRegistrar.registerContentObserver(_data, _model, adapter);
			}
			catch (Exception e) {}
		}
	}

	@Override
	public void registerContentObserver(Uri uri, boolean descendants, ContentObserver observer)
	{
		if (_observerRegistrar != null && observer != null)
		{
			try
			{
				_observerRegistrar.registerContentObserver(uri, descendants, observer);
			}
			catch (Exception e) {}
		}
	}

	@Override
	public void unregisterContentObserver(ContentObserver observer)
	{
		if (_observerRegistrar != null && observer != null)
		{
			try
			{
				_observerRegistrar.unregisterContentObserver(observer);
			}
			catch (Exception e) {}
		}
	}
}
