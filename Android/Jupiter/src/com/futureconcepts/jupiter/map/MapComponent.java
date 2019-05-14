package com.futureconcepts.jupiter.map;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
//import android.util.Log;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.Config;
import com.futureconcepts.jupiter.MainMapActivity;
import com.futureconcepts.jupiter.layers.CoordinatesLayer;
import com.futureconcepts.jupiter.layers.ScaleBarLayer;
import com.futureconcepts.jupiter.layers.TopographyLayer;
import com.futureconcepts.jupiter.layers.ZoomLayer;
import com.futureconcepts.jupiter.map.LayerStyle;

import com.osa.android.droyd.map.DroydMapComponent;
import com.osa.android.droyd.map.Point;

public class MapComponent implements Closeable
{
//	private static final String TAG = "MapComponent";
	public static final int TOUCH_CONTROL_DRAG_TO_MOVE = 1;
	public static final int TOUCH_CONTROL_RUBBERBAND = 2;
	public static final int TOUCH_CONTROL_NONE = 3;
	private DroydMapComponent _droydMapComponent;
	private MapView _mapView;
	private Config _config;
	private Hashtable<String, Layer> _layers = new Hashtable<String, Layer>();
	private Handler _handler = new Handler();
	private MapComponent.Listener _listener;
	private ArrayList<Feature> _features = new ArrayList<Feature>();
	
	public MapComponent(MainMapActivity context, MapView mapView)
	{
		_mapView = mapView;
		_config = Config.getInstance(context);
		_droydMapComponent = new DroydMapComponent(context);
		_droydMapComponent.setTheme("osm-outdoor");
		_droydMapComponent.enableLayer("TopLoadStatus", false);
		if (_config.getDistanceFormat() == Config.DISTANCE_FORMAT_FEET)
		{
			_droydMapComponent.setDistanceUnit(DroydMapComponent.DISTANCE_UNIT_IMPERIAL);
		}
		else
		{
			_droydMapComponent.setDistanceUnit(DroydMapComponent.DISTANCE_UNIT_METRIC);
		}
	    _droydMapComponent.setMapUpdatePolicy(DroydMapComponent.MAP_UPDATE_ANIMATED);
	    _droydMapComponent.setAnimationTime(250);
	    _droydMapComponent.setTouchControl(DroydMapComponent.TOUCH_CONTROL_DRAG_TO_MOVE);
	}

	@Override
	public void close() throws IOException
	{
		Enumeration<Layer> layers = _layers.elements();
		while (layers.hasMoreElements())
		{
			Layer layer = layers.nextElement();
			if (layer.isLoaded())
			{
				layer.close();
			}
		}
		if (_droydMapComponent != null)
		{
			_droydMapComponent.dispose();
			_droydMapComponent = null;
		}
	    if (_mapView != null)
	    {
	    	_mapView.setOnTouchListener(null);
	    }
	}

	public DroydMapComponent getDroydMapComponent()
	{
		return _droydMapComponent;
	}
	
	public void loadMaps()
	{
	    new MapLoaderTask().execute("load");
	}
	
	public void registerListener(MapComponent.Listener listener)
	{
		_listener = listener;
	}
	
	public void setTouchControl(int mode)
	{
		if (mode == TOUCH_CONTROL_DRAG_TO_MOVE)
		{
			_droydMapComponent.setTouchControl(DroydMapComponent.TOUCH_CONTROL_DRAG_TO_MOVE);
		}
		else if (mode == TOUCH_CONTROL_RUBBERBAND)
		{
			_droydMapComponent.setTouchControl(DroydMapComponent.TOUCH_CONTROL_RUBBERBAND);
		}
		else if (mode == TOUCH_CONTROL_NONE)
		{
			_droydMapComponent.setTouchControl(DroydMapComponent.TOUCH_CONTROL_NONE);
		}
	}
	
	public void attachLayer(final Layer layer)
	{
		String id = layer.getId();
		if (_config.isLayerEnabledById(id))
		{
			if (layer.isLoaded() == false)
			{
				layer.load();
			}
			if (layer.isEnabled() == false)
			{
				layer.setEnabled(true);
			}
		}
		_layers.put(id, layer);
	}
	
