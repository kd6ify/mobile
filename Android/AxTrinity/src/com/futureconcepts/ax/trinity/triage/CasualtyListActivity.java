package com.futureconcepts.ax.trinity.triage;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.model.dataset.TriageDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.CheckIncidentNotNull;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CasualtyListActivity extends ListActivity implements Client,EditTextWithSearchInterface
{
	private static final String TAG = CasualtyListActivity.class.getSimpleName();
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
		
	private static final String TAG_SELECTED_COLOR = "SelectedColor";

	private String _incidentID;
	private Location _lastKnownLocation;
	
	private Triage _triage;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private String _selectedColor;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private Timer _resyncIntervalTimer;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.view_casualties);
        _incidentID = MercurySettings.getCurrentIncidentId(this);
        if (_incidentID != null)
        {
        	setTitle("Triage Casualties for " + Config.getCurrentIncidentName(this));
        }
        else
        {
        	onError("Please select an incident");
		}
        if (savedInstanceState != null)
        {
        	_selectedColor = savedInstanceState.getString(TAG_SELECTED_COLOR);
        }
        queryCasualties(null);
        _adapter = new MyAdapter();
        _observer = new MyObserver(new Handler());
        setListAdapter(_adapter);
        addSearchListener();
        getContentResolver().registerContentObserver(Triage.CONTENT_URI, true, _observer);
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	CheckIncidentNotNull.destroyActivityIfIncidentIsNull(this,true);
        _resyncIntervalTimer = new Timer("ResyncIntervalTimer");
        _resyncIntervalTimer.schedule(new TimerTask()
        {
			@Override
			public void run()
			{
				sync();
			}
        }, 0, RESYNC_INTERVAL);
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	_resyncIntervalTimer.cancel();
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	_syncServiceConnection.disconnect();
    	removeSearchListener();
    	if(_triage!=null && !_triage.isClosed())
    		_triage.close();
    }

    @Override
    public void onSaveInstanceState(Bundle icicle)
    {
    	if (_selectedColor != null)
    	{
    		icicle.putString(TAG_SELECTED_COLOR, _selectedColor);
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.casualty_list_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_scan_tag:
			onMenuScanTag();
			break;
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
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Scan Tag"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Scan Tag".equals(options[which]))
						{
							onMenuScanTag();
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(TriageDataSet.class.getName());
		}
	}
		
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		try
		{
			_triage.moveToPosition(position);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.withAppendedPath(Triage.CONTENT_URI, _triage.getID()));
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    public void onImmediateClicked(View view)
    {
    	_selectedColor = TriageColor.RED;
    	updateSearchFieldProperties("Immediate");
    	queryCasualties(null);
    }

    public void onDelayedClicked(View view)
    {
    	_selectedColor = TriageColor.YELLOW;
    	updateSearchFieldProperties("Delayed");
    	queryCasualties(null);
    }
    
    public void onMinorClicked(View view)
    {
    	_selectedColor = TriageColor.GREEN;
    	updateSearchFieldProperties("Minor");
    	queryCasualties(null);
    }

    public void onDeceasedClicked(View view)
    {
    	_selectedColor = TriageColor.BLACK;
    	updateSearchFieldProperties("Deceased");
    	queryCasualties(null);
    }

    public void onMarkedClicked(View view)
    {
    }

    private void onMenuScanTag()
	{
		LocationManager locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
		_lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		IntentIntegrator intentIntegrator = new IntentIntegrator(this);
		intentIntegrator.initiateScan();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null)
		{
			try
			{
				Uri existingUri = Triage.findUri(this, Triage.TRACKING_ID+"='"+scanResult.getContents()+"'");
				if (existingUri == null)
				{
					onAddNewCasualty(scanResult);
				}
				else
				{
					onEditExistingCasualty(existingUri);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void onAddNewCasualty(IntentResult scanResult)
	{
		Uri addressUri = null;
		Uri personUri = null;
		Person.Content personContent = new Person.Content(this);
		personContent.setLast("Doe");
		personUri = personContent.insert(Person.CONTENT_URI);
		if (_lastKnownLocation != null)
		{
			Address.Content addressContent = new Address.Content(this, Config.getDeviceId(this));
			addressContent.setWKT(_lastKnownLocation);
			addressUri = addressContent.insert(this);
		}
		Triage.Content triageContent = new Triage.Content(this, Config.getDeviceId(this), MercurySettings.getCurrentIncidentId(this));
		triageContent.setTrackingID(scanResult.getContents());
		triageContent.setAddressID(addressUri);
		triageContent.setPersonID(personUri);
		Uri uri = triageContent.insert(Triage.CONTENT_URI);
		if (uri != null)
		{
			Intent editIntent = new Intent(Intent.ACTION_INSERT, uri);
			startActivity(editIntent);
		}
	}

	private void onEditExistingCasualty(Uri uri)
	{
		Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
		startActivity(editIntent);
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

	protected void queryCasualties(String text)
	{
		boolean notify = false;
		if (_triage != null)
		{
			_triage.close();
			notify = true;
		}
		String currentIncidentId = MercurySettings.getCurrentIncidentId(this);
		String whereClause = null;
		if(text!= null && text.length()>0)
		{
			if(_selectedColor != null){
				whereClause = Triage.INCIDENT+"='"+currentIncidentId+"' AND "+Triage.COLOR+"='"+_selectedColor+"' AND "+
					Triage.TRACKING_ID+" Like '%"+text+"%'";
			}else
			{
				whereClause = Triage.INCIDENT+"='"+currentIncidentId+"' AND "+
					Triage.TRACKING_ID+" Like '%"+text+"%'";
			}
		}
		if (_selectedColor == null)
		{
			if(whereClause== null){
				_triage = Triage.queryIncident(this, currentIncidentId);
			}else{
				_triage = new Triage(this, getContentResolver().query(Triage.CONTENT_URI, null, whereClause, null, null));
			}			
		}
		else if (_selectedColor.equals(TriageColor.GREEN))
		{
			if(whereClause== null){
				_triage = Triage.queryMinor(this, currentIncidentId);
			}else{
				_triage = new Triage(this, getContentResolver().query(Triage.CONTENT_URI, null, whereClause, null, null));
			}
		}
		else if (_selectedColor.equals(TriageColor.YELLOW))
		{
			if(whereClause== null){
				_triage = Triage.queryDelayed(this, currentIncidentId);
			}else{
				_triage = new Triage(this, getContentResolver().query(Triage.CONTENT_URI, null, whereClause, null, null));
			}			
		}
		else if (_selectedColor.equals(TriageColor.RED))
		{
			if(whereClause== null){
				_triage = Triage.queryImmediate(this, currentIncidentId);
			}else{
				_triage = new Triage(this, getContentResolver().query(Triage.CONTENT_URI, null, whereClause, null, null));
			}			
		}
		else if (_selectedColor.equals(TriageColor.BLACK))
		{
			if(whereClause== null){
				_triage = Triage.queryDeceased(this, currentIncidentId);
			}else{
				_triage = new Triage(this, getContentResolver().query(Triage.CONTENT_URI, null, whereClause, null, null));
			}			
		}
		if (notify && _adapter != null)
		{
			_adapter.changeCursor(_triage);
			_adapter.notifyDataSetChanged();
		}
	}
	
	public class MyAdapter extends AlternatingColorCursorAdapter
	{
		public MyAdapter()
		{
			super(CasualtyListActivity.this, R.layout.casualty_list_item, _triage);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			Button button = (Button)view.findViewById(R.id.tracking_id);
			try
			{
				final Triage triage = (Triage)c;
				final int position = triage.getPosition();
				String colorID = triage.getColorID();
				if (colorID != null)
				{
					if (colorID.equals(TriageColor.GREEN))
					{
						button.setTextColor(CasualtyListActivity.this.getResources().getColor(R.color.green_button_gradient_end));
					}
					else if (colorID.equals(TriageColor.YELLOW))
					{
						button.setTextColor(CasualtyListActivity.this.getResources().getColor(R.color.yellow_button_gradient_start));
					}
					else if (colorID.equals(TriageColor.RED))
					{
						button.setTextColor(CasualtyListActivity.this.getResources().getColor(R.color.red_button_gradient_end));
					}
					else if (colorID.equals(TriageColor.BLACK))
					{
						button.setTextColor(CasualtyListActivity.this.getResources().getColor(R.color.widget_background));
					}
				}
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onListItemClick(null, v, position, 0);
					}
				});
				button.setText(triage.getTrackingID());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			try
			{
				if (_triage != null)
				{
					_triage.requery();
					if (_adapter != null)
					{
						_adapter.notifyDataSetChanged();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
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
	
	private void updateSearchFieldProperties(String triageType)
	{
		((EditTextWithSearch)findViewById(R.id.triage_search)).setHint("Search "+triageType+" Triage...");
		((EditTextWithSearch)findViewById(R.id.triage_search)).setText(null);
		((TextView)findViewById(R.id.empty_triage)).setText("No "+triageType+" Triage Found" );
	}
	
	private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.triage_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.triage_search)).removeSearchListener(this);
    }
	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method stub
		queryCasualties(text);
		
	}	
}