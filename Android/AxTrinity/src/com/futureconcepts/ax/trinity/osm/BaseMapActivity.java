package com.futureconcepts.ax.trinity.osm;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
//import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenCycleMap;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
//import org.mapsforge.map.scalebar.Imperial;

import com.futureconcepts.ax.trinity.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class BaseMapActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener{

	  protected static final int DIALOG_ENTER_COORDINATES = 2923878;
	  protected ArrayList<LayerManager> layerManagers = new ArrayList<LayerManager>();
	  protected ArrayList<MapViewPosition> mapViewPositions = new ArrayList<MapViewPosition>();
	  protected ArrayList<MapView> mapViews = new ArrayList<MapView>();
	  protected TileCache tileCache;
	  protected LatLong californiaPointCenter = new LatLong(36.0770001602737,-119.027105695653);
	  protected TileDownloadLayer downloadLayer;
	  protected TileRendererLayer tileRendererLayer;
	  
	  protected PreferencesFacade preferencesFacade;
	  protected SharedPreferences sharedPreferences;
	  protected MapSettings _mapSettings;
	  protected void createControls() {}
	  
	  protected void createLayerManagers()
	  {
	    for (MapView mapView : this.mapViews) {
	      this.layerManagers.add(mapView.getLayerManager());
	    }
	  }
	  
	  public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
	  {
//	    if ("scale".equals(key))
//	    {
//	      destroyTileCaches();
//	      for (MapView mapView : this.mapViews) {
//	        mapView.getModel().displayModel.setUserScaleFactor(DisplayModel.getDefaultUserScaleFactor());
//	      }
//	      Log.d("BaseMapActivity", "Tilesize now " + ((MapView)this.mapViews.get(0)).getModel().displayModel.getTileSize());
//	      createTileCaches();
//	      redrawLayers();
//	    }
	  }
	  
	  public void changeDisplayScale(float fs)
	  {
		_mapSettings.setPreferenceMapScale(""+fs);
		DisplayModel.setDefaultUserScaleFactor(fs);
	     destroyTileCaches();
	     for (MapView mapView : this.mapViews) {
	        mapView.getModel().displayModel.setUserScaleFactor(DisplayModel.getDefaultUserScaleFactor());
	     }
	     Log.d("BaseMapActivity", "Tilesize now " + ((MapView)this.mapViews.get(0)).getModel().displayModel.getTileSize());
	     createTileCaches();
	     redrawLayers();
	  }
	  
//	  float userScaleFactor = DisplayModel.getDefaultUserScaleFactor();
//		float fs = Float.valueOf(preferences.getString(
//				SamplesApplication.SETTING_SCALE,
//				Float.toString(userScaleFactor)));
//		Log.e(SamplesApplication.TAG,
//				"User ScaleFactor " + Float.toString(fs));
//		if (fs != userScaleFactor) {
//			DisplayModel.setDefaultUserScaleFactor(fs);
//		}
	  
	  
	  protected void createSharedPreferences()
	  {
	    SharedPreferences sp = getSharedPreferences(getPersistableId(), 0);
	    this.preferencesFacade = new AndroidPreferences(sp);
	  }
	  
	  protected void createLayers()
	  {
		  createLayerOnline();
	  }
	  protected void createLayerOffline()
	  {
		   tileRendererLayer = Utils.createTileRendererLayer(this.tileCache, (MapViewPosition)this.mapViewPositions.get(0), getMapFile(), getRenderTheme(), false);
		  ((LayerManager)this.layerManagers.get(0)).getLayers().add(0,tileRendererLayer);
	  }
	  
	  protected void createLayerOnline()
	  {
	   // TileRendererLayer tileRendererLayer = Utils.createTileRendererLayer(this.tileCache, (MapViewPosition)this.mapViewPositions.get(0), getMapFile(), getRenderTheme(), false);
		  //Also Can BE OpenCycleMap.INSTANCE;
		  this.downloadLayer = new TileDownloadLayer(this.tileCache, (MapViewPosition)this.mapViewPositions.get(0), OpenStreetMapMapnik.INSTANCE, AndroidGraphicFactory.INSTANCE);
	    ((LayerManager)this.layerManagers.get(0)).getLayers().add(0,downloadLayer);
	  }
	  	  
	  protected void createMapViewPositions()
	  {
	    for (MapView mapView : this.mapViews) {
	      this.mapViewPositions.add(initializePosition(mapView.getModel().mapViewPosition));
	    }
	  }
	  protected void createMapViews()
	  {
		MapView mapView = getMapView();
	    mapView.getModel().init(this.preferencesFacade);
	    mapView.setClickable(true);
	    mapView.getMapScaleBar().setVisible(true);
	    mapView.setBuiltInZoomControls(hasZoomControls());
	    mapView.getMapZoomControls().setZoomLevelMin((byte)1);
	    mapView.getMapZoomControls().setZoomLevelMax((byte)18);
	    mapView.getMapZoomControls().displayZoomControls();
	  //  mapView.getModel().displayModel.setFixedTileSize(Integer.valueOf(260));
	    this.mapViews.add(mapView);
	  }
	  
	  protected void createTileCaches()
	  {
	    this.tileCache = AndroidUtil.createTileCache(this, getPersistableId(), ((MapView)this.mapViews.get(0)).getModel().displayModel.getTileSize(), getScreenRatio(), ((MapView)this.mapViews.get(0)).getModel().frameBufferModel.getOverdrawFactor());
	  }
	  
	  protected void destroyLayers()
	  {		  
		  LayerManager layerManager;
	    for (Iterator<LayerManager> i$ = this.layerManagers.iterator(); i$.hasNext();)
	    {
	      layerManager = (LayerManager)i$.next();
	      for (Layer layer : layerManager.getLayers())
	      {
	        layerManager.getLayers().remove(layer);
	        layer.onDestroy();
	      }
	    }	    
	  }
	  
	  protected void destroyMapViewPositions()
	  {
	    for (MapViewPosition mapViewPosition : this.mapViewPositions) {
	      mapViewPosition.destroy();
	    }
	  }
	  
	  protected void destroyMapViews()
	  {
	    for (MapView mapView : this.mapViews) {
	      mapView.destroy();
	    }
	  }
	  
	  protected void destroyTileCaches()
	  {
	    this.tileCache.destroy();
	  }
	  
	  protected MapPosition getInitialPosition()
	  {
//		  //_mapSettings.getMapMode()==MapSettings.MAP_OFFLINE
//	    MapDatabase mapDatabase = new MapDatabase();
//	    FileOpenResult result = mapDatabase.openFile(getMapFile());
//	    if (result.isSuccess())
//	    {
//	      MapFileInfo mapFileInfo = mapDatabase.getMapFileInfo();
//	      if ((mapFileInfo != null) && (mapFileInfo.startPosition != null)) {
//	        return new MapPosition(mapFileInfo.startPosition, mapFileInfo.startZoomLevel.byteValue());
//	      }
//	      return new MapPosition(californiaPointCenter, (byte)4);
//	    }else
//	    {
	    	return new MapPosition(californiaPointCenter, (byte)4);
//	    }
	  //  throw new IllegalArgumentException("Invalid Map File " + getMapFileName());
	  }
	  
	  protected File getMapFile()
	  {	
	   // File file = new File(Environment.getExternalStorageDirectory()+ File.separator+Environment.DIRECTORY_DOWNLOADS, getMapFileName());
	    File file = new File(this._mapSettings.getMapFile());//.getString(MapFilePath, "null"));
		  Log.i("BaseMapActivity", "Map file is " + file.getAbsolutePath());
	    return file;
	  }
	  
	  private String getMapFileName()
	  {
	    return "california.map";
	  }
	  
	  protected MapView getMapView()
	  {
	   // MapView mv = new MapView(this);
	    setContentView(R.layout.geo_map_osm_main); 
	    return (MapView)findViewById(R.id.mapView);
	  }
	  
	  protected String getPersistableId()
	  {
	    return getClass().getSimpleName();
	  }
	  
	  protected XmlRenderTheme getRenderTheme()
	  {
	    return InternalRenderTheme.OSMARENDER;
	  }
	  
	  protected float getScreenRatio()
	  {
	    return 1.0F;
	  }
	  
	  protected boolean hasZoomControls()
	  {
	    return true;
	  }
	  
	  protected MapViewPosition initializePosition(MapViewPosition mvp)
	  {
	    LatLong center = mvp.getCenter();
	    if (center.equals(new LatLong(0.0D, 0.0D))) {
	     // mvp.setMapPosition(getInitialPosition());
	    	mvp.setMapPosition(getInitialPosition());
	    }
	    mvp.setZoomLevelMax((byte)18);
	    mvp.setZoomLevelMin((byte)3);
	    return mvp;
	  }
	  
	  protected void onCreate(Bundle savedInstanceState)
	  {
	    super.onCreate(savedInstanceState);
	    
	    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	   // this.MapData = getSharedPreferences(MapFilePath, Context.MODE_PRIVATE);
	    this._mapSettings = MapSettings.getInstance(getApplicationContext());
	    createSharedPreferences();
	    createMapViews();
	    createMapViewPositions();
	    createLayerManagers();
	    createTileCaches();
	    createControls();
	    createLayers();
	  }
	   
	  protected void onResume()
	  {
		  super.onResume();
		  if(_mapSettings.getMapMode()==MapSettings.MAP_ONLINE)
			  this.downloadLayer.onResume();
	  }
	  
	  protected void onDestroy()
	  {
	    super.onDestroy();
	    destroyTileCaches();
	    destroyMapViewPositions();
	    destroyMapViews();
	    this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	    AndroidResourceBitmap.clearResourceBitmaps();
	  }
	  
	  protected void onPause()
	  {
	    super.onPause();
	    for (MapView mapView : this.mapViews) {
	      mapView.getModel().save(this.preferencesFacade);
	    }
	    this.preferencesFacade.save();
	    if(_mapSettings.getMapMode()==MapSettings.MAP_ONLINE)
	    	this.downloadLayer.onPause();
	  }

	  protected void onStart()
	  {
	    super.onStart();
	  //  createLayers();
	  }
	  
	  protected void onStop()
	  {
	    super.onStop();
	    if(isFinishing()){
	     destroyLayers();
	    }
	  }
	  
	  protected void redrawLayers()
	  {
	    for (LayerManager layerManager : this.layerManagers) {
	      layerManager.redrawLayers();
	    }
	  }
	  
	  protected void setContentView()
	  {
	    setContentView((View)this.mapViews.get(0));
	  }
}
