package com.futureconcepts.jupiter.layers;

import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.map.MapComponent;
import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.TransverseMercatorProjection;
import com.futureconcepts.jupiter.map.AreaFeature;
import com.futureconcepts.jupiter.map.AreaLayerStyle;
import com.futureconcepts.jupiter.map.Feature;
import com.futureconcepts.jupiter.map.LineFeature;
import com.futureconcepts.jupiter.map.LineLayerStyle;
import com.futureconcepts.jupiter.map.PointFeature;
import com.futureconcepts.jupiter.map.PointLayerStyle;

public class UtmGridLayer extends MapComponent.Layer
{
	public static final String ID = "ed499d70-629f-46d0-bdfe-73619f734de7";
	public static final String COORDS_ID = "4c3e8a5f-00ae-47c4-b6db-7d8af3f1c118";
	private static final String LAYER_NAME = "UTM Grid";
	private Extent _extent;
	private TransverseMercatorProjection _proj;
	private int _zone;
	private int _row;
	private ArrayList<Feature> _features = new ArrayList<Feature>();
	
	public UtmGridLayer(Context context, MapComponent component)
	{
		component.super();
		_id = ID;
		_extent = getScreenExtent(context);
		LineLayerStyle style = new LineLayerStyle();
		style.setFillColor(0xff00ff00);
		style.setWidth(1);
		getMapComponent().addLayerById(ID, this, style);
		AreaLayerStyle pointStyle = new AreaLayerStyle();
//		pointStyle.setIconDrawable(context.getResources().getDrawable(R.drawable.blue_location));
		pointStyle.setLabelTextColor(0xff000000);
		pointStyle.setLabelFontSize(12.0f);
		pointStyle.setLabelEnabled(true);
		getMapComponent().addLayerById(COORDS_ID, null, pointStyle);
	}

	@Override
	public void close()
	{
		for (Feature feature : _features)
		{
			getMapComponent().removeFeature(feature);
		}
		_features.clear();
	}
	
	private boolean screenToMap(Point2D.Double point)
	{
		boolean value = false;
		if (point != null)
		{
			value = getMapComponent().screenToMap(point);
		}
		return value;
	}
	
	@Override
	public String getName()
	{
		return LAYER_NAME;
	}
	
	@Override
	public void load()
	{
		super.load();
		_proj = new TransverseMercatorProjection();
		_proj.setEllipsoid(Ellipsoid.UTM_NAD27_ZONE11);
		_zone = getZoneFromNearestMeridian(Math.toRadians(_extent.centerLongitude));
		_proj.setUTMZone(_zone);
		_row = getRowFromNearestParallel(Math.toRadians(_extent.centerLatitude));
		drawGrid();
	}

	private Extent getScreenExtent(Context context)
	{
		Extent value = new Extent();
    	Point2D.Double lowerLeftScreen = new Point2D.Double();
    	Point2D.Double upperRightScreen = new Point2D.Double();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		lowerLeftScreen.x = 0;
		lowerLeftScreen.y = dm.heightPixels;
		screenToMap(lowerLeftScreen);
		upperRightScreen.x = dm.widthPixels;
		upperRightScreen.y = 0;
		screenToMap(upperRightScreen);
		value.centerLongitude = (lowerLeftScreen.x + upperRightScreen.x) / 2.0d;
		value.centerLatitude = (lowerLeftScreen.y + upperRightScreen.y) / 2.0d;
		value.lowerLeftDegrees = new Point2D.Double(lowerLeftScreen.x, lowerLeftScreen.y);
		value.upperRightDegrees = new Point2D.Double(upperRightScreen.x, upperRightScreen.y);
		return value;
	}
	
	private Extent getUtmZoneExtent()
	{
		Extent value = new Extent();
		value.lowerLeftDegrees = new Point2D.Double();
		value.lowerLeftDegrees.x = -180.0d + (_zone - 1.0d) * 6.0d;
		value.lowerLeftDegrees.y = 180 - (_row - 1) * 8;
		value.upperRightDegrees = new Point2D.Double();
		value.upperRightDegrees.x = value.lowerLeftDegrees.x + 6.0d;
		value.upperRightDegrees.y = value.lowerLeftDegrees.y + 8.0d;
		return value;
	}

