package com.futureconcepts.ax.trinity.geo;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.dataset.MapViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.BuildConfig;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelMapActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MainMapActivity extends ModelMapActivity implements Client
{
	private static final String TAG = MainMapActivity.class.getSimpleName();
	
	private static final String SAVED_ZOOM_VISIBILITY = "ZoomVisibility";
	private static final String SAVED_ZOOM_LEVEL = "ZoomLevel";
	private static final String SAVED_CENTER_ME = "SavedCenterMe";
//	private static final String SAVED_TRACKS = "SavedTracks";
//	private static final String SAVED_ACTIVE_TRACKS = "ActiveTracks";
//	private static final String SAVED_SHOW_DATA = "ShowData";
	private static final String SAVED_LATITUDE = "Latitude";
	private static final String SAVED_LONGITUDE = "Longitude";
	
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute

	private Handler _handler = new Handler();
	private AssetObserver _assetObserver;
	private AddressObserver _addressObserver;
	private IncidentObserver _incidentObserver;
	private TriageObserver _triageObserver;
//	private HashMap<String, LayerInfo> mLayers;
	private ArrayList<Layer> _layers = new ArrayList<Layer>();
	public MapView _mapView;
	private boolean _zoomControlEnabled;
	private boolean _gotFirstFix = false;
	boolean _centerMe = false;
	private MyLocationOverlay _myLocationOverlay;
	private Incident _incident;
	private EquipmentViewCursorByCheckIn _equipment;
	private PersonnelViewCursorByCheckIn _personnel;
	private Tactic _tasks;
	private Tactic _priorityTasks;
	private Triage _triage;
	private Bundle _savedInstanceState;
	private LoadOverlaysAsyncTask _loadOverlaysTask;
	private SyncServiceConnection _syncServiceConnection;
	private Timer _resyncIntervalTimer;
	
	public MapView getMapView()
	{
		return _mapView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (BuildConfig.DEBUG)
        {
    		setContentView(R.layout.geo_map_debug);
        }
        else
        {
        	setContentView(R.layout.geo_map);
        }
        DateTime.setContext(this);
        if (MercurySettings.getCurrentIncidentId(this) != null)
        {
    		_mapView = (MapView)findViewById(R.id.map);
    		_mapView.setClickable(true);
    		_savedInstanceState = savedInstanceState;
    		_loadOverlaysTask = new LoadOverlaysAsyncTask();
    		_loadOverlaysTask.execute();
        }
        else
        {
        	onError("Please select an incident");
		}
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		centerMapOnSomething(intent);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (_myLocationOverlay != null)
		{
			_myLocationOverlay.enableMyLocation();
		}
		if (_incident != null && _incident.getCount() == 1)
		{
			_incident.moveToFirst();
		}
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
		if (_myLocationOverlay != null)
		{
			_myLocationOverlay.disableMyLocation();
		}
		_resyncIntervalTimer.cancel();
	}
	
	@Override
	public void onDestroy()
	{
		if (_loadOverlaysTask != null)
		{
			_loadOverlaysTask.cancel(true);
		}
		unregisterContentObserver(_addressObserver);
		unregisterContentObserver(_assetObserver);
		unregisterContentObserver(_incidentObserver);
		unregisterContentObserver(_triageObserver);
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
			return addPlacemark();
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
	
	private final class LoadOverlaysAsyncTask extends AsyncTask<Void, Integer, Exception>
	{
		private ProgressDialog _loadOverlayDataProgressDialog;
		
		@Override
		protected void onPreExecute()
		{
			_loadOverlayDataProgressDialog = ProgressDialog.show(MainMapActivity.this, "Loading", "Loading data...", true, false);
		}
		
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	    	try
	    	{
		    	Context context = MainMapActivity.this;
		    	String incidentID = MercurySettings.getCurrentIncidentId(MainMapActivity.this);
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_incident = Incident.query(context, Uri.withAppendedPath(Incident.CONTENT_URI, incidentID)));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingCursor(_equipment = EquipmentViewCursorByCheckIn.query(context));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingCursor(_personnel = PersonnelViewCursorByCheckIn.query(context));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_tasks = Tactic.queryTasks(context, MercurySettings.getCurrentOperationalPeriodId(MainMapActivity.this)));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_priorityTasks = Tactic.queryPriorityTasks(context, MercurySettings.getCurrentIncidentId(context)));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_triage = Triage.queryIncident(context, MercurySettings.getCurrentIncidentId(MainMapActivity.this)));
		    	}
	    	}
	    	catch (Exception e)
	    	{
	    		result = e;
	    	}
	    	return result;
	     }

