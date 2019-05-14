package com.futureconcepts.jupiter;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.compass.MainCompassActivity;
import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.layers.CoordinatesLayer;
import com.futureconcepts.jupiter.layers.DistanceOverTerrainRulerLayer;
import com.futureconcepts.jupiter.layers.DistanceRulerLayer;
import com.futureconcepts.jupiter.layers.FolderLayer;
import com.futureconcepts.jupiter.layers.MyLocationLayer;
import com.futureconcepts.jupiter.layers.ScaleBarLayer;
import com.futureconcepts.jupiter.layers.TopographyLayer;
import com.futureconcepts.jupiter.layers.UtmGridLayer;
import com.futureconcepts.jupiter.layers.ZoomLayer;
import com.futureconcepts.jupiter.map.Feature;
import com.futureconcepts.jupiter.map.MapComponent;
import com.futureconcepts.jupiter.map.MapView;
import com.futureconcepts.jupiter.map.PointFeature;
import com.futureconcepts.jupiter.map.MapComponent.Layer;
import com.futureconcepts.jupiter.util.FormatterFactory;
import com.futureconcepts.jupiter.util.FormatterFactory.LocationFormatter;
import com.futureconcepts.jupiter.util.FormatterFactory.ScalarFormatter;

public class MainMapActivity extends Activity
{
	private static final String TAG = "MainMapActivity";
	
	public static final String ACTION_SHOW_ON_MAP2 = "com.futureconcepts.jupiter.action.show_on_map";
	
	private static final int ACTIVITY_SETUP_ROUTE = 2;

