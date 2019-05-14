package com.futureconcepts.ax.trinity.osm;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.model.MapViewPosition;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressType;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.Drawing;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.dataset.MapViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.ModelOSMMapActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.geo.GpsStatus;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.osm.IncidentOverlay.IncidentOverlayMarker;
import com.futureconcepts.gqueue.MercurySettings;

public class MainMapOSMActivity extends ModelOSMMapActivity implements Client, MyLocationObserver,DangerZone{
	
	//private static final String SAVED_CENTER_ME = "SavedCenterMe";
	private AssetObserver _assetObserver;
	private AddressObserver _addressObserver;
	private IncidentObserver _incidentObserver;
	private TriageObserver _triageObserver;
	private DrawingObserver _drawingObserver;
	private Handler _handler = new Handler();
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
	private Bundle _savedInstanceState;
	private Incident _incident;
	private EquipmentViewCursorByCheckIn _equipment;
	private PersonnelViewCursorByCheckIn _personnel;
	private Collection _collection;
	private LoadOverlaysAsyncTask _loadOverlaysTask;
	private SyncServiceConnection _syncServiceConnection;
	private Timer _resyncIntervalTimer;
	private LruOSMIconsCache iconsCache;
	private Tactic _tasks;
	private Tactic _priorityTasks;
	private Triage _triage;
	private Address _notes;
	private MyLocationOverlay myLocationOverlay;
	private Drawing _drawings; 
	private boolean _centerMe = false;
	private int currentMapMode =1;//map mode online
	private int SELECT_FILE = 1;
	private String rootLocalDirectoryFolderPathForMaps =Environment.getExternalStorageDirectory()+"/Trinity/Maps/";
	private String[] centerModeOptions = {"Center Me", "Center Incident"};
	private String[] mapModeOptions = {"Offline","Online"};
	private String[] generalOptions = {"Refresh","Scale Map"};
	private String[] trackingOptions = {"Fast: 10sec/10m","Normal: 1min/50m","Casual: 15min/500m"};
	private ImageButton searchingLocationIcon;
	private BombLayer bombLayer;
	private ChemicalSpillLayer chemicalLayer;
	private MediaPlayer mp;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        DateTime.setContext(this);
        iconsCache = new LruOSMIconsCache();
   //     _mapSettings = MapSettings.getInstance(getApplicationContext());
        if (MercurySettings.getCurrentIncidentId(this) != null)
        {
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
        searchingLocationIcon= (ImageButton) findViewById(R.id.search_current_location);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fade);
        searchingLocationIcon.startAnimation(myFadeInAnimation);
        updateTrackerModeText();
        setAlphaToMenuButtons();
       setUpMediaPlayer();
	}
	private void setUpMediaPlayer()
	{
		mp = MediaPlayer.create(MainMapOSMActivity.this, R.raw.danger_zone);
//		mp.setOnCompletionListener(new OnCompletionListener(){
// 			@Override
// 			public void onCompletion(MediaPlayer arg0) {
// 				// TODO Auto-generated method stub
// 				
// 			}	        	 		
// 		});
	}
	
	private void setAlphaToMenuButtons()
	{
		 AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
	     alpha.setDuration(0); // Make animation instant
	     alpha.setFillAfter(true); // Tell it to persist after the animation ends
	     findViewById(R.id.menuButtons).startAnimation(alpha);
	}
	private void updateTrackerModeText()
	{
		((TextView)findViewById(R.id.trackerModeText)).setText("Tracking Mode ("+_mapSettings.getPreferenceTrackerMode()+")");
	}
	
	@Override
	protected void createLayers() {
		// TODO Auto-generated method stub
		if(_mapSettings.getMapMode()==MapSettings.MAP_OFFLINE)
		{
			if(getMapFile().exists()){
				createLayerOffline();
				currentMapMode=MapSettings.MAP_OFFLINE;
			}else
			{
				currentMapMode=MapSettings.MAP_OFFLINE;
				Toast.makeText(getApplicationContext(), "Unable to find map file: "+getMapFile().getName(),
						Toast.LENGTH_LONG).show();
				selectMapFile();
			}
		}else
		{
			createLayerOnline();
			currentMapMode=MapSettings.MAP_ONLINE;
		}
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
		
		if (myLocationOverlay != null)
		{
			myLocationOverlay.enableMyLocation(_centerMe);
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
		if (myLocationOverlay != null)
		{
			myLocationOverlay.disableMyLocation();
		}
		_resyncIntervalTimer.cancel();
	}
	
	@Override
	public void onDestroy()
	{
		if(mp!= null)
		{
			mp.release();
		}
		if (_loadOverlaysTask != null)
		{
			_loadOverlaysTask.cancel(true);
		}
		if(bombLayer!=null)
		{
			bombLayer.unRegisterDangerListener(this);
		}
		if(chemicalLayer!=null)
		{
			chemicalLayer.unRegisterDangerListener(this);
		}
		unregisterContentObserver(_addressObserver);
		unregisterContentObserver(_assetObserver);
		unregisterContentObserver(_incidentObserver);
		unregisterContentObserver(_triageObserver);
		unregisterContentObserver(_drawingObserver);
//		try
//		{
//			int size = _mapView.getOverlays().size();
//			for (int i = 0; i < size; i++)
//			{
//				Object o = _mapView.getOverlays().get(i);
//				if (Closeable.class.isAssignableFrom(o.getClass()))
//				{
//					Closeable closeableOverlay = (Closeable)o;
//					closeableOverlay.close();
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
		iconsCache.getIconsCache().evictAll();
		_syncServiceConnection.disconnect();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
		{
			_mapSettings.setMapFile( data.getStringExtra("mapFile"));
			//MapData.edit().putString(MapFilePath, data.getStringExtra("mapFile")).commit();
			Toast.makeText(getApplicationContext(), "Map file location has been saved.",Toast.LENGTH_SHORT).show();
			mapModeOffline();
		}else
		{
			if(currentMapMode ==MapSettings.MAP_OFFLINE)
			{				
				mapModeOnline();
			}
			Toast.makeText(getApplicationContext(), "Action Cancelled.",Toast.LENGTH_SHORT).show();
		}
	}

	public void goBack(View view)
	{
		finish();
	}
	public void displayMapMode(View view)
	{
		int isChecked;
		if(currentMapMode == MapSettings.MAP_ONLINE){
			isChecked = 1;
		}else{
			isChecked = 0;}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Map Mode");
		builder.setSingleChoiceItems(mapModeOptions,
				isChecked, new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if("Offline".equals(mapModeOptions[which]))
						{
							mapModeOffline();
						}else if("Online".equals(mapModeOptions[which]))
						{
							mapModeOnline();
						}
						dialog.dismiss();
					}
				});
		builder.setPositiveButton("Select Map File", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				selectMapFile();	
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void displayCenterMode(View view)
	{
		int isChecked = 1;
		if(_centerMe)
		{
			isChecked = 0;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Center Mode");
		builder.setSingleChoiceItems(centerModeOptions,
				isChecked, new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Center Me".equals(centerModeOptions[which]))
						{
							tryCenterMeNow();
						}else if("Center Incident".equals(centerModeOptions[which]))
						{
							 myLocationOverlay.setSnapToLocationEnabled(false);
							 centerIncident();
						}
						dialog.dismiss();
					}
				});
		builder.setPositiveButton("Change Tracking Mode", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				displayTrackingOptions();
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
	
	public void displayTrackingOptions()
	{
		int checked = 1;//1 = tracking mode normal
		if(_mapSettings.getPreferenceTrackerMode().contains("Fast"))
		{
			checked = 0;
		}else if(_mapSettings.getPreferenceTrackerMode().contains("Casual"))
		{
			checked = 2;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Tracking Mode");
		builder.setSingleChoiceItems(trackingOptions,checked, new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						_mapSettings.setPreferenceTrackerMode(trackingOptions[which]);
						setTrackingMode();
						_centerMe = true;
						myLocationOverlay.enableMyLocation(_centerMe);
						tryCenterMeNow();
						dialog.dismiss();
						updateTrackerModeText();
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
	
	public void displayGeneralOptions(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(generalOptions,new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Refresh".equals(generalOptions[which]))
						{
							sync();
							_assetObserver.onChange(true);
						}else if("GPS Status".equals(generalOptions[which]))
						{
							Intent intent = new Intent(getApplicationContext(), GpsStatus.class);
							startActivity(intent);
						}else if("Scale Map".equals(generalOptions[which]))
						{
							displayScaleMode();
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
	
	private void displayScaleMode()
	{
		int position = 1;		
		String scaleMode = _mapSettings.getPreferenceMapScale();
		Log.e("asddsa","before change size: "+ scaleMode);
		if(scaleMode.equals("0.7"))
		{
			position=0;
		}else if(scaleMode.equals("1.5"))
		{
			position=2;
		}		
		final String[] a ={"Small","Normal","Large"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Map Scale Mode");
		builder.setSingleChoiceItems(a,position, new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						double value = 0;//0.7, 1.0, 1.5
						if(a[which].equals("Small"))
						{
							value = 0.7;
						}else if(a[which].equals("Normal"))
						{
							value = 1.0;
						}
						else if(a[which].equals("Large"))
						{
							value = 1.5;
						}
						changeDisplayScale((float)value);		
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
	
	private void mapModeOffline()
	{
		if(currentMapMode != MapSettings.MAP_OFFLINE)
		{	
			if (getMapFile().exists()) {
				if(downloadLayer!=null){
					downloadLayer.onPause();
					downloadLayer.onDestroy();
					((LayerManager) this.layerManagers.get(0)).getLayers().remove(
						downloadLayer);
				}
				createLayerOffline();
				currentMapMode = MapSettings.MAP_OFFLINE;
				_mapSettings.setMapMode(MapSettings.MAP_OFFLINE);
				Toast.makeText(getApplicationContext(), "Map Mode Saved", Toast.LENGTH_SHORT).show();
			} else {
				 selectMapFile();
			}
		}else{
			if(tileRendererLayer!=null){
				tileRendererLayer.onDestroy();
				((LayerManager)this.layerManagers.get(0)).getLayers().remove(0);
			}
			createLayerOffline();
		}
	}
	
	private void selectMapFile()
	{
		Intent selectFile = new Intent(getApplicationContext(),SelectFileActivity.class);
		selectFile.putExtra("path",rootLocalDirectoryFolderPathForMaps);
		startActivityForResult(selectFile, SELECT_FILE);
	}
	
	private void mapModeOnline()
	{
		if(currentMapMode != MapSettings.MAP_ONLINE){
			if(tileRendererLayer!=null){
				tileRendererLayer.onDestroy();
				((LayerManager)this.layerManagers.get(0)).getLayers().remove(0);
			}
			createLayerOnline();
			downloadLayer.start();
			currentMapMode = MapSettings.MAP_ONLINE;
			_mapSettings.setMapMode(MapSettings.MAP_ONLINE);
			Toast.makeText(getApplicationContext(), "Map Mode Saved", Toast.LENGTH_SHORT).show();
		}
	}
	
	private final class LoadOverlaysAsyncTask extends AsyncTask<Void, Integer, Exception>
	{
		private ProgressDialog _loadOverlayDataProgressDialog;
		
		@Override
		protected void onPreExecute()
		{
			_loadOverlayDataProgressDialog = ProgressDialog.show(MainMapOSMActivity.this, "Loading", "Loading data...", true, false);
		}
		
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	    	try
	    	{
		    	Context context = MainMapOSMActivity.this;
		    	String incidentID = MercurySettings.getCurrentIncidentId(getApplicationContext());
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
		    		startManagingModel(_tasks = Tactic.queryTasks(context, MercurySettings.getCurrentOperationalPeriodId(getApplicationContext())));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_priorityTasks = Tactic.queryPriorityTasks(context, MercurySettings.getCurrentIncidentId(context)));
		    	}
		    	if (isCancelled() == false)
		    	{
		    		startManagingModel(_triage = Triage.queryIncident(context, MercurySettings.getCurrentIncidentId(getApplicationContext())));
		    	}
		    	if(isCancelled()==false)
		    	{
		    		startManagingModel(_drawings =Drawing.queryDrawings(getApplicationContext()) );
		    	}
		    	if(isCancelled()==false)
		    	{
		    		startManagingModel(_collection =Collection.query(context, Collection.CONTENT_URI));
		    	}
		    	if(isCancelled()==false)
		    	{
		    		startManagingModel(_notes =Address.queryWhere(context, Address.CONTENT_URI, Address.TYPE+"='"+AddressType.NOTE+"'"));
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
	    		addMyLocationOverlay();
	    		addDrawings();
	    	 	addIncidentOverlay();
    			addEquipmentOverlay();
    			addPersonnelOverlay();
    			addTaskOverlay();
    			addPriorityTaskOverlay();
    			addTriageOverlay();
    			addCollectionsOverlay();
    			addNotesOverlay();
    			centerMapOnSomething(getIntent());
    			registerContentObservers();    			
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
			if (intent.getData() != null)
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
				myLocationOverlay.setSnapToLocationEnabled(_centerMe);//_centerMe = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/* package private */

	void centerOnPoint(LatLong point)
	{
		if (point != null)
		{
			((MapView)this.mapViews.get(0)).getModel().mapViewPosition.setZoomLevel((byte)14);
			((MapView)this.mapViews.get(0)).getModel().mapViewPosition.animateTo(point);
			_centerMe = false;
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
				LatLong point = getGeoPointFromWKT(cursor);
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
	
	private static LatLong getGeoPointFromWKT(Address address)
	{
		LatLong result = null;
			if(address.getWKT().contains("POINT"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POINT", "").split(" ");
				result = new LatLong(Double.parseDouble(g[1]), Double.parseDouble(g[0]));
			}else if(address.getWKT().contains("POLYGON"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POLYGON", "").split(",");
				String [] f = g[0].split(" ");
				result = new LatLong(Double.parseDouble(f[1]), Double.parseDouble(f[0]));
			}
		return result;
	}
	
	private void registerContentObservers()
	{
		_addressObserver = new AddressObserver();
		_assetObserver = new AssetObserver();
		_incidentObserver = new IncidentObserver();
		_triageObserver = new TriageObserver();
		_drawingObserver = new DrawingObserver();
		
		registerContentObserver(Address.CONTENT_URI, true, _addressObserver);
		registerContentObserver(Asset.CONTENT_URI, true, _assetObserver);
		registerContentObserver(Incident.CONTENT_URI, true, _incidentObserver);
		registerContentObserver(Triage.CONTENT_URI, true, _triageObserver);
		registerContentObserver(Drawing.CONTENT_URI, true, _drawingObserver);
	}
	
	
	private void sync()
	{
		if (_syncServiceConnection != null && _syncServiceConnection.isConnected())
		{
			_syncServiceConnection.syncDataset(MapViewDataSet.class.getName());
		}
	}
	
	private void addDrawings()
	{
		if(_drawings != null)
			DrawingsLayer.displayDrawings(getApplicationContext(), _drawings,((LayerManager)this.layerManagers.get(0)).getLayers());
	}
	
	public Layers getLayers()
	{
		return ((LayerManager)this.layerManagers.get(0)).getLayers();
	}
	
	public MapView getMapViewFromMapViews()
	{
		return ((MapView)this.mapViews.get(0));
	}
	
	private void addIncidentOverlay()
	{
		Log.d("OSM", "addIncidentOverlay");
		try
		{
			if (_incident != null)
			{
				IncidentOverlay incidentOverlay = new IncidentOverlay();
				IncidentOverlayMarker item = incidentOverlay.creteIncidentItem(_incident,getApplicationContext());				
				String incidentDetalis = _incident.getString(_incident.getColumnIndex(Incident.DETAILS));
				if(incidentDetalis!=null && incidentDetalis.contains("Bomb")){
					bombLayer = new BombLayer();
					bombLayer.registerDangerListener(this);
					myLocationOverlay.registerListener(bombLayer);
					bombLayer.displayBombLayer(getApplicationContext(), incidentDetalis, getLayers(),
							incidentOverlay.getIncidentLocation(), getMapViewFromMapViews());
				}else if(incidentDetalis!=null && incidentDetalis.contains("Chemical"))
				{
				  chemicalLayer = new ChemicalSpillLayer();
				  chemicalLayer.registerDangerListener(this);
				  myLocationOverlay.registerListener(chemicalLayer);
				  chemicalLayer.displayChemicalSpillLayer(getApplicationContext(),incidentDetalis, getLayers(),
						  incidentOverlay.getIncidentLocation(),getMapViewFromMapViews());
				}
				getLayers().add(item);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addEquipmentOverlay()
	{
		Log.d("OSM", "addEquipmentOverlay");
		try
		{
			if (_equipment != null)
			{
				EquipmentOverlay.createEquipmentItems(getApplicationContext(),
						((LayerManager)this.layerManagers.get(0)).getLayers(),
						_equipment,iconsCache);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addPersonnelOverlay()
	{
		Log.d("OSM", "addPersonnelOverlay");
		try
		{
			if (_personnel != null)
			{
				PersonnelOverlay.createPersonnelItems(getApplicationContext(),
						((LayerManager)this.layerManagers.get(0)).getLayers(),
						_personnel,iconsCache);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addTaskOverlay()
	{
		Log.d("OSM", "addTaskOverlay");
		try
		{
			if (_tasks != null)
			{
				TasksOverlay.createTaskItems(MainMapOSMActivity.this,
						((LayerManager)this.layerManagers.get(0)).getLayers(),
						_tasks);
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
				TasksOverlay.createTaskItems(MainMapOSMActivity.this, ((LayerManager)this.layerManagers.get(0)).getLayers(), _priorityTasks);
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
				TriageOverlay.createTriageItems(getApplicationContext(), 
						((LayerManager)this.layerManagers.get(0)).getLayers(),
						_triage,
						iconsCache);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addCollectionsOverlay()
	{
		try
		{
//			if (_collection != null)
//			{
//				CollectiveOverlay collectionOverlay = new CollectiveOverlay();
//				collectionOverlay.createCollectiveItems(getApplicationContext(), 
//						((LayerManager)this.layerManagers.get(0)).getLayers(),_collection);
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addNotesOverlay()
	{
		//NotesLayer notesLayer = new NotesLayer(iconsCache, this);
		//notesLayer.displayAllNotes(_notes, iconsCache, this,getLayers());

//		try
//		{
//			if (_collection != null)
//			{
//				CollectiveOverlay.createCollectiveItems(getApplicationContext(), 
//						((LayerManager)this.layerManagers.get(0)).getLayers(),_collection);
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}
	
	
//	@Override
//	public void onSaveInstanceState(Bundle outState)
//	{
//		try
//		{		
//			outState.putBoolean(SAVED_CENTER_ME, _centerMe);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState)
//	{
//		_centerMe = savedInstanceState.getBoolean(SAVED_CENTER_ME);
//		if (center != null)
//		{
//			animateTo(center);
//		}
//	}
//	

	private void addMyLocationOverlay()
	{		
	    org.mapsforge.core.graphics.Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(
	    		getResources().getDrawable(R.drawable.placemarkblue));
	    this.myLocationOverlay = new MyLocationOverlay(this, (MapViewPosition)this.mapViewPositions.get(0),
	    		bitmap);
	    ((LayerManager)this.layerManagers.get(0)).getLayers().add(this.myLocationOverlay);
	    setTrackingMode();
	    this.myLocationOverlay.enableMyLocation(_centerMe);
	    this.myLocationOverlay.registerListener(this);
	}
	
	private void setTrackingMode()
	{
		//"Fast: 10sec/10m","Normal: 1min/50m","Casual: 15min/500m"};
		if(_mapSettings.getPreferenceTrackerMode().contains("Fast"))
		{
			this.myLocationOverlay.setUpdateDistanceAndInterval(10,(10*1000));
			Toast.makeText(getApplicationContext(), "Tracker Mode: Fast", Toast.LENGTH_SHORT).show();
		}else if(_mapSettings.getPreferenceTrackerMode().contains("Normal"))
		{
			this.myLocationOverlay.setUpdateDistanceAndInterval(50,(60*1000));
			Toast.makeText(getApplicationContext(), "Tracker Mode: Normal", Toast.LENGTH_SHORT).show();
		}else
		{
			this.myLocationOverlay.setUpdateDistanceAndInterval(100,(15*60*1000));
			Toast.makeText(getApplicationContext(), "Tracker Mode: Casual", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void tryCenterMeNow()
	{
		if (myLocationOverlay != null)
		{
			_centerMe = true;
			myLocationOverlay.setSnapToLocationEnabled(true);
			if(myLocationOverlay.getLastLocation()!=null){
				LatLong myPoint = MyLocationOverlay.locationToLatLong(myLocationOverlay.getLastLocation());
				if (myPoint != null)
				{
					if(((MapView)this.mapViews.get(0)).getModel().mapViewPosition.getZoomLevel()>=(byte)14){
						((MapView)this.mapViews.get(0)).getModel().mapViewPosition.setZoomLevel((byte)14);
					}
					((MapView)this.mapViews.get(0)).getModel().mapViewPosition.animateTo(myPoint);
				}
			}else
			{
				CustomAlertDialog customDialog = new CustomAlertDialog(this, new String[] {"OK"},
				  "Information","Getting your current location; please wait. Map will be centered when we obtain your location.",android.R.drawable.ic_menu_info_details,
					 new CustomAlertDialog.DialogButtonClickListener() {
						@Override
						public void onDialogButtonClick(View v) {
							// TODO Auto-generated method stub					
						}
					});
				customDialog.show();
			}
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
					LatLong point = getGeoPointFromWKT(address);
					if (point != null)
					{
						centerOnPoint(point);
					}else
					{
						Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
					}
				}else
				{
					Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onSyncServiceConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncServiceDisconnected() {
		// TODO Auto-generated method stub
		
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
	
	private final class IncidentObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable() {
			@Override
			public void run() {
				try
				{
					if (_incident != null && _incident.isClosed() == false)
					{
						_incident.requery();
						for (Layer layer:((LayerManager)layerManagers.get(0)).getLayers())
						{
							if (layer instanceof IncidentOverlay.IncidentOverlayMarker)
							{
								((IncidentOverlay.IncidentOverlayMarker)layer).setLatLong(IncidentOverlay.getGeoPointFromWKT(_incident.getAddress(getApplicationContext())));
								layer.requestRedraw();
							}
						}
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
				if(_drawingObserver != null)
				{
					_drawingObserver.onChange(true);
				}
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
					if (_equipment != null)
					{
						stopManagingCursor(_equipment);
						_equipment.close();
						_equipment = EquipmentViewCursorByCheckIn.query(MainMapOSMActivity.this);
						startManagingCursor(_equipment);
						for (Layer layer:((LayerManager)layerManagers.get(0)).getLayers())
						{
							if (layer instanceof EquipmentOverlay)
							{
								((LayerManager)layerManagers.get(0)).getLayers().remove(layer);
							}
						}
						addEquipmentOverlay();
					}
					if (_personnel != null)
					{
						stopManagingCursor(_personnel);
						_personnel.close();
						_personnel = PersonnelViewCursorByCheckIn.query(MainMapOSMActivity.this);
						startManagingCursor(_personnel);
						for (Layer layer:((LayerManager)layerManagers.get(0)).getLayers())
						{
							if (layer instanceof PersonnelOverlay)
							{
								((LayerManager)layerManagers.get(0)).getLayers().remove(layer);
							}
						}
						addPersonnelOverlay();
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
					if (_triage != null)
					{
						_triage.requery();
						for (Layer layer:((LayerManager)layerManagers.get(0)).getLayers())
						{
							if (layer instanceof TriageOverlay)
							{
								((LayerManager)layerManagers.get(0)).getLayers().remove(layer);
							}
						}
						addTriageOverlay();
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
	
	private final class DrawingObserver extends ContentObserver
	{
		private Runnable _deferredRunnable = new Runnable()
		{
			@Override
			public void run() {
				try
				{
					if (_drawings != null)
					{
						_drawings.requery();
						for (Layer layer:((LayerManager)layerManagers.get(0)).getLayers())
						{
							if (layer instanceof Polygon || layer instanceof Polyline)
							{
								((LayerManager)layerManagers.get(0)).getLayers().remove(layer);
							}
						}
						addDrawings();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		public DrawingObserver()
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

	@Override
	public void locationHasChange(final boolean snapToLocationEnabled, Location location) {
		// TODO Auto-generated method stub
		Handler mHandler = new Handler(getMainLooper());
	    mHandler.post(new Runnable() {
	        @Override
	        public void run() {
	        	Toast.makeText(getApplicationContext(),"Location Acquired", Toast.LENGTH_SHORT).show();
	        	searchingLocationIcon.clearAnimation();
	        	myLocationOverlay.unRegisterListener(MainMapOSMActivity.this);
	        }
	    });	
	}

	@Override
	public void locationIsInDangerZone(final boolean isDangerZone) {
		// TODO Auto-generated method stub
		Handler mHandler = new Handler(getMainLooper());
	    mHandler.post(new Runnable() {
	        @Override
	        public void run() {
	        	if(isDangerZone){
	        		((View)findViewById(R.id.danger_zone_text)).setVisibility(View.VISIBLE);
	        		Toast.makeText(getApplicationContext(),"You are in a Danger Zone!!!!", Toast.LENGTH_LONG).show();	  
//	        		Vibrator v = (Vibrator) MainMapOSMActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
//	        		// Vibrate for 500 milliseconds
//	        		v.vibrate(500);
//	        		v.cancel();
	        		if(mp!=null && !mp.isPlaying())
	        	 		mp.start();
	        	}else
	        	{
	        		((View)findViewById(R.id.danger_zone_text)).setVisibility(View.GONE);
	        	}
	        }
	    });	
	}	
	
}
