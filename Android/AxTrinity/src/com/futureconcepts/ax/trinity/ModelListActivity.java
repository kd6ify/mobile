package com.futureconcepts.ax.trinity;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.ListActivity;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.BaseAdapter;

public class ModelListActivity extends ListActivity implements IContentObserverRegistrar
{
	private Uri _data;
	private BaseTable _model;
	private DefaultContentObserverRegistrar _observerRegistrar;
	private ConnectivityManager _connectivityManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        _data = getIntent().getData();
        _observerRegistrar = new DefaultContentObserverRegistrar(getContentResolver());
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(isIncidentNull())
		{
			finish();
		}
	}
    
    protected boolean isIncidentNull()
	{
		if(CheckIncidentNotNull.isIncidentNull(this))
			return true;
		else
			return false;
	}
    
    @Override
    public void onDestroy()
    {
    	try
    	{
    		if (_observerRegistrar != null)
    		{
    			_observerRegistrar.close();
    			_observerRegistrar = null;
    		}
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
    
    protected boolean moveToFirstIfOneRow()
    {
    	boolean result = false;
    	if (_model != null)
    	{
    		if (_model.getCount() == 1)
    		{
    			_model.moveToFirst();
    			result = true;
    		}
    	}
    	return result;
    }

    protected ConnectivityManager getConnectivityManager()
	{
		if (_connectivityManager == null)
		{
			_connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		}
		return _connectivityManager;
	}
    
    protected String getFormattedLocalTime(DateTime datetime, String nullText)
    {
    	String result = nullText;
    	if (datetime != null)
    	{
    		result = datetime.toLocalDateTime().toString("MM/dd/yy HH:mm:ss");
    	}
    	return result;
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
