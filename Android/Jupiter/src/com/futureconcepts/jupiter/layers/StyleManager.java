package com.futureconcepts.jupiter.layers;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.map.AreaLayerStyle;
import com.futureconcepts.jupiter.map.LayerStyle;
import com.futureconcepts.jupiter.map.LineLayerStyle;
import com.futureconcepts.jupiter.map.PointLayerStyle;

public class StyleManager
{
	public static final String STYLE_ROUTE_START = "RouteStart";
	public static final String STYLE_ROUTE_WAYPOINT = "RouteWaypoint";
	public static final String STYLE_ROUTE_FINISH = "RouteFinish";
	public static final String STYLE_ROUTE_ROUTE = "RouteRoute";
	public static final String STYLE_ROUTE_TRACKS = "RouteTracks";
	public static final String STYLE_MY_LOCATION = "MyLocation";
	public static final String STYLE_ACCURACY_CIRCLE = "AccuracyCircle";
	
	private static StyleManager _instance;
	
	private Context _context;
	
	private Hashtable<String, LayerStyle> _map;
	
	public static StyleManager instance(Context context)
	{
		if (_instance == null)
		{
			_instance = new StyleManager(context);
		}
		return _instance;
	}
	
	private StyleManager(Context context)
	{
		_context = context;
		_map = new Hashtable<String, LayerStyle>();
		_map.put(STYLE_ROUTE_START, getPointStyle(R.drawable.go));
		_map.put(STYLE_ROUTE_WAYPOINT, getPointStyle(R.drawable.pause));
		_map.put(STYLE_ROUTE_FINISH, getPointStyle(R.drawable.stop));
		_map.put(STYLE_ROUTE_ROUTE, getPointStyle(R.drawable.blue_marker_r));
		_map.put(STYLE_ROUTE_TRACKS, getLineStyle());
		_map.put(STYLE_MY_LOCATION, getMyLocationStyle());
		_map.put(STYLE_ACCURACY_CIRCLE, getAccuracyCircleStyle());
	}

	public LayerStyle getStyle(String key)
	{
		return _map.get(key);
	}
	
	public Drawable getStyleDrawable(String key)
	{
		LayerStyle style = _map.get(key);
		if (style instanceof PointLayerStyle)
		{
			PointLayerStyle pointStyle = (PointLayerStyle)style;
			return pointStyle.getIconDrawable();
		}
		else
		{
			return _context.getResources().getDrawable(R.drawable.blue_marker_r);
		}
	}
	
	private LayerStyle getPointStyle(int resId)
	{
		PointLayerStyle style = new PointLayerStyle();
		style.setLabelEnabled(true);
		style.setLabelTextColor(0xffff55);
		style.setLabelHaloColor(0x000000);
		style.setLabelFontSize(16.0f);
		style.setIconDrawable(_context.getResources().getDrawable(resId));
		style.setAnchorX(0.5f);
		style.setAnchorY(1.0f);
		return style;
	}
	
	private LayerStyle getMyLocationStyle()
	{
	    // create point layer
	    PointLayerStyle style = new PointLayerStyle();
	    style.setIconDrawable(_context.getResources().getDrawable(R.drawable.hiker));
	    style.setLabelEnabled(false);
	    style.setAnchorX(0.5f);
	    style.setAnchorY(0.5f);
	    return style;
	}
	
	private LayerStyle getAccuracyCircleStyle()
	{
	    AreaLayerStyle style = new AreaLayerStyle();
	    style.setFillColor(0x11000055);
	    return style;
	}
	
	private LayerStyle getLineStyle()
	{
		LineLayerStyle style = new LineLayerStyle();
		style.setFillColor(0x7fcf0064);
		style.setWidth(6);
		return style;
	}
}