//	     protected void onProgressUpdate(Integer... progress)
//	     {
//	         setProgressPercent(progress[0]);
//	     }

	     protected void onPostExecute(Exception result)
	     {
	    	 if (_loadOverlayDataProgressDialog != null)
	    	 {
				_loadOverlayDataProgressDialog.dismiss();
				_loadOverlayDataProgressDialog = null;
	    	 }
	    	 if (result == null && isFinishing() == false)
	    	 {
	    		addIncidentOverlay();
    			addEquipmentOverlay();
    			addPersonnelOverlay();
    			addTaskOverlay();
    			addPriorityTaskOverlay();
    			addTriageOverlay();
    			centerMapOnSomething(getIntent());
    			registerContentObservers();
    			setZoomControlEnabled(true);
    			addMyLocationOverlay();
    			_mapView.invalidate();
    			if (_savedInstanceState == null)
    			{
    				sync();
    			}
	    	 }
	     }
	}
	
	private void centerMapOnSomething(Intent intent)
	{
		try
		{
			if (_savedInstanceState != null)
			{
				centerLastPosition();
			}
			else if (intent.getData() != null)
			{
				Uri uri = intent.getData();
				String mimeType = getContentResolver().getType(uri);
				if (mimeType.equals(Address.CONTENT_ITEM_TYPE))
				{
					centerAddress(uri);
				}
			}
			else if ((_incident != null) && (_incident.getAddressID() != null))
			{
				Uri uri = Uri.withAppendedPath(Address.CONTENT_URI, _incident.getAddressID());
				centerAddress(uri);
			}
			else
			{
				_centerMe = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void registerContentObservers()
	{
		_addressObserver = new AddressObserver();
		_assetObserver = new AssetObserver();
		_incidentObserver = new IncidentObserver();
		_triageObserver = new TriageObserver();
		registerContentObserver(Address.CONTENT_URI, true, _addressObserver);
		registerContentObserver(Asset.CONTENT_URI, true, _assetObserver);
		registerContentObserver(Incident.CONTENT_URI, true, _incidentObserver);
		registerContentObserver(Triage.CONTENT_URI, true, _triageObserver);
	}
	
	private void sync()
	{
		if (_syncServiceConnection != null && _syncServiceConnection.isConnected())
		{
			_syncServiceConnection.syncDataset(MapViewDataSet.class.getName());
		}
	}
	
	private void addIncidentOverlay()
	{
		try
		{
			if (_incident != null)
			{
				addOverlay(new IncidentOverlay(_mapView, _incident));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addEquipmentOverlay()
	{
		Log.d(TAG, "addEquipmentOverlay");
		try
		{
			if (_equipment != null)
			{
				addOverlay(new EquipmentOverlay(this, _equipment));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addPersonnelOverlay()
	{
		Log.d(TAG, "addPersonnelOverlay");
		try
		{
			if (_personnel != null)
			{
				addOverlay(new PersonnelOverlay(this, _personnel));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addTaskOverlay()
	{
		try
		{
			if (_tasks != null)
			{
				TaskLayer layer = new TaskLayer(_mapView, _tasks);
				layer.populate();
				addLayer(layer);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addPriorityTaskOverlay()
	{
		try
		{
			if (_priorityTasks != null)
			{
				TaskLayer layer = new TaskLayer(_mapView, _priorityTasks);
				layer.populate();
				addLayer(layer);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addTriageOverlay()
	{
		try
		{
			if (_triage != null)
			{
				addOverlay(new TriageOverlay(_mapView, _triage));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addLayer(Layer layer)
	{
		_layers.add(layer);
	}
	
//	private void addLayer(String name, Class<?> theClass, int resId)
//	{
//		if (mLayers == null)
//		{
//			mLayers = new HashMap<String, LayerInfo>();
//		}
//		LayerInfo info = new LayerInfo();
//		info.setName(name);
//		info.setClass(theClass);
//		info.setIconId(resId);
//		info.setSelected(false);
//		mLayers.put(name, info);
//	}
	
//	private static final int[] layers = new int[] {
//		//	R.id.menu_layer_select_all,
//			R.id.menu_layer_mylocation,
//			R.id.menu_layer_asset};
	
	
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
//		for (int i = 0; i < layers.length; i++)
//		{
//			setCheckItem(menu, layers[i], false);
//		}
//		int overlaySize = _mapView.getOverlays().size();
//		for (int i = 0; i < overlaySize; i++)
//		{
//			Object o = mMapView.getOverlays().get(i);
//			if (o instanceof MyLocationOverlay)
//			{
//				setCheckItem(menu, R.id.menu_layer_mylocation, true);
//			}
//			else if (o instanceof AssetOverlay)
//			{
//				setCheckItem(menu, R.id.menu_layer_asset, true);
//			}
//		}
		if (_mapView != null)
		{
			setCheckItem(menu, R.id.menu_layer_sat, _mapView.isSatellite());
			setCheckItem(menu, R.id.menu_layer_traffic, _mapView.isTraffic());
	//		setCheckItem(menu, R.id.menu_layer_streetview, _mapView.isStreetView());
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		try
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
			_assetObserver.onChange(true);
			break;
		case R.id.menu_gps_status:
			return gpsStatus();
		case R.id.menu_center_pan:
			_centerMe = !_centerMe;
			if (_centerMe)
			{
				tryCenterMeNow();
			}
			break;
		case R.id.menu_center_incident:
			centerIncident();
			break;
//		case R.id.menu_layer_all:
//			if (mapContainsLayer(MyLocationOverlay.class) == false)
//			{
//				addMyLocationOverlay(false);
//			}
//			if (mapContainsLayer(AssetOverlay.class) == false)
//			{
//				addAssetOverlay();
//			}
//			if (mapContainsLayer(DrawingOverlay.class) == false)
//			{
//				addOverlay(new DrawingOverlay(this));
//			}
//			if (mapContainsLayer(MediaImagesOverlay.class) == false)
//			{
//				addOverlay(new MediaImagesOverlay(this));
//			}
//			if (mapContainsLayer(MediaVideoOverlay.class) == false)
//			{
//				addOverlay(new MediaVideoOverlay(this));
//			}
//			if (mapContainsLayer(TacticOverlay.class) == false)
//			{
//				addOverlay(new TacticOverlay(this, getModelContext()));
//			}
//			break;
//		case R.id.menu_layer_mylocation:
//			if (mapContainsLayer(MyLocationOverlay.class))
//			{
//				removeMyLocationOverlay();
//			}
//			else
//			{
//				addMyLocationOverlay(false);
//			}
//			break;
		case R.id.menu_layer_sat:
			_mapView.setSatellite(!item.isChecked());
			break;
		case R.id.menu_layer_traffic:
			_mapView.setTraffic(!item.isChecked());
			break;
//		case R.id.menu_layer_streetview:
//			_mapView.setStreetView(!item.isChecked());
//			break;
			//see JIRA:TKTRIN-19
//		case R.id.menu_icon_smaller:
//			changeIconSizeBump(-4);
//			break;
//		case R.id.menu_icon_larger:
//			changeIconSizeBump(8);
//			break;
//		case R.id.menu_icon_normal:
//			zeroIconSizeBump();
//			break;
//		case R.id.menu_test1:
//			onMenuTest1();
//			break;
		}
		return false;
	}

	private void onMenuTest1()
	{
		Log.d(TAG, "onMenuTest1");
		if (_addressObserver != null)
		{
			_addressObserver.onChange(true);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void changeIconSizeBump(final int bumpAdjustValue)
	{
		int oldValue = Config.getMapIconSizeBump(this);
		int newValue = oldValue + bumpAdjustValue;
		Config.setMapIconSizeBump(this, newValue);
		Log.d(TAG, "IconSizeBump=" + newValue);
		List<Overlay> overlays = _mapView.getOverlays();
		for (Overlay overlay : overlays)
		{
			if (overlay instanceof AssetOverlay)
			{
				AssetOverlay assetOverlay = (AssetOverlay)overlay;
				assetOverlay.repopulate();
			}
		}
		_mapView.invalidate();
	}

	private void zeroIconSizeBump()
	{
		Config.setMapIconSizeBump(this, 0);
		List<Overlay> overlays = _mapView.getOverlays();
		for (Overlay overlay : overlays)
		{
			if (overlay instanceof AssetOverlay)
			{
				AssetOverlay assetOverlay = (AssetOverlay)overlay;
				assetOverlay.repopulate();
			}
		}
		_mapView.invalidate();
	}
	
	private boolean addPlacemark()
	{
		boolean result = false;
		if (_myLocationOverlay != null)
		{
			Location location = _myLocationOverlay.getLastFix();
			if (location != null)
			{
				// Add new placemark/drawing here
			}
		}
		return result;
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
	
	private boolean gpsStatus()
	{
		Intent intent = new Intent(this, GpsStatus.class);
		startActivity(intent);
		return true;
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
	
	private void centerIncident()
	{
		try
		{
			if (_incident != null && _incident.getCount() == 1)
			{
				_incident.moveToFirst();
				Address address = _incident.getAddress(this);
				if (address != null)
				{
					centerOnPoint(address.getWKTAsPoint());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void centerAddress(Uri uri)
	{
		Address cursor = null;
		try
		{
			cursor = Address.query(this, uri);
			if (cursor != null)
			{
				Point point = cursor.getWKTAsPoint();
				if (point != null)
				{
					centerOnPoint(point);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
	}
	
	private void centerLastPosition()
	{
		Point point = new Point();
		point.y = _savedInstanceState.getInt(SAVED_LATITUDE);
		point.x = _savedInstanceState.getInt(SAVED_LONGITUDE);
		int zoom = _savedInstanceState.getInt(SAVED_ZOOM_LEVEL);
		centerOnPoint(point);
		setZoom(zoom);
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
		
	private final class IncidentObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable() {
			@Override
			public void run() {
				try
				{
					boolean doInvalidate = false;
					if (_incident != null && _incident.isClosed() == false)
					{
						_incident.requery();
						List<Overlay> overlays = _mapView.getOverlays();
						for (Overlay overlay : overlays)
						{
							if (overlay instanceof IncidentOverlay)
							{
								IncidentOverlay incidentOverlay = (IncidentOverlay)overlay;
								incidentOverlay.repopulate();
								doInvalidate = true;
							}
						}
					}
					if (doInvalidate)
					{
						_mapView.invalidate();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		public IncidentObserver()
		{
			super(_handler);
		}
		@Override
		public void onChange(boolean selfChange)
		{
			_handler.removeCallbacks(_deferredRunnable);
			_handler.postDelayed(_deferredRunnable, 1000 * 15);
		}
	}
	private final class AddressObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable() {
			@Override
			public void run() {
				if ((_incidentObserver != null) && (_incident != null) && (_incident.isClosed() == false))
				{
					_incidentObserver.onChange(true);
				}
				if (_assetObserver != null)
				{
					_assetObserver.onChange(true);
				}
				if (_triageObserver != null)
				{
					_triageObserver.onChange(true);
				}
			}
		};
		public AddressObserver()
		{
			super(_handler);
		}
		@Override
		public void onChange(boolean selfChange)
		{
			_handler.removeCallbacks(_deferredRunnable);
			_handler.postDelayed(_deferredRunnable, 1000 * 15);
		}
	}
	private final class AssetObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable()
		{
			@Override
			public void run() {
				try
				{
					boolean doInvalidate = false;
					if (_equipment != null)
					{
						stopManagingCursor(_equipment);
						_equipment.close();
						_equipment = EquipmentViewCursorByCheckIn.query(MainMapActivity.this);
						startManagingCursor(_equipment);
						List<Overlay> overlays = _mapView.getOverlays();
						for (Overlay overlay : overlays)
						{
							if (overlay instanceof EquipmentOverlay)
							{
								overlays.remove(overlay);
								overlays.add(new EquipmentOverlay(MainMapActivity.this, _equipment));
//								EquipmentOverlay equipmentOverlay = (EquipmentOverlay)overlay;
//								equipmentOverlay.repopulate(_equipment);
								doInvalidate = true;
								break;
							}
						}
					}
					if (_personnel != null)
					{
						stopManagingCursor(_personnel);
						_personnel.close();
						_personnel = PersonnelViewCursorByCheckIn.query(MainMapActivity.this);
						startManagingCursor(_personnel);
						List<Overlay> overlays = _mapView.getOverlays();
						for (Overlay overlay : overlays)
						{
							if (overlay instanceof PersonnelOverlay)
							{
								overlays.remove(overlay);
								overlays.add(new PersonnelOverlay(MainMapActivity.this, _personnel));
//								PersonnelOverlay personnelOverlay = (PersonnelOverlay)overlay;
//								personnelOverlay.repopulate(_personnel);
								doInvalidate = true;
								break;
							}
						}
					}
					if (doInvalidate)
					{
						_mapView.invalidate();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		public AssetObserver()
		{
			super(_handler);
		}
		@Override
		public void onChange(boolean selfChange)
		{
			_handler.removeCallbacks(_deferredRunnable);
			_handler.postDelayed(_deferredRunnable, 1000 * 15);
		}
	}
	private final class TriageObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable()
		{
			@Override
			public void run() {
				try
				{
					boolean doInvalidate = false;
					if (_triage != null)
					{
						_triage.requery();
						List<Overlay> overlays = _mapView.getOverlays();
						for (Overlay overlay : overlays)
						{
							if (overlay instanceof TriageOverlay)
							{
								TriageOverlay triageOverlay = (TriageOverlay)overlay;
								triageOverlay.repopulate();
								doInvalidate = true;
							}
						}
					}
					if (doInvalidate)
					{
						_mapView.invalidate();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		public TriageObserver()
		{
			super(_handler);
		}
		@Override
		public void onChange(boolean selfChange)
		{
			_handler.removeCallbacks(_deferredRunnable);
			_handler.postDelayed(_deferredRunnable, 1000 * 15);
		}
	}
}