	public void attachLayer(String id)
	{
		if (id.equals(TopographyLayer.ID))
		{
			attachLayer(new TopographyLayer(this));
		}
		else if (id.equals(ScaleBarLayer.ID))
		{
			attachLayer(new ScaleBarLayer(this));
		}
		else if (id.equals(ZoomLayer.ID))
		{
			attachLayer(new ZoomLayer(this));
		}
		else if (id.equals(CoordinatesLayer.ID))
		{
			attachLayer(new CoordinatesLayer(this));
		}
	}
	
	public boolean hasLayerById(String id)
	{
		return _layers.containsKey(id);
	}
	
	public Enumeration<Layer> getLayers()
	{
		return _layers.elements();
	}
	
	public List<String> getLayerIds()
	{
		if (_droydMapComponent != null)
		{
			return Collections.list(_layers.keys());
		}
		else
		{
			return null;
		}
	}
	
	public Layer findLayerById(String id)
	{
		return _layers.get(id);
	}
		
	public void setMapView(MapView mapView)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setMapView(mapView);
		}
		_mapView = mapView;
	}
	
	public void zoomBy(double factor)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.zoomBy(factor);
		}
	}
	
	public double getScale()
	{
		if (_droydMapComponent != null)
		{
			return _droydMapComponent.getScale();
		}
		else 
		{
			return 0.0d;
		}
	}

	public void setScale(double scale)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setScale(scale);
		}
	}
	
	public double getElevation(Point2D.Double point)
	{
		if (_droydMapComponent != null)
		{
			return _droydMapComponent.getElevation(point.x, point.y);
		}
		else
		{
			return 0;
		}
	}
	
	public Point2D.Double getPoint()
	{
		Point2D.Double value = new Point2D.Double();
		if (_droydMapComponent.getLongitude() != 0.0d)
		{
			value.x = _droydMapComponent.getLongitude();
			value.y = _droydMapComponent.getLatitude();
		}
		return value;
	}
	
	public void setPosition(Location location)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setPosition(location.getLongitude(), location.getLatitude());
		}
	}
	
	public void setPosition(Point2D.Double value)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setPosition(value.x, value.y);
		}
	}
	
	public void setPositionScale(Location location, double scale)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setPositionScale(location.getLongitude(), location.getLatitude(), scale);
		}
	}

	public void setPositionScale(Point2D.Double point, double scale)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.setPositionScale(point.x, point.y, scale);
		}
	}
		
	public Feature getFeatureAtPixel(Point2D.Double point, double tolerance)
	{
		Feature result = null;
		if (_droydMapComponent != null)
		{
			com.osa.android.droyd.map.Feature droydFeature = null;
			droydFeature = _droydMapComponent.getFeatureAtPixel((float)point.x, (float)point.y, tolerance);
			for (Feature feature : _features)
			{
				if (feature._droydFeature == droydFeature)
				{
					result = feature;
					break;
				}
			}
			
		}
		return result;
	}
	
	public void showOverview()
	{
		if (_droydMapComponent != null)
		{
        	_droydMapComponent.showOverview();
        }
    }
	
	public void repaint()
    {
		if (_droydMapComponent != null)
		{
			_droydMapComponent.repaint();
		}
    }

	public void refreshViews()
	{
		Enumeration<Layer> elements = _layers.elements();
		while (elements.hasMoreElements())
		{
			Layer layer = elements.nextElement();
			boolean isEnabled = _config.isLayerEnabledById(layer.getId());
			layer.setEnabled(isEnabled);
		}
	}
	
	public void addLayerById(String id, Layer layer, LayerStyle style)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.addLayer(id, style._droydLayerStyle);
		}
		if (layer != null)
		{
			_layers.put(id, layer);
		}
	}
	
	public void removeLayerById(String id)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.removeLayer(id);
		}
		if (_layers.containsKey(id))
		{
			_layers.remove(id);
		}
	}

	public void addFeature(String layerId, Feature feature)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.addFeature(layerId, feature._droydFeature);
			_features.add(feature);
		}
	}
	
	public void removeFeature(Feature feature)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.removeFeature(feature._droydFeature);
			_features.remove(feature);
		}
	}
	
	public void enableLayer(String layerId, boolean value)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.enableLayer(layerId, value);
		}
	}
	
	public boolean screenToMap(Point2D.Double point)
	{
		boolean result = false;
		if (_droydMapComponent != null)
		{
			Point point2 = new Point(point.x, point.y);
			result = _droydMapComponent.screenToMap(point2);
			if (result)
			{
				point.x = point2.x;
				point.y = point2.y;
				result = true;
			}
		}
		return result;
	}
	
	public void enableLayers(String layerName, boolean value)
	{
		if (_droydMapComponent != null)
		{
			_droydMapComponent.enableLayers(layerName, value);
		}
	}

	private class MapLoaderTask extends AsyncTask<String, Integer, Integer>
	{
		@Override
		protected void onPreExecute()
		{
			_droydMapComponent.setMapView(_mapView);
			if (_listener != null)
			{
				_listener.onLoadBegin();
			}
		}

		@Override
		protected Integer doInBackground(String... params)
		{
			int mapFileCount = 0;
		    String extDirName = Environment.getExternalStorageDirectory().getAbsolutePath();
		    File folder = new File(extDirName, "FutureConcepts/.maps/");
		    File[] files = folder.listFiles();
		    if (files != null)
		    {
			    if (files.length > 0)
			    {
			    	for (int i = 0; i < files.length; i++)
			    	{
			    		File file = files[i];
			    		String path = file.getPath();
			    		if (path.contains(".ebmd") || path.contains(".smd"))
			    		{
			    			mapFileCount++;
			    		}
			    	}
			    }
			    if (mapFileCount > 0)
			    {
			    	_droydMapComponent.setDataDirectory(folder, 5*1024*1024);
			    }
		    }
		    return mapFileCount;
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
			if (_listener != null)
			{
				if (result > 0)
				{
					
					_listener.onLoadComplete(result);
				}
				else
				{
					_listener.onLoadFail();
				}
			}
		}
	}

	public interface Listener
	{
	    void onLoadBegin();
		void onLoadComplete(int mapFilesCount);
	    void onLoadFail();
	}
	
	public class Layer implements Closeable
	{
		protected String _id;
		protected boolean _isLoaded = false;
		protected boolean _isEnabled = false;
		
		public String getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return "Default";
		}
		
		public MapComponent getMapComponent()
		{
			return MapComponent.this;
		}
		
		public int getIconId()
		{
			return android.R.drawable.btn_star;
		}
		
		public boolean loadInBackgroundThread()
		{
			return false;
		}
		
		public boolean isLoaded()
		{
			return _isLoaded;
		}
		
		public void load()
		{
			_isLoaded = true;
		}
		
		public void setEnabled(boolean value)
		{
			MapComponent.this.enableLayer(getId(), value);
			_isEnabled = value;
		}
		
		public boolean isEnabled()
		{
			return _isEnabled;
		}
		
		@Override
		public void close() throws IOException
		{
		}
		
		public void runOnUiThread(Runnable runnable)
		{
			_handler.post(runnable);
		}
		
	    public void postAddLayer(final LayerStyle style)
	    {
	    	runOnUiThread(new Runnable()
	    	{
				@Override
				public void run()
				{
					addLayerById(_id, Layer.this, style);
				}
	    	});
	    }

	    public void postAddLayer(final String id, final LayerStyle style)
	    {
	    	runOnUiThread(new Runnable()
	    	{
				@Override
				public void run()
				{
					addLayerById(id, Layer.this, style);
				}
	    	});
	    }
	    
	    public void postAddFeature(final String key, final Feature feature)
	    {
	    	runOnUiThread(new Runnable()
	    	{
				@Override
				public void run()
				{
					addFeature(key, feature);
				}
	    	});
	    }

	    public void postRemoveLayer()
	    {
	    	runOnUiThread(new Runnable()
	    	{
				@Override
				public void run()
				{
					removeLayerById(_id);
				}
	    	});
	    }
	    
	    public void removeLayer()
	    {
	    	removeLayerById(_id);
	    }
	}
}
