package com.futureconcepts.ax.rex.activities.geo;

import java.io.Closeable;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressType;
import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.BuildConfig;
import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.rex.activities.SpecifyVictimsActivity;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MainMapActivity extends MapActivity implements Client
{
	private static final String TAG = MainMapActivity.class.getSimpleName();
	
	private static final String SAVED_ZOOM_VISIBILITY = "ZoomVisibility";
	private static final String SAVED_ZOOM_LEVEL = "ZoomLevel";
	private static final String SAVED_CENTER_ME = "SavedCenterMe";
	private static final String SAVED_LATITUDE = "Latitude";
	private static final String SAVED_LONGITUDE = "Longitude";

	private Handler _handler = new Handler();
	public MapView _mapView;
	private boolean _zoomControlEnabled;
	private boolean _gotFirstFix = false;
	boolean _centerMe = true;
	private MyLocationOverlay _myLocationOverlay;
	private IncidentOverlay _incidentOverlay;
	private Bundle _savedInstanceState;
	private SyncServiceConnection _syncServiceConnection;
	private Uri _data;
	private ProgressDialog _locatingDialog;
	
	public MapView getMapView()
	{
		return _mapView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        if (BuildConfig.DEBUG)
//        {
 //   		setContentView(R.layout.geo_map_debug);
   //     }
     //   else
        {
        	setContentView(R.layout.geo_map);
        }
        _data = getIntent().getData();
		_mapView = (MapView)findViewById(R.id.map);
		_mapView.setClickable(true);
		_savedInstanceState = savedInstanceState;
		registerContentObservers();
		setZoomControlEnabled(true);
		addMyLocationOverlay();
		addIncidentOverlay();
		if (_savedInstanceState == null)
		{
			sync();
		}
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
	}

	@Override
    public void onResume()
    {
    	super.onResume();
		if (_myLocationOverlay != null)
		{
			_myLocationOverlay.enableMyLocation();
		}
    	_locatingDialog = new ProgressDialog(this);
    	_locatingDialog.setTitle("Locating");
    	_locatingDialog.setMessage("Locating your current location...");
    	_locatingDialog.setCancelable(true);
    	_locatingDialog.setButton("Cancel", new OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				_locatingDialog = null;
				_myLocationOverlay.disableMyLocation();
				_myLocationOverlay = null;
			}
    	});
    	_locatingDialog.show();
    }
    
	@Override
	public void onPause()
	{
		super.onPause();
		if (_myLocationOverlay != null)
		{
			_myLocationOverlay.disableMyLocation();
		}
	}
	
	@Override
	public void onDestroy()
	{
		try
		{
			int size = _mapView.getOverlays().size();
			for (int i = 0; i < size; i++)
			{
				Object o = _mapView.getOverlays().get(i);
				if (Closeable.class.isAssignableFrom(o.getClass()))
				{
					Closeable closeableOverlay = (Closeable)o;
					closeableOverlay.close();
				}
			}
			if (_myLocationOverlay != null)
			{
				_myLocationOverlay.disableMyLocation();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_syncServiceConnection.disconnect();
		super.onDestroy();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		switch (event.getKeyCode())
		{
		case KeyEvent.KEYCODE_DPAD_CENTER:
//			return addPlacemark();
		}
		return super.dispatchKeyEvent(event);		
	}
	
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
	
	@Override
	protected boolean isLocationDisplayed()
	{
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public void onClick_MarkHere(View view)
	{
		Log.d(TAG, "onClick_MarkHere");
		_incidentOverlay.mark(_myLocationOverlay.getMyLocation());
	}

	public void onClick_UseMarked(View view)
	{
		Log.d(TAG, "onClick_UseMarked");
		String addressID = createAddress();
		ContentValues values = new ContentValues();
		values.put(IncidentRequest.ADDRESS, addressID);
		getContentResolver().update(_data, values, null, null);
    	Intent intent = new Intent(this, SpecifyVictimsActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
	}

	public void onClick_Skip(View view)
	{
		Log.d(TAG, "onClick_Skip");
    	Intent intent = new Intent(this, SpecifyVictimsActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
	}

	public void onMarkSet(GeoPoint geoPoint)
	{
		findViewById(R.id.btn_use_marked).setVisibility(View.VISIBLE);
	}
	
	private String createAddress()
	{
		String result = null;
		if (_incidentOverlay != null)
		{
			ContentValues values = new ContentValues();
			String addressID = UUID.randomUUID().toString();
			values.put(Address.ID, addressID);
			values.put(Address.TYPE, AddressType.MOBILE);
			Address.setWKTFromLocation(values, _incidentOverlay.getLocation());
			getContentResolver().insert(Address.CONTENT_URI, values);
			result = addressID;
		}
		return result;
	}
	
	private void registerContentObservers()
	{
//		_addressObserver = new AddressObserver();
//		_incidentObserver = new IncidentObserver();
//		registerContentObserver(Address.CONTENT_URI, true, _addressObserver);
//		registerContentObserver(Incident.CONTENT_URI, true, _incidentObserver);
	}
	
	private void sync()
	{
//		if (_syncServiceConnection != null)
//		{
//			_syncServiceConnection.syncDataset(MapViewDataSet.class.getName());
//		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		if (_centerMe)
		{
			menu.findItem(R.id.menu_center_pan).setTitle("Allow Pan");
		}
		else
		{
			menu.findItem(R.id.menu_center_pan).setTitle("Center Me");
		}
		setCheckItem(menu, R.id.menu_layer_sat, _mapView.isSatellite());
		setCheckItem(menu, R.id.menu_layer_traffic, _mapView.isTraffic());
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		GeoPoint center = null;
		outState.putInt(SAVED_ZOOM_LEVEL, _mapView.getZoomLevel());
		outState.putBoolean(SAVED_ZOOM_VISIBILITY, _zoomControlEnabled);
		if (_centerMe && (_myLocationOverlay != null))
		{
			center = _myLocationOverlay.getMyLocation();
		}
		else
		{
			center = _mapView.getMapCenter();
		}
		if (center != null)
		{
			outState.putInt(SAVED_LATITUDE, center.getLatitudeE6());
			outState.putInt(SAVED_LONGITUDE, center.getLongitudeE6());
		}
		outState.putBoolean(SAVED_CENTER_ME, _centerMe);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		GeoPoint center = null;
		setZoom(savedInstanceState.getInt(SAVED_ZOOM_LEVEL));
		setZoomControlEnabled(savedInstanceState.getBoolean(SAVED_ZOOM_VISIBILITY));
		if (savedInstanceState.containsKey(SAVED_LATITUDE))
		{
			center = new GeoPoint(savedInstanceState.getInt(SAVED_LATITUDE), savedInstanceState.getInt(SAVED_LONGITUDE));
		}
		_centerMe = savedInstanceState.getBoolean(SAVED_CENTER_ME);
		if (center != null)
		{
			animateTo(center);
		}
	}

	private void tryCenterMeNow()
	{
		if (_myLocationOverlay != null)
		{
			GeoPoint myPoint = _myLocationOverlay.getMyLocation();
			if (myPoint != null)
			{
				animateTo(myPoint);
			}
		}
	}
	
	private void setZoomControlEnabled(boolean value)
	{
		_zoomControlEnabled = value;
		_mapView.setBuiltInZoomControls(value);
	}
	
	private void addMyLocationOverlay()
	{
		MyLocationOverlay myLocationOverlay = new MyLocationOverlay( this, _mapView) {
			@Override
			public void onLocationChanged(Location location)
			{
				super.onLocationChanged(location);
				if (_gotFirstFix && _centerMe)
				{
					MyLocationOverlay overlay = _myLocationOverlay;
					if (overlay != null)
					{
						GeoPoint point = overlay.getMyLocation();
						if (point != null)
						{
							animateTo(point);
						}
					}
				}
			}
		};
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				_gotFirstFix = true;
				_handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						_locatingDialog.dismiss();
						_locatingDialog = null;
						findViewById(R.id.btn_mark_here).setVisibility(View.VISIBLE);
					}
				});
				if (_centerMe)
				{
					GeoPoint point = _myLocationOverlay.getMyLocation();
					if (point != null)
					{
						try
						{
							animateTo(point);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					if (_savedInstanceState == null)
					{
						setZoom(18);
					}
				}
			}
		});
		addOverlay(myLocationOverlay);
		_myLocationOverlay = myLocationOverlay;
		_myLocationOverlay.enableMyLocation();
	}

	private void removeMyLocationOverlay()
	{
		if (_myLocationOverlay != null)
		{
			_myLocationOverlay.disableMyLocation();
			_mapView.getOverlays().remove(_myLocationOverlay);
			_myLocationOverlay = null;
		}
	}
	
	private void addIncidentOverlay()
	{
		_incidentOverlay = new IncidentOverlay(this, _mapView);
		addOverlay(_incidentOverlay);
	}
	
	private void addOverlay(Overlay overlay)
	{
		_mapView.getOverlays().add(overlay);
	}
	
	private void setCheckItem(Menu menu, int id, boolean isChecked)
	{
		MenuItem item = menu.findItem(id);
		if (item != null)
		{
			item.setChecked(isChecked);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_refresh:
			sync();
			break;
		case R.id.menu_center_pan:
			_centerMe = !_centerMe;
			if (_centerMe)
			{
				tryCenterMeNow();
			}
			break;
		case R.id.menu_layer_sat:
			_mapView.setSatellite(!item.isChecked());
			break;
		case R.id.menu_layer_traffic:
			_mapView.setTraffic(!item.isChecked());
			break;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/* package private */
	void postInvalidateMap()
	{
		if (_mapView != null)
		{
			_mapView.postInvalidate();
		}
	}
	
	/* package private */
	void setZoom(int zoomLevel)
	{
		MapController controller = _mapView.getController();
		if (controller != null)
		{
			controller.setZoom(zoomLevel);
		}
	}
		
	/* package private */
	void animateTo(GeoPoint geoPoint)
	{
		if (geoPoint != null)
		{
			MapController controller = _mapView.getController();
			if (controller != null)
			{
				controller.animateTo(geoPoint);
			}
		}
	}
	
	/* package private */
	void centerOnGeoPoint(GeoPoint geoPoint)
	{
		try
		{
			if (geoPoint != null)
			{
				animateTo(geoPoint);
				setZoom(17);
				_centerMe = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	void centerOnPoint(Point point)
	{
		if (point != null)
		{
			centerOnGeoPoint(new GeoPoint(point.y, point.x));
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
}