    private MapView _mapView;
    private MapComponent _mapComponent;
    private Config _config;
    private boolean _closeMapComponentIfDestroyed = true;
    private MyLocationLayer _myLocationLayer;
    private MapComponent.Listener _myMapComponentListener;
    private LocationManager _locationManager;
    private MyLocationListener _locationListener;
	private int _minTime;
	private float _minDistance;
	private OnSharedPreferenceChangeListener _sharedPreferenceChangeListener;
	private FormatterFactory _formatterFactory;
	private LocationFormatter _locationFormatter;
	private ScalarFormatter _distanceFormatter;
	private boolean _followMe;
	private View.OnTouchListener _tapHandler;
	private Vibrator _vibrator;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        _locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
        _config = Config.getInstance(this);
        _vibrator = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        _formatterFactory = new FormatterFactory(this);
        _sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				Log.d(TAG, "onSharedPreferenceChanged " + key);
				if (key.equals(Config.FORMAT_LOCATION))
				{
					onPreferenceChangedFormatLocation(sharedPreferences);
				}
				else if (key.equals(Config.FORMAT_DISTANCE))
				{
					onPreferenceChangedFormatDistance(sharedPreferences);
				}
				else if (key.equals(Config.FOLLOW_ME))
				{
					onPreferenceChangedFollowMe(sharedPreferences);
				}
				else if (key.startsWith(Config.LAYER_ENABLED))
				{
					onPreferenceChangedLayerEnabled(sharedPreferences, key);
				}
			}
        };
        _config.getSharedPreferences().registerOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
        setContentView(R.layout.main_map);
	    _locationListener = new MyLocationListener();
	    _myMapComponentListener = new MyMapComponentListener();
        _mapView = (MapView)findViewById(R.id.map_view);
        _mapComponent = (MapComponent)getLastNonConfigurationInstance();
        if (_mapComponent == null)
        {
        	_mapComponent = new MapComponent(this, _mapView);
            _mapComponent.registerListener(_myMapComponentListener);
            _mapComponent.loadMaps();
        }
        else
        {
        	_mapComponent.setMapView(_mapView);
            _mapComponent.registerListener(_myMapComponentListener);
        	_myLocationLayer = (MyLocationLayer)_mapComponent.findLayerById(MyLocationLayer.ID);
        	synchronizeStateWithPreferences();
        	_mapComponent.refreshViews();
        }
	    if (getIntent().getAction() != null)
	    {
	    	onNewIntent(getIntent());
	    }
	    setTapHandler(new FeatureSelectTapHandler());
    }

    @Override
    public void onNewIntent(Intent intent)
    {
    	Log.d(TAG, "onNewIntent");
    	super.onNewIntent(intent);
    	String action = intent.getAction();
    	if (action.equals(Intent.ACTION_VIEW))
    	{
			if (intent.getData() != null)
			{
				onShowOnMap(intent.getData());
			}
    	}
    }
    
    private void onPreferenceChangedFormatLocation(SharedPreferences sharedPreferences)
    {
		_locationFormatter = _formatterFactory.getLocationFormatter();
    }

    private void onPreferenceChangedFormatDistance(SharedPreferences sharedPreferences)
    {
		_distanceFormatter = _formatterFactory.getDistanceFormatter();
    }
    
    private void onPreferenceChangedFollowMe(SharedPreferences sharedPreferences)
    {
		_followMe = sharedPreferences.getBoolean(Config.FOLLOW_ME, false);
//		if (_followMe)
//		{
//			if (_locationManager != null)
//			{
//				Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//				if (location != null)
//				{
//					_locationListener.onLocationChanged(location);
//				}
//			}
//		}
    }

    private void onPreferenceChangedLayerEnabled(SharedPreferences sharedPreferences, String key)
    {
    	boolean nowEnabled = sharedPreferences.getBoolean(key, false);
		String layerId = new String(key.replace(Config.LAYER_ENABLED, ""));
    	if (layerId.equals(ZoomLayer.ID))
    	{
    		findViewById(R.id.zoom_control).setVisibility(nowEnabled ? View.VISIBLE : View.GONE);
    	}
    	else if (layerId.equals(CoordinatesLayer.ID))
    	{
    	//	findViewById(R.id.coords).setVisibility(nowEnabled ? View.VISIBLE : View.GONE);
    	}
    	else
    	{
			Layer mapLayer = _mapComponent.findLayerById(layerId);
			if (mapLayer != null)
			{
				if (nowEnabled != mapLayer.isEnabled())
				{
					if (mapLayer.isLoaded() == false && nowEnabled == true)
					{
						mapLayer.load();
					}
					mapLayer.setEnabled(nowEnabled);
				}
			}
		}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        // this function is called when activity shuts down due to a configuration change,
        // so we prevent disposing of the map component
        _closeMapComponentIfDestroyed = false;
        // unset the associated map view and preserve map component
        _mapComponent.setMapView(null);
        return _mapComponent;
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	Log.d(TAG, "onPause");
        if (_locationListener != null)
        {
        	_locationManager.removeUpdates(_locationListener);
        }
    	if (isFinishing())
    	{
	        try
	        {
	        	if (_sharedPreferenceChangeListener != null)
	        	{
	        		_config.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
	        		_sharedPreferenceChangeListener = null;
	        	}
	        	float lastScale = (float)_mapComponent.getScale();
	        	if (lastScale != 0)
	        	{
	        		_config.setLastMapScale(lastScale);
	        		Log.d(TAG, "setLastMapScale " + lastScale);
	        		Point2D.Double point = _mapComponent.getPoint();
	        		_config.setLastMapLatitude((float) point.y);
	        		_config.setLastMapLongitude((float) point.x);
	        	}
		        if (_closeMapComponentIfDestroyed)
		        {
			        if (_mapComponent != null)
			        {
						_mapComponent.close();
			        	_mapComponent = null;
			        }
		        }
	        }
	    	catch (IOException e)
	    	{
				e.printStackTrace();
			}
    	}
    }
    
    @Override
    public void onResume()
    {
    	Log.d(TAG, "onResume");
    	super.onResume();
		_minTime = 1000 * 10;
		_minDistance = 10.0f;
//		_minTime = 0;
//		_minDistance = 0.0f;
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _minTime, _minDistance, _locationListener);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_map_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		if (_config.getTripId() != null)
		{
			if (TrackRecorderService.getInstance() != null)
			{
				menu.findItem(R.id.menu_record).setEnabled(false);
				menu.findItem(R.id.menu_mark_waypoint).setEnabled(true);
				menu.findItem(R.id.menu_mark_waypoint).setTitle("Mark Waypoint");
			}
			else
			{
				menu.findItem(R.id.menu_record).setEnabled(true);
				menu.findItem(R.id.menu_mark_waypoint).setEnabled(true);
				menu.findItem(R.id.menu_mark_waypoint).setTitle("Mark Placemark");
			}
		}
		else
		{
			menu.findItem(R.id.menu_mark_waypoint).setEnabled(false);
			menu.findItem(R.id.menu_record).setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_about:
			startMyActivity(AboutActivity.class);
			break;
		case R.id.menu_mark_waypoint:
			onMenuMarkWaypoint();
			break;
		case R.id.menu_layers:
			onMenuLayers();
			break;
		case R.id.menu_mylocation:
			onMenuMyLocation();
			break;
		case R.id.menu_utm_grid:
			onMenuUtmGrid();
			break;
		case R.id.menu_record:
			startSubActivity(SetupTrackActivity.class, ACTIVITY_SETUP_ROUTE);
			break;
		case R.id.menu_compass:
			startMyActivity(MainCompassActivity.class);
			break;
		case R.id.menu_folders:
			onMenuFolders();
			break;
		case R.id.menu_trips:
			startMyActivity(TripsActivity.class);
			break;
		case R.id.menu_preferences:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menu_distance_ruler:
			findViewById(R.id.ruler).setVisibility(View.VISIBLE);
			setTapHandler(new DistanceRulerTapHandler());
			break;
//		case R.id.menu_distance_over_terrain_ruler:
//			findViewById(R.id.ruler).setVisibility(View.VISIBLE);
//			setTapHandler(new DistanceOverTerrainRulerTapHandler());
//			break;
			
//		case R.id.menu_touch_mode_zoom_select:
//			_mapComponent.setTouchMode(MapComponent.TOUCH_MODE_ZOOM_SELECT);
//			break;
//		case R.id.menu_touch_mode_pan:
//			_mapComponent.setTouchMode(MapComponent.TOUCH_MODE_PAN);
//			break;
//		case R.id.menu_touch_mode_feature_select:
//			_mapComponent.setTouchMode(MapComponent.TOUCH_MODE_FEATURE_SELECT);
//			break;
		}
		return false;
	}

	private long _startTime;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
			_startTime = ev.getEventTime();
		}
		else if (ev.getAction() == MotionEvent.ACTION_UP)
		{
			long endTime = ev.getEventTime();
			if (endTime - _startTime < 175)
			{
				if (_tapHandler != null)
				{
					_vibrator.vibrate(100);
					if (_tapHandler.onTouch(_mapView, ev) == false)
					{
						setTapHandler(null);
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ACTIVITY_SETUP_ROUTE)
		{
			if (resultCode == RESULT_OK)
			{
				addFolderLayer(data.getData());
				TrackRecorderService.startIfNeccessary(this, data);
			}
		}
	}

	private void setTapHandler(View.OnTouchListener value)
	{
		try
		{
			if (_tapHandler != null)
			{
				if (_tapHandler instanceof Closeable)
				{
					Closeable closeit = (Closeable)_tapHandler;
					if (closeit != null)
					{
						closeit.close();
					}
				}
			}
			_tapHandler = value;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void buttonDistanceRulerOnClick(View view)
	{
		if (_tapHandler != null)
		{
			setTapHandler(null);
			findViewById(R.id.ruler).setVisibility(View.GONE);
		}
	}
	
	private void startMyActivity(Class<?> theClass)
	{
		Intent intent = new Intent(this, theClass);
		startActivity(intent);
	}
	
	private void synchronizeStateWithPreferences()
	{
	    SharedPreferences sharedPreferences = _config.getSharedPreferences();
	    if (sharedPreferences != null)
	    {
	    	onPreferenceChangedFormatLocation(sharedPreferences);
	    	onPreferenceChangedFormatDistance(sharedPreferences);
	        onPreferenceChangedFollowMe(sharedPreferences);
	        onPreferenceChangedLayerEnabled(sharedPreferences, Config.LAYER_ENABLED + ZoomLayer.ID);
	        onPreferenceChangedLayerEnabled(sharedPreferences, Config.LAYER_ENABLED + CoordinatesLayer.ID);
	    }
	}
	
	private void onMenuFolders()
	{
		if (_config.getTripId() != null)
		{
			Folder folder = Folder.getFolderById(this, _config.getTripId());
			if (folder != null)
			{
				Intent intent = new Intent(this, ViewFolderActivity.class);
				intent.setData(Uri.withAppendedPath(Folder.CONTENT_URI, folder.get_ID()));
				folder.close();
				folder = null;
				startActivity(intent);
			}
		}
	}

	private void onShowOnMap(Uri uri)
	{
		String mimeType = getContentResolver().getType(uri);
		if (mimeType.equals(Placemark.CONTENT_ITEM_TYPE))
		{
			showPlacemarkOnMap(uri);
		}
		else if (mimeType.equals(Folder.CONTENT_ITEM_TYPE))
		{
			Folder folder = new Folder(getContentResolver().query(uri, null, null, null, null));
			if (folder != null)
			{
				if (folder.getCount() == 1)
				{
					folder.moveToFirst();
					showFolderOnMap(folder);
				}
				folder.close();
				folder = null;
			}
		}
	}
		
	private void showFolderOnMap(Folder folder)
	{
		FolderLayer folderLayer = (FolderLayer)_mapComponent.findLayerById(folder.getId());
		if (folderLayer == null)
		{
			addFolderLayer(Uri.withAppendedPath(Folder.CONTENT_URI, folder.get_ID()));
		}
		else
		{
			if (folderLayer.isLoaded() == false)
			{
				folderLayer.load();
			}
			if (folderLayer.isEnabled() == false)
			{
				folderLayer.setEnabled(true);
			}
		}
	}
	
	private void showPlacemarkOnMap(Uri data)
	{
		Placemark placemark = Placemark.getPlacemarkByUri(this, data);
		if (placemark != null)
		{
			Folder folder = Folder.getFolderById(this, placemark.getParentId());
			if (folder != null)
			{
				showFolderOnMap(folder);
				if (placemark.getLatitude() != 0)
				{
					Point2D.Double point = new Point2D.Double(placemark.getLongitude(), placemark.getLatitude());
					_mapComponent.setPosition(point);
				}
				folder.close();
				folder = null;
			}
			placemark.close();
			placemark = null;
		}
	}

	private void onMenuMyLocation()
	{
		if (_myLocationLayer != null)
		{
			if (_myLocationLayer.isEnabled() == false)
			{
				_config.setLayerEnabledById(MyLocationLayer.ID, true);
			}
			Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null)
			{
				_myLocationLayer.onLocationChanged(location);
				_mapComponent.setPosition(location);
				alertPoint(new Point2D.Double(location.getLongitude(), location.getLatitude()));
			}
			else
			{
				Toast.makeText(this, "Location not available--please try again later", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void onMenuUtmGrid()
	{
		UtmGridLayer layer = (UtmGridLayer)_mapComponent.findLayerById(UtmGridLayer.ID);
		if (layer == null)
		{
			_mapComponent.setScale(4501);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			layer = new UtmGridLayer(this, _mapComponent);
			layer.load();
			layer.setEnabled(true);
			_config.setLayerEnabledById(UtmGridLayer.ID, true);
		}
		else
		{
			layer.setEnabled(false);
			_config.setLayerEnabledById(UtmGridLayer.ID, false);
			try
			{
				layer.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_mapComponent.removeLayerById(UtmGridLayer.ID);
		}
	}
		
	private void onMenuMarkWaypoint()
	{
		Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Uri uri = TrackRecorderService.markWaypointOrPlacemark(this, location);
		if (uri != null)
		{
			Intent intent = new Intent(this, EditPlacemarkActivity.class);
			intent.setData(uri);
			startActivity(intent);
		}
	}
	
	private void startSubActivity(Class<?> theClass, int subActivityCode)
	{
		Intent intent = new Intent(this, theClass);
		startActivityForResult(intent, subActivityCode);
	}
	
	private void addFolderLayer(Uri uri)
	{
		FolderLayer folderLayer = new FolderLayer(this, _mapComponent, uri);
		_mapComponent.attachLayer(folderLayer);
		folderLayer.load();
		folderLayer.setEnabled(true);
	}
	
	private void onMenuLayers()
	{
		Intent intent = new Intent(this, LayersActivity.class);
		Enumeration<Layer> layers = _mapComponent.getLayers();
		int i = 0;
		while (layers.hasMoreElements())
		{
			Layer layer = layers.nextElement();
			Bundle bundle = new Bundle();
			bundle.putString("id", layer.getId());
			bundle.putString("name", layer.getName());
			bundle.putBoolean("isenabled", layer.isEnabled());
			bundle.putInt("icon", layer.getIconId());
			intent.putExtra("layer_" + i++, bundle);
		}
		startActivity(intent);
	}
		
	public void attachLayer(String id)
	{
		_mapComponent.attachLayer(id);
	}
	
	public void updateCoords()
	{
		if (_mapComponent != null)
		{
		//	Point2D.Double point1 = new Point2D.Double(0, 0);
		//	updateScreenCoord(point1, _topLeftCoord);
		//	DisplayMetrics metrics = getResources().getDisplayMetrics();
		//	Point2D.Double point2 = new Point2D.Double(metrics.widthPixels, metrics.heightPixels);
		//	updateScreenCoord(point2, _bottomRightCoord);
		}
	}

	public void updateScreenCoord(Point2D.Double point, TextView view)
	{
		if (_mapComponent.screenToMap(point))
		{
			view.setText(_locationFormatter.format(point));
			view.setVisibility(View.VISIBLE);
		}
		else
		{
			view.setVisibility(View.GONE);
		}
	}
	
	private void alertPoint(Point2D.Double point)
	{
		StringBuilder sb = new StringBuilder();
		Point2D.Double point2 = new Point2D.Double(point.x, point.y);
		sb.append(_locationFormatter.format(point2));
		sb.append("\n");
		try
		{
			Geocoder geocoder = new Geocoder(this);
			List<Address> addresses = geocoder.getFromLocation(point.y, point.x, 1);
			if (addresses.size() > 0)
			{
				sb.append("\n");
				Address address = addresses.get(0);
				int lineCount = address.getMaxAddressLineIndex();
				for (int i = 0; i < lineCount; i++)
				{
					sb.append(address.getAddressLine(i));
					sb.append("\n");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sb.append("Address lookup failed");
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setCancelable(true);
		ab.setMessage(sb.toString());
		ab.show();
	}

    private class MyLocationListener implements LocationListener
    {
    	private boolean _gotFirstFix = false;
    	
		@Override
		public void onLocationChanged(Location location)
		{
			if (_gotFirstFix == false)
			{
				_gotFirstFix = true;
				Toast.makeText(MainMapActivity.this, "GPS Signal acquired", Toast.LENGTH_LONG).show();
			}
			if (_followMe)
			{
				if (_mapComponent != null)
				{
					_mapComponent.setPosition(location);
				}
			}
			if (_myLocationLayer != null && _myLocationLayer.isEnabled())
			{
				_myLocationLayer.onLocationChanged(location);
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
		public void onStatusChanged(String provider, final int status, final Bundle extras)
		{
		}
    }
    
    private class MyMapComponentListener implements MapComponent.Listener
    {
    	private ProgressDialog _progressDialog;
    	
		@Override
		public void onLoadBegin()
		{
	        _progressDialog = ProgressDialog.show(MainMapActivity.this, "Please wait", "Loading maps");
		}

		@Override
		public void onLoadComplete(int mapFilesCount)
		{
			attachLayer(TopographyLayer.ID);
			attachLayer(ScaleBarLayer.ID);
			attachLayer(ZoomLayer.ID);
			attachLayer(CoordinatesLayer.ID);
			_myLocationLayer = new MyLocationLayer(MainMapActivity.this, _mapComponent);
			_mapComponent.attachLayer(_myLocationLayer);
			Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null)
			{
				_myLocationLayer.onLocationChanged(location);
			}
	        DefaultZoomControl zoomView = (DefaultZoomControl)findViewById(R.id.zoom_control);
	    	zoomView.setMapController(_mapComponent);
	    	synchronizeStateWithPreferences();
			if (_progressDialog != null)
			{
				_progressDialog.dismiss();
			}
	    	Toast.makeText(MainMapActivity.this, String.format("loaded %d map files", mapFilesCount), Toast.LENGTH_LONG).show();
	    	float scale = _config.getLastMapScale();
	    	if (scale != 0)
	    	{
	    		Point2D.Double point = new Point2D.Double(_config.getLastMapLongitude(), _config.getLastMapLatitude());
	    		_mapComponent.setPositionScale(point, scale);
				StringBuilder sb = new StringBuilder();
				sb.append("Restoring map to:\n");
				sb.append(_locationFormatter.format(point));
	    		Toast.makeText(MainMapActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
	    	}
	    	else
	    	{
	    		_mapComponent.showOverview();
	    	}
		}

		@Override
		public void onLoadFail()
		{
			if (_progressDialog != null)
			{
				_progressDialog.dismiss();
			}
	    	Toast.makeText(MainMapActivity.this, "No maps found--please synchronize your device", Toast.LENGTH_LONG).show();
	    	finish();
		}
    }
    
    private class DistanceRulerTapHandler implements OnTouchListener
    {
    	private DistanceRulerLayer _layer;
    	
    	public DistanceRulerTapHandler()
    	{
    		_layer = (DistanceRulerLayer)_mapComponent.findLayerById(DistanceRulerLayer.ID);
    		if (_layer == null)
    		{
    			_layer = new DistanceRulerLayer(MainMapActivity.this, _mapComponent);
    		}
    		_layer.load();
    		_layer.setEnabled(true);
    	}
    	
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			Point2D.Double point = new Point2D.Double(event.getX(), event.getY());
			_mapComponent.screenToMap(point);
    		_layer.addPoint(_distanceFormatter, point);
    		return true;
		}
    }

    private class FeatureSelectTapHandler implements OnTouchListener
    {
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			Point2D.Double point = new Point2D.Double(event.getX(), event.getY());
			Feature feature = _mapComponent.getFeatureAtPixel(point, 30);
			if (feature instanceof PointFeature)
			{
				PointFeature pointFeature = (PointFeature)feature;
				Uri uri = pointFeature.getUri();
				if (uri != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			}
    		return true;
		}
    }

    private class DistanceOverTerrainRulerTapHandler implements OnTouchListener
    {
    	private DistanceOverTerrainRulerLayer _layer;
    	
    	public DistanceOverTerrainRulerTapHandler()
    	{
    		_layer = (DistanceOverTerrainRulerLayer)_mapComponent.findLayerById(DistanceOverTerrainRulerLayer.ID);
    		if (_layer == null)
    		{
    			_layer = new DistanceOverTerrainRulerLayer(MainMapActivity.this, _mapComponent);
    		}
    		_layer.load();
    		_layer.setEnabled(true);
    	}
    	
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			Point2D.Double point = new Point2D.Double(event.getX(), event.getY());
			_mapComponent.screenToMap(point);
    		_layer.addPoint(_distanceFormatter, point);
    		return true;
		}
    }
}