	private void drawGrid()
	{
		Point2D.Double lowerLeftMeters = new Point2D.Double();
		Point2D.Double upperRightMeters = new Point2D.Double();
		_proj.transform(_extent.lowerLeftDegrees, lowerLeftMeters);
		_proj.transform(_extent.upperRightDegrees, upperRightMeters);
		lowerLeftMeters.x = lowerLeftMeters.x - (lowerLeftMeters.x % 1000);
		lowerLeftMeters.y = lowerLeftMeters.y - (lowerLeftMeters.y % 1000);
		upperRightMeters.x = upperRightMeters.x - (upperRightMeters.x % 1000) + 1000;
		upperRightMeters.y = upperRightMeters.y - (upperRightMeters.y % 1000) + 1000;
		drawVerticalGrid(lowerLeftMeters, upperRightMeters);
		drawHorizontalGrid(lowerLeftMeters, upperRightMeters);
		drawCornerCoord(lowerLeftMeters, 0, -.001);
		drawCornerCoord(upperRightMeters, 0, .001);
	}
	
	private void drawCornerCoord(Point2D.Double point, double xoffset, double yoffset)
	{
		Point2D.Double degPoint = new Point2D.Double();
		AreaFeature feature = new AreaFeature();
		_proj.inverseTransform(point, degPoint);
		feature.addPoint(degPoint);
		degPoint.x += xoffset;
		degPoint.y += yoffset;
		feature.addPoint(degPoint);
		feature.setLabel(String.format("%07d %07d", (int)Math.floor(point.x), (int)Math.floor(point.y)));
		addPointFeature(feature);
	}
	
	private void drawVerticalGrid(Point2D.Double llm, Point2D.Double urm)
	{
		Point2D.Double leftMeters = new Point2D.Double();
		Point2D.Double rightMeters = new Point2D.Double();
		Point2D.Double leftDegrees = new Point2D.Double();
		Point2D.Double rightDegrees = new Point2D.Double();
		leftMeters.x = llm.x;
		rightMeters.x = urm.x;
		for (double y = llm.y; y <= urm.y; y += 1000.0d)
		{
			leftMeters.y = y;
			rightMeters.y = y;
			_proj.inverseTransform(leftMeters, leftDegrees);
			_proj.inverseTransform(rightMeters, rightDegrees);
			LineFeature feature = new LineFeature();;
			feature.addPoint(leftDegrees);
			feature.addPoint(rightDegrees);
			addLineFeature(feature);
		}
	}

	private void drawHorizontalGrid(Point2D.Double llm, Point2D.Double urm)
	{
		Point2D.Double bottomMeters = new Point2D.Double();
		Point2D.Double topMeters = new Point2D.Double();
		Point2D.Double bottomDegrees = new Point2D.Double();
		Point2D.Double topDegrees = new Point2D.Double();
		bottomMeters.y = llm.y;
		topMeters.y = urm.y;
		for (double x = llm.x; x <= urm.x; x += 1000.0d)
		{
			bottomMeters.x = x;
			topMeters.x = x;
			_proj.inverseTransform(bottomMeters, bottomDegrees);
			_proj.inverseTransform(topMeters, topDegrees);
			LineFeature feature = new LineFeature();;
			feature.addPoint(bottomDegrees);
			feature.addPoint(topDegrees);
			addLineFeature(feature);
		}
	}
	
	private void addLineFeature(Feature feature)
	{
		getMapComponent().addFeature(ID, feature);
		_features.add(feature);
	}

	private void addPointFeature(Feature feature)
	{
		getMapComponent().addFeature(COORDS_ID, feature);
		_features.add(feature);
	}
	
	public void removeAllFeatures()
	{
	}
	
    private int getRowFromNearestParallel(double latitude)
    {
        int degrees = (int) MapMath.radToDeg(MapMath.normalizeLatitude(latitude));
        if (degrees < -80 || degrees > 84) {
            return 0;
        }
        if (degrees > 80) {
            return 24;
        }
        return (degrees + 80) / 8 + 3;
    }

    private int getZoneFromNearestMeridian(double longitude)
    {
        int zone = (int) Math.floor((MapMath.normalizeLongitude(longitude) + Math.PI) * 30.0 / Math.PI) + 1;
        if (zone < 1) {
            zone = 1;
        } else if (zone > 60) {
            zone = 60;
        }
        return zone;
    }
    
    private class Extent
    {
    	public Point2D.Double lowerLeftDegrees;
    	public Point2D.Double upperRightDegrees;
    	public double centerLongitude;
    	public double centerLatitude;
    }
}
