package com.futureconcepts.jupiter.layers;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.map.MapComponent;
import com.futureconcepts.jupiter.map.LineFeature;
import com.futureconcepts.jupiter.map.LineLayerStyle;
import com.futureconcepts.jupiter.map.PointFeature;
import com.futureconcepts.jupiter.map.PointLayerStyle;
import com.futureconcepts.jupiter.util.FormatterFactory.ScalarFormatter;

public class DistanceRulerLayer extends MapComponent.Layer
{
	public static final String ID = "5e3836ae-63a2-44c5-8e76-f2a038b6b971";
	public static final String MARKER_ID = "ec65bad5-60c2-46cc-bcff-e7a73560b10d";
	private static final String LAYER_NAME = "Distance Ruler";
	private ArrayList<PointFeature> _points;
	private ArrayList<LineFeature> _lines;
	
	public DistanceRulerLayer(Context context, MapComponent component)
	{
		component.super();
		_id = ID;
		createLineStyle(context);
		createPointStyle(context);
		_points = new ArrayList<PointFeature>();
		_lines = new ArrayList<LineFeature>();
	}

	private void createLineStyle(Context context)
	{
		LineLayerStyle style = new LineLayerStyle();
		style.setLabelEnabled(true);
		style.setLabelTextColor(0xff000000);
		style.setLabelHaloColor(0xff00ff00);
		style.setLabelFontSize(14.0f);
		style.setFillColor(0xff000000);
		style.setWidth(2);
		postAddLayer(style);
	}
	
	private void createPointStyle(Context context)
	{
		PointLayerStyle style = new PointLayerStyle();
		style.setAnchorX(0.5f);
		style.setAnchorY(1.0f);
		style.setIconDrawable(context.getResources().getDrawable(R.drawable.blue_marker_m));
		postAddLayer(MARKER_ID, style);
	}
	
	@Override
	public void close()
	{
		removeAllFeatures();
		getMapComponent().removeLayerById(ID);
		getMapComponent().removeLayerById(MARKER_ID);
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
		if (_points.size() > 0)
		{
			removeAllFeatures();
		}
	}

	@Override
	public void setEnabled(boolean value)
	{
		getMapComponent().enableLayer(MARKER_ID, value);
		super.setEnabled(value);
	}
	
	private void removeAllFeatures()
	{
		MapComponent mc = getMapComponent();
		if (_points != null)
		{
			for (PointFeature pointFeature : _points)
			{
				mc.removeFeature(pointFeature);
			}
			_points.clear();
		}
		if (_lines != null)
		{
			for (LineFeature lineFeature : _lines)
			{
				mc.removeFeature(lineFeature);
			}
			_lines.clear();
		}
	}
	
	public void addPoint(ScalarFormatter formatter, Point2D.Double point)
	{
		PointFeature pointFeature = new PointFeature();
		pointFeature.setPoint(point);
		getMapComponent().addFeature(MARKER_ID, pointFeature);
		_points.add(pointFeature);
		int pointCount = _points.size();
		if (pointCount > 1)
		{
			LineFeature lineFeature = new LineFeature();
			Point2D.Double point1 = _points.get(pointCount - 2).getPoint();
			Point2D.Double point2 = _points.get(pointCount - 1).getPoint();
			lineFeature.addPoint(point1);
			lineFeature.addPoint(point2);
			float results[] = new float[3];
			Location.distanceBetween(point1.y, point2.x, point2.y, point2.x, results);
			String resultString = formatter.format(results[0]);
			lineFeature.setLabel(resultString);
			getMapComponent().addFeature(ID, lineFeature);
		}
		getMapComponent().repaint();
	}
}
