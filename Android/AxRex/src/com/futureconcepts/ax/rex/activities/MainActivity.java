package com.futureconcepts.ax.rex.activities;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.model.data.IncidentRequestStatus;
import com.futureconcepts.ax.model.dataset.IncidentRequestDataSet;
import com.futureconcepts.ax.model.dataset.IncidentRequestStaticDataSet;
import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.rex.config.Config;
import com.futureconcepts.ax.sync.client.ISyncListener;
import com.futureconcepts.ax.sync.client.ISyncTransaction;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
//import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements Client
{
	private static final String TAG = MainActivity.class.getSimpleName();
	private IncidentRequest _incidentRequest;
	private MyAdapter _adapter;
	private Handler _handler;
	private ProgressDialog _syncDialog;
	private MySyncListener _syncListener;
	
	private MyObserver _observer;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.main_activity);
        findViewById(R.id.btn_create).setVisibility(View.INVISIBLE);
        _handler = new Handler();
        _observer = new MyObserver();
        getContentResolver().registerContentObserver(IncidentRequest.CONTENT_URI, true, _observer);
        _incidentRequest = IncidentRequest.query(this);
        if (_incidentRequest != null)
        {
            findViewById(R.id.btn_create).setVisibility(View.VISIBLE);
        }
		_adapter = new MyAdapter();
        setListAdapter(_adapter);
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_syncServiceConnection != null)
    	{
    		if (_syncListener != null)
    		{
    			_syncServiceConnection.unregisterSyncListener(_syncListener);
    			_syncListener = null;
    		}
    		_syncServiceConnection.disconnect();
    		_syncServiceConnection = null;
    	}
    	if (_observer != null)
    	{
    		getContentResolver().unregisterContentObserver(_observer);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_incidentRequest.moveToPosition(position);
		String rid = _incidentRequest.getID();
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(IncidentRequest.CONTENT_URI, rid)));
	}
	
    private void sync()
    {
    	_syncServiceConnection.syncDataset(IncidentRequestDataSet.class.getName());
    }
    
    public void onCreateIncidentClick(View view)
    {
    	if (_incidentRequest != null)
    	{
	    	Intent intent = new Intent(this, SelectTypeActivity.class);
	    	startActivity(intent);
    	}
    	else
    	{
	    	AlertDialog.Builder b = new AlertDialog.Builder(this);
	    	b.setTitle("Synchronizer working");
	    	b.setMessage("Please wait for synchronizer to complete.");
	    	b.setCancelable(true);
	    	b.show();
    	}
    }
    
	private class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(MainActivity.this, R.layout.request_list_item, _incidentRequest);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			try
			{
				IncidentRequest incidentRequest = (IncidentRequest)c;
				((TextView)view.findViewById(R.id.incident_name)).setText(incidentRequest.getIncidentName());
				((TextView)view.findViewById(R.id.status)).setText(getStatusText(incidentRequest.getStatusID()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private String getStatusText(String statusID)
	{
		String result = "unknown status";
		if (statusID != null)
		{
			if (statusID.equals(IncidentRequestStatus.NEW))
			{
				result = "New";
			}
			else if (statusID.equals(IncidentRequestStatus.APPROVED))
			{
				result = "Approved";
			}
			else if (statusID.equals(IncidentRequestStatus.DISMISSED))
			{
				result = "Dismissed";
			}
			else if (statusID.equals(IncidentRequestStatus.IN_REVIEW))
			{
				result = "In Review";
			}
			else if (statusID.equals(IncidentRequestStatus.SHARED))
			{
				result = "Shared";
			}
		}
		return result;
	}
	
	private void setImageViewIcon(View view, int resid, Icon icon)
	{
		ImageView imageView = (ImageView)view.findViewById(resid);
		if (icon != null)
		{
			byte[] bytes = icon.getImage();
			if (bytes != null)
			{
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			}
		}
	}

	@Override
	public void onSyncServiceConnected()
	{
		Log.d(TAG, "onSyncServiceConnected");
		_syncListener = new MySyncListener();
		_syncServiceConnection.registerSyncListener(_syncListener);
        if (Config.isFirstApplicationLaunch(this))
        {
        	performInitialConfiguration();
        }
        sync();
	}

	private void performInitialConfiguration()
	{
        Toast.makeText(this, "performing initial configuration--please relaunch later", Toast.LENGTH_LONG).show();
		_syncServiceConnection.setCurrentIncidentID("BBF7747F-A4AE-4CF1-8ECB-50709D84A080"); // fake ID to get schemas
		_syncServiceConnection.syncDataset(IncidentRequestStaticDataSet.class.getName());
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
	}
	
	private class MySyncListener extends ISyncListener.Stub
	{
		@Override
		public IBinder asBinder()
		{
			return this;
		}

		@Override
		public void onTransaction(ISyncTransaction transaction) throws RemoteException
		{
		}

		@Override
		public void onTransactionComplete(ISyncTransaction arg0) throws RemoteException
		{
		}

		@Override
		public void onStart() throws RemoteException
		{
			_handler.post(new Runnable() {
				@Override
				public void run()
				{
		        	if (_syncDialog == null)
		        	{
		        		_syncDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
		        	}
	        		_syncDialog.setTitle("Syncing");
	        		_syncDialog.setMessage("Please wait...");
	        		_syncDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog)
						{
							finish();
						}
	        		});
        			_syncDialog.show();
				}
			});
		}

		@Override
		public void onStop() throws RemoteException
		{
			_handler.post(new Runnable() {
				@Override
				public void run()
				{
		        	if (_syncDialog != null)
		        	{
		        		_syncDialog.dismiss();
		        		_syncDialog = null;
		        	}
				}
			});
		}

		@Override
		public void onRescheduled() throws RemoteException
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	private final class MyObserver extends ContentObserver
	{
		public MyObserver()
        {
	        super(_handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_incidentRequest != null)
			{
				try
				{
					_incidentRequest.requery();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				_incidentRequest = IncidentRequest.query(MainActivity.this);
		        if (_incidentRequest != null)
		        {
		        	_adapter.changeCursor(_incidentRequest);
		        	_handler.post(new Runnable() {
						@Override
						public void run()
						{
				            findViewById(R.id.btn_create).setVisibility(View.VISIBLE);
						}
		        	});
		        }
			}
			if (_adapter != null)
			{
				_adapter.notifyDataSetChanged();
			}
		}
	}
}