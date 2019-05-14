package com.futureconcepts.ax.rex.activities;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;

public class SubmitRequestActivity extends Activity implements Client
{
	private ProgressDialog _progressDialog;
	private SyncServiceConnection _syncServiceConnection;
	private Handler _handler;
	private Uri _data;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_request_activity);
        _data = getIntent().getData();
        _handler = new Handler();
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setTitle("Submit Incident Request");
        _progressDialog.setMessage("Submitting Incident Request...");
        _progressDialog.show();
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_progressDialog != null)
    	{
    		if (_progressDialog.isShowing())
    		{
    			_progressDialog.dismiss();
    			_progressDialog = null;
    		}
    	}
    	_syncServiceConnection.disconnect();
    }
    
    private void showAlert()
    {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Incident Request Status");
		ab.setMessage("The incident request has been submitted.  ");
		ab.setNeutralButton("OK", new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
    }

	@Override
	public void onSyncServiceConnected()
	{
		IncidentRequest incidentRequest = new IncidentRequest(this, getContentResolver().query(_data, null, null, null, null));
		if (incidentRequest.getCount() == 1)
		{
			incidentRequest.moveToFirst();
			String addressID = incidentRequest.getAddressID();
			if (addressID != null)
			{
				Uri addressUri = Uri.withAppendedPath(Address.CONTENT_URI, addressID);
				_syncServiceConnection.uploadInsert(addressUri);
			}
			incidentRequest.close();
		}
		_syncServiceConnection.uploadInsert(_data);
		_handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				showAlert();
			}
		});
	}

	@Override
	public void onSyncServiceDisconnected()
	{
	}
}
