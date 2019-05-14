package com.futureconcepts.ax.trinity.triage;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.CheckIncidentNotNull;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObject;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class TriageMainActivity extends FragmentActivity implements OnItemClickListener
{
	private static final String TAG = TriageMainActivity.class.getSimpleName();

	private String _incidentID;
	private MyAdapter _adapter;
	private Location _lastKnownLocation;
	private ArrayList<Integer> _resources;
	private MyLocationListener _locationListener;
	
	private static final int ACTIVITY_SCAN = 1;
	private static final int ACTIVITY_MANUAL = 2;
	
	private BroadcastReceiver _messageReceiver;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_main);
        _incidentID = MercurySettings.getCurrentIncidentId(this);
        if (_incidentID != null)
        {
        	setTitle("Triage: " + Config.getCurrentIncidentName(this));
            _resources = new ArrayList<Integer>();
            addResource(R.drawable.triage_scan_button);
            addResource(R.drawable.triage_manual_button);
            addResource(R.drawable.triage_status_button);
            _adapter = new MyAdapter();
    		GridView grid = (GridView)findViewById(R.id.grid);
            grid.setAdapter(_adapter);
            grid.setOnItemClickListener(this);
        }
        else
        {
        	onError("Please select an incident");
		}
        _locationListener = new MyLocationListener();
		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, _locationListener);
//		_messageReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				String title = intent.getStringExtra("Title");
//				String message = intent.getStringExtra("Message");
//				DialogFragment dialogFragment = TriageInsertedDialogFragment.newInstance();
//				dialogFragment.show(getSupportFragmentManager(), "dialog");
//			}
//		};
//		registerReceiver(_messageReceiver, new IntentFilter("com.futureconcepts.trinity.user_message"));
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
//    	unregisterReceiver(_messageReceiver);
    	getLocationManager().removeUpdates(_locationListener);
    }
    
    @Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		int resId = _resources.get(position);
		if (resId == R.drawable.triage_scan_button)
		{
			onScanClicked(view);
		}
		else if (resId == R.drawable.triage_manual_button)
		{
			onManualClicked(view);
		}
		else if (resId == R.drawable.triage_status_button)
		{
			onStatusClicked(view);
		}
	}
	
    public void goBack(View view)
	{
		finish();
	}
//    @Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.triage_main_options_menu, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//	
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu)
//    {
//    	return true;
//    }
    
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		switch (item.getItemId())
//		{
//		}
//		return false;
//	}
		
	private LocationManager getLocationManager()
	{
		return (LocationManager)getSystemService(LOCATION_SERVICE);
	}
	//com.google.zxing.client.android.SCAN
	public void onScanClicked(View view)
	{
		 if(appInstalledOrNot("com.google.zxing.client.android")) {
			 LocationManager locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
				_lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				IntentIntegrator intentIntegrator = new IntentIntegrator(this);
				ArrayList<String> formats = new ArrayList<String>();
				formats.add("CODE_128");
				intentIntegrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);       
	        }
	        else {
	       		 String [] buttons = new String[] {"Yes","No"};	       	 
	   		 // Create an instance of the dialog fragment and show it
	   		 final CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,"Information","Barcode Scanner is not installed. Do you want to install it?",android.R.drawable.ic_menu_info_details,
	   				 new CustomAlertDialog.DialogButtonClickListener() {
	   					@Override
	   					public void onDialogButtonClick(View v) {
	   						// TODO Auto-generated method stub
	   						if("Yes".equals(v.getTag()))
	   						{
	   							Intent installScanner = new Intent(Intent.ACTION_VIEW);
	   							installScanner.setData(Uri.parse("market://details?id=com.google.zxing.client.android"));//"http://zxing.appspot.com/scan"));
	   							startActivity(installScanner);
	   						}						
	   					}
	   				});
	   		 customDialog.show();
	            System.out.println("App is not installed on your phone");
	        }
	}
	
	private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
    }
	public void onManualClicked(View view)
	{
		LocationManager locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
		_lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Intent intent = new Intent(this, EnterTrackingIdActivity.class);
		startActivityForResult(intent, ACTIVITY_MANUAL);
	}

	public void onStatusClicked(View view)
	{
		Intent intent = new Intent(this, CasualtyListActivity.class);
		startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == IntentIntegrator.REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Toast.makeText(this, "SCAN SUCCESSFUL", Toast.LENGTH_LONG).show();
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
				Log.d(TAG, "got scan type " + scanResult.getFormatName());
				if (scanResult != null)
				{
					try
					{
						Uri existingUri = Triage.findUri(this, Triage.TRACKING_ID+"='"+scanResult.getContents()+"'");
						if (existingUri == null)
						{
							onAddNewCasualty(scanResult.getContents());
						}
						else
						{
							onViewExistingCasualty(existingUri);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if (requestCode == ACTIVITY_MANUAL)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				try
				{
					String trackingID = intent.getStringExtra("tracking_id");
					if (trackingID == null || trackingID.length() == 0)
					{
						
					}
					Uri existingUri = Triage.findUri(this, Triage.TRACKING_ID+"='"+trackingID+"'");
					if (existingUri == null)
					{
						onAddNewCasualty(trackingID);
					}
					else
					{
						onViewExistingCasualty(existingUri);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void onAddNewCasualty(String trackingID)
	{
		Uri addressUri = null;
		Uri personUri = null;
		Person.Content personContent = new Person.Content(this);
		personContent.setLast("Doe");
		personUri = personContent.insert(Person.CONTENT_URI);
		LocationManager locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
		_lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (_lastKnownLocation != null)
		{
			Address.Content addressContent = new Address.Content(this, Config.getDeviceId(this));
			addressContent.setWKT(_lastKnownLocation);
			addressUri = addressContent.insert(this);
		}
		Triage.Content triageContent = new Triage.Content(this, Config.getDeviceId(this), MercurySettings.getCurrentIncidentId(this));
		triageContent.setTrackingID(trackingID);
		if (addressUri != null)
		{
			triageContent.setAddressID(addressUri);
		}
		if (personUri != null)
		{
			triageContent.setPersonID(personUri);
		}
		Uri uri = triageContent.insert(Triage.CONTENT_URI);
		if (uri != null)
		{
			Intent editIntent = new Intent(Intent.ACTION_INSERT, uri);
			startActivity(editIntent);
		}
	}

	private void onViewExistingCasualty(Uri uri)
	{
		Intent editIntent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(editIntent);
	}
	
	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Incident");
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

	private void addResource(int resId)
	{
		try
		{
			_resources.add(resId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private class MyLocationListener implements LocationListener
	{
		private boolean _gotFix = false;

		@Override
		public void onLocationChanged(Location arg0)
		{
			if (_gotFix == false)
			{
				_gotFix = true;
				Toast.makeText(TriageMainActivity.this, "Acquired accurate Triage location", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}
	
	private class MyAdapter extends BaseAdapter
	{
		public int getCount() 
		{
			return _resources.size();
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public boolean areAllItemsEnabled()
		{
			return true;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
	//		if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.launcher_list_item, null);
			}
			ImageView imageView = (ImageView)convertView.findViewById(R.id.icon);
//			FrameLayout countGroup = (FrameLayout)convertView.findViewById(R.id.count_group);
//			TextView countView = (TextView)countGroup.findViewById(R.id.count);
			try
			{
				imageView.setImageResource(_resources.get(position));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return convertView;
		}
	}
}