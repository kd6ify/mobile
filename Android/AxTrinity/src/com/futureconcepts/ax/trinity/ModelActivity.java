package com.futureconcepts.ax.trinity;

import com.futureconcepts.ax.model.data.BaseTable;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ModelActivity extends Activity implements IContentObserverRegistrar
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
    
    protected void startManagingModel(BaseTable model)
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

    // IContentObserverRegistrar
    
	@Override
	public void registerContentObserver(BaseAdapter adapter)
	{
		_observerRegistrar.registerContentObserver(_data, _model, adapter);
	}

	@Override
	public void registerContentObserver(Uri uri, boolean descendants, ContentObserver observer)
	{
		_observerRegistrar.registerContentObserver(uri, descendants, observer);
	}

	@Override
	public void unregisterContentObserver(ContentObserver observer)
	{
		_observerRegistrar.unregisterContentObserver(observer);
	}
}
