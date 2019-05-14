package com.futureconcepts.ax.trinity.ar;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.dataset.TriageDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.CheckIncidentNotNull;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class TriageARDemoActivity extends ModelActivity implements Client
{
	private MyObserver _contentObserver;
	private SyncServiceConnection _syncClient;
	private Bundle _savedInstanceState;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        
 //       requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ar_triage);
        if (MercurySettings.getCurrentIncidentId(this) != null)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            
    		_contentObserver = new MyObserver(new Handler());
    		registerContentObserver(Triage.CONTENT_URI, true, _contentObserver);
    		_contentObserver.onChange(true);
            
    		_syncClient = new SyncServiceConnection(this, this);
    		_syncClient.connect();
        }
        else
        {
        	onError("Please select incident");
		}
    }
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		CheckIncidentNotNull.destroyActivityIfIncidentIsNull(this,true);
	}   
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (_syncClient != null)
		{
			_syncClient.disconnect();
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
			OverviewView ov = (OverviewView) findViewById(R.id.OverviewView);
			ov.AddLocations();
		}
	}

	@Override
	public void onSyncServiceConnected()
	{
		if (_savedInstanceState == null)
		{
			_syncClient.syncDataset(TriageDataSet.class.getName());
		}
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		// TODO Auto-generated method stub
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Incident Required");
		ab.setMessage(message);
		ab.setCancelable(false);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}