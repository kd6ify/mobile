package com.futureconcepts.ax.trinity.main;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.ax.model.dataset.OperationalPeriodViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.ModelListActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.main.IncidentSelectorActivity.MyCursor;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class OperationalPeriodSelectorActivity extends ModelListActivity implements Client,EditTextWithSearchInterface
{
	private static final String TAG = OperationalPeriodSelectorActivity.class.getSimpleName();

	private OperationalPeriod _period;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.operational_period_list);
		final String incidentId = MercurySettings.getCurrentIncidentId(this);
        if (incidentId != null)
        {
            DateTime.setContext(this);
    		_period = OperationalPeriod.queryIncident(this, incidentId);
            _adapter = new MyAdapter();
            setListAdapter(_adapter);
            _adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        	public Cursor runQuery(CharSequence constraint) {	
	        		String text = constraint.toString();
	        		if( text == null  || text.length () == 0 )
	        		{
	        			_period = OperationalPeriod.queryIncident(getContext(), incidentId);
	        		}else{
	        			_period = OperationalPeriod.queryWhere(getContext(),
	        				OperationalPeriod.INCIDENT+"='"+incidentId+"' AND "+
	        				OperationalPeriod.NAME+" Like '%"+constraint.toString()+"%'");
	        		}
	        		return _period;//_myCursor;
	        	}
	        });
            _observer = new MyObserver(new Handler());
    		registerContentObserver(OperationalPeriod.CONTENT_URI, false, _observer);
    		_syncServiceConnection = new SyncServiceConnection(this, this);
    		_syncServiceConnection.connect();
    		addSearchListener();
        }
        else
        {
        	onError("Please select an incident");
		}
    }
    
    private Context getContext()
    {
    	return this;
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	if (_period != null && isFinishing())
    	{
	    	if (MercurySettings.getCurrentOperationalPeriodId(this) == null)
	    	{
	    		if (_period.getCount() > 0)
	    		{
	    			_period.moveToFirst();
	    			MercurySettings.setCurrentOperationalPeriodId(this, _period.getID());
	    			Toast.makeText(this, "WARNING: " + _period.getName() + " has been selected", Toast.LENGTH_LONG).show();
	    		}
	    	}
    	}
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_syncServiceConnection != null)
    	{
    		_syncServiceConnection.disconnect();
    	}
    	try
    	{
	    	if (_period != null)
	    	{
	    		_period.close();
	    		_period = null;
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	removeSearchListener();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.operational_period_selector_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_refresh:
			sync();
			break;
		}
		return false;
	}
	
	public void goBack(View view)
	{
		finish();
	}
	public void refresh(View view)
	{
		sync();
	}
	
	
           
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Log.i(TAG, "onListItemClick");
		try
		{
			_period  = (OperationalPeriod)getListView().getItemAtPosition(position);
			MercurySettings.setCurrentOperationalPeriodId(this, _period.getID());			   
	        
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finish();
	}

	public void sync()
	{
		if (_syncServiceConnection != null && _syncServiceConnection.isConnected())
		{
			_syncServiceConnection.syncDataset(OperationalPeriodViewDataSet.class.getName());
		}
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Incident");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}
			
	private class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(OperationalPeriodSelectorActivity.this, R.layout.operational_period_list_item, _period);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			OperationalPeriod period = (OperationalPeriod)cursor;
			try
			{
				((TextView)view.findViewById(R.id.text1)).setText(period.getName());
				DateTime startTime = period.getActualStart();
				if (startTime == null)
				{
					startTime = period.getProjectedStart();
				}
				((TextView)view.findViewById(R.id.date)).setText(getFormattedLocalTime(startTime, "no start time"));
				DateTime endTime = period.getActualEnd();
				if (endTime == null)
				{
					endTime = period.getProjectedEnd();
				}
				((TextView)view.findViewById(R.id.date_end_time)).setText(getFormattedLocalTime(endTime, "no end time"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class MyObserver extends ContentObserver
	{

		public MyObserver(Handler handler)
		{
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_period != null)
			{
				_period.requery();
				if (_adapter != null)
				{
					_adapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onSyncServiceConnected()
	{
		if (_savedInstanceState == null)
		{
			sync();
		}
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
	}
	
	private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.period_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.period_search)).removeSearchListener(this);
    }

	@Override
	public void handleSearch(final String text) {
		// TODO Auto-generated method stub
		Handler mHandler = new Handler(getMainLooper());
	    mHandler.post(new Runnable() {
	        @Override
	        public void run() {	      
	        	if(_adapter!=null)
	        		_adapter.getFilter().filter(text);
	        		//_adapter.notifyDataSetChanged();
	        }
	    });	   		
		
	}
}
