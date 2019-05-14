package com.futureconcepts.jupiter.layers;

import java.util.ArrayList;
import java.util.UUID;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Track;
import com.futureconcepts.jupiter.map.MapComponent;
import com.futureconcepts.jupiter.map.Feature;
import com.futureconcepts.jupiter.map.LineFeature;
import com.futureconcepts.jupiter.map.PointFeature;

public class FolderLayer extends MapComponent.Layer
{
	private static final String TAG = "FolderLayer";
	private Uri _data;
	private Folder _folder;
	private Placemark _placemark;
	private String _tracksLayerId;
	private String _folderName;
	
	private Context _context;
	
	private ContentObserver _myObserver;
	
	private StyleManager _styleManager;
	
	private ArrayList<Feature> _features;
	
	public FolderLayer(Context context, MapComponent component, Uri uri)
	{
		component.super();
		_context = context;
		_styleManager = StyleManager.instance(context);
		_features = new ArrayList<Feature>();
		_data = uri;
		_folder = new Folder(context.getContentResolver().query(_data, null, null, null, null));
		if ((_folder != null) && (_folder.getCount() == 1))
		{
			_folder.moveToFirst();
			_id = _folder.getId();
			_folderName = _folder.getName();
		}
		else
		{
			throw new IllegalArgumentException("uri");
		}
		_myObserver = new MyObserver(new Handler());
		context.getContentResolver().registerContentObserver(uri, false, _myObserver);
		Log.d(TAG, "registerContentObserver for " + uri.toString());
	}
	
	@Override
	public String getName()
	{
		if (_folderName != null)
		{
			return _folderName;
		}
		else
		{
			return "?Folder?";
		}
	}
	
	@Override
	public void load()
	{
		ContentResolver resolver = _context.getContentResolver();
		_placemark = new Placemark(resolver.query(Placemark.CONTENT_URI, null, Placemark.PARENT_ID + "='" + _folder.getId() + "'", null, Placemark.TIME + " ASC"));
		if (_placemark != null)
		{
			if (_placemark.getCount() > 0)
			{
				_placemark.moveToPosition(0);
				Point2D.Double point = new Point2D.Double(_placemark.getLongitude(), _placemark.getLatitude());
				getMapComponent().setPositionScale(point, 3000.0d);
				loadPlacemarks();
			}
		}
		super.load();
	}
	
	private void loadPlacemarks()
	{
		if (_folder.getCount() == 1)
		{
			int count = _placemark.getCount();
			for (int i = 0; i < count; i++)
			{
				_placemark.moveToPosition(i);
				String styleName = _placemark.getStyle();
				if (styleName == null)
				{
					styleName = StyleManager.STYLE_ROUTE_WAYPOINT;
				}
				if (getMapComponent().hasLayerById(_id) == false)
				{
					getMapComponent().addLayerById(_id, this, _styleManager.getStyle(styleName));
				}
				PointFeature feature = new PointFeature();
				feature.setUri(Uri.withAppendedPath(Placemark.CONTENT_URI, _placemark.get_ID()));
				feature.setLabel(_placemark.getName());
				Point2D.Double point = new Point2D.Double(_placemark.getLongitude(), _placemark.getLatitude());
				feature.setPoint(point);
				addFeature(_id, feature);
				String waypointId = _placemark.getId();
				Track track = new Track(_context.getContentResolver().query(Track.CONTENT_URI, null, Track.PARENT_ID + "='" + waypointId + "'", null, Track.SEQUENCE + " ASC"));
				if (track != null)
				{
					if (track.getCount() > 0)
					{
						loadTracks(track);
					}
					track.close();
					track = null;
				}
			}
		}
	}

	private void loadTracks(Track track)
	{
		_tracksLayerId = UUID.randomUUID().toString().toLowerCase();
		getMapComponent().addLayerById(_tracksLayerId, null, _styleManager.getStyle(StyleManager.STYLE_ROUTE_TRACKS));
		int count = track.getCount();
		LineFeature lineFeature = new LineFeature();
		for (int i = 0; i < count; i++)
		{
			track.moveToPosition(i);
			Point2D.Double point = new Point2D.Double(track.getLongitude(), track.getLatitude());
			lineFeature.addPoint(point);
		}
		addFeature(_tracksLayerId, lineFeature);
	}
	
	private void addFeature(String layerName, Feature feature)
	{
		getMapComponent().addFeature(layerName, feature);
		_features.add(feature);
	}

	private void removeAllFeatures()
	{
		for (int i = 0; i < _features.size(); i++)
		{
			Feature feature = _features.get(i);
			getMapComponent().removeFeature(feature);
		}
		_features.clear();
	}
	
	@Override
	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		getMapComponent().enableLayers("FCFolder" + _id + "*", value);
	}
	
	public Placemark findPlacemark(Uri uri)
	{
		if (_placemark != null)
		{
			int count = _placemark.getCount();
			if (count > 0)
			{
				for (int i = 0; i < count; i++)
				{
					_placemark.moveToPosition(i);
					Uri resultUri = Uri.withAppendedPath(Placemark.CONTENT_URI, _placemark.get_ID());
					if (resultUri.equals(uri))
					{
						return _placemark;
					}
				}
			}
		}
		return null;
	}
	
	private class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
		{
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_placemark != null)
			{
				_placemark.requery();
				removeAllFeatures();
				loadPlacemarks();
			}
		}
	}
}
