package com.futureconcepts.ax.trinity.osm;

import java.util.ArrayList;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Polygon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class TappablePolygon extends Polygon
{
	public TappablePolygon(Paint paintFill, Paint paintStroke,
			GraphicFactory graphicFactory) {
		super(paintFill, paintStroke, graphicFactory);
		// TODO Auto-generated constructor stub
	}		

	public boolean contains(Point tap, MapView mapView)
	{
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		for(LatLong point :this.getLatLongs())
		{
			((CustomLatLong)point).updateXYPoint(mapView);
			Point obj = ((CustomLatLong)point).getAsPoint();
			points.add(new Coordinate(obj.x,obj.y));
		}			
		GeometryFactory gf = new GeometryFactory();
	    com.vividsolutions.jts.geom.Polygon polygon = gf.createPolygon(new LinearRing(new CoordinateArraySequence(points
	    		.toArray(new Coordinate[points.size()])), gf), null);
		com.vividsolutions.jts.geom.Point point = gf.createPoint(new Coordinate(tap.x,tap.y));
		System.out.println(point.within(polygon));
		if(polygon.contains(point)){
			return true;
		}
		return false;
	}				
}
