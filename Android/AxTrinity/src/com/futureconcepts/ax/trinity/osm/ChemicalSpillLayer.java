package com.futureconcepts.ax.trinity.osm;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Polygon;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;


import android.content.Context;
import android.location.Location;
import android.util.Log;

public class ChemicalSpillLayer implements MyLocationObserver, DangerZoneNotifier{
	private String TAG = ChemicalSpillLayer.class.getSimpleName();
	private  final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	private List<DangerZone> dangerZoneListeners;
	private ChemicalLayer dangerZone;
	private MapView map;
	private TappablePolygon actionDistance;
	public ChemicalSpillLayer()
	{
		dangerZoneListeners = new ArrayList<DangerZone>();
	}	
	
	private  Paint getPaint(int color, int strokeWidth, Style style)
	  {
	    Paint paint = GRAPHIC_FACTORY.createPaint();
	    paint.setColor(color);
	    paint.setStrokeWidth(strokeWidth);
	    paint.setStyle(style);
	    return paint;
	  }
	private Paint getDefaultCircleFill()
	{
	  return getPaint(GRAPHIC_FACTORY.createColor(100, 255, 0, 0), 0, Style.FILL);
	}
	
	private Paint getDefaultCircleFillYellow()
	{
	  return getPaint(GRAPHIC_FACTORY.createColor(100, 255, 255, 0), 0, Style.FILL);
	}
	
	private Paint getDefaultCircleFillStroke()
	{
		 return getPaint(GRAPHIC_FACTORY.createColor(160, 255, 255, 0), 2, Style.STROKE);
	}
	
	public void displayChemicalSpillLayer(Context context,String xmlData, Layers layers,LatLong latLong, MapView mapView)
	{
		map = mapView;
		ChemicalSpillData chemicalData = parseIncidentDetails(xmlData);
		dangerZone = new ChemicalLayer(latLong,chemicalData.getChemicalSpillArea(),
				getDefaultCircleFill(),null);
		
		actionDistance = new TappablePolygon(getDefaultCircleFillYellow(),
				getDefaultCircleFillStroke(), GRAPHIC_FACTORY);
		
		List<CustomLatLong> points = generateProtectiveAtionDistance(latLong,chemicalData);
		actionDistance.getLatLongs().addAll(points);

		layers.add(actionDistance);
		layers.add(dangerZone);
	}
	
	private List<CustomLatLong> generateProtectiveAtionDistance(LatLong initialPoint,ChemicalSpillData chemicalData)
	{
		chemicalData.printAllValues();
		float raius = chemicalData.getChemicalSpillArea();
		float angle =chemicalData.getWindDirection();
		float distance = chemicalData.getChemicalProactiveDistance()*1000;//in meters		
		CustomLatLong circleEast = new CustomLatLong(getLatitude(initialPoint.latitude, getDy(raius, 90+angle)),//before 90+angle
				getLongitude(initialPoint.longitude,initialPoint.latitude, getDx(raius, 90+ angle)),map);//before90+angle//		
		CustomLatLong circleWest =new CustomLatLong(getLatitude(initialPoint.latitude, getDy(raius, 270+angle)),//before 270+angle
				getLongitude(initialPoint.longitude,initialPoint.latitude, getDx(raius, 270+angle)),map);//before 270+angle//
		
		double longitudeMaxWind = getLongitude(initialPoint.longitude, initialPoint.latitude, getDx(distance, angle+180));//-117.9386358843031
		double latitudeMaxWind =  getLatitude(initialPoint.latitude, getDy(distance, angle+180));
		//LatLong middlePoint = new LatLong(latitudeMaxWind, longitudeMaxWind);
		double topMiddlePointLong =getLongitude(longitudeMaxWind, latitudeMaxWind, getDx(distance/2, 90+angle));//before 90
		double topMiddlePointLat = getLatitude(latitudeMaxWind, getDy(distance/2, 90+angle));//before 90+angle
		
		double bottomMiddlePointLong = getLongitude(longitudeMaxWind, latitudeMaxWind, getDx(distance/2, 270+angle));//before 270
		double bottomMiddlePointLat = getLatitude(latitudeMaxWind, getDy(distance/2, 270+angle));//before 270+angle
		
		
		List<CustomLatLong>  points= new ArrayList<CustomLatLong>();
		points.add(circleEast);		
		//CustomLatLong ll = new CustomLatLong(bottomMiddlePointLat,bottomMiddlePointLong);
		points.add(new CustomLatLong(topMiddlePointLat,topMiddlePointLong,map));
		points.add(new CustomLatLong(bottomMiddlePointLat,bottomMiddlePointLong,map));
		points.add(circleWest);
		points.add(circleEast);		
		return points;
	}
	
	
		
	private double getLongitude(double longitude,double latitude, double Dx)
	{
		return longitude+ deltaLongitude(Dx,latitude);
	}
	
	private double getLatitude(double latitude, double Dy)
	{
		return latitude+deltaLatitude(Dy);
	}
	
	private double deltaLongitude(double Dx,double latitude)
	{
		//111320 is constant for longitude
		return  Dx/(111320*Math.cos(Math.toRadians(latitude)));
		
	}
	private double deltaLatitude(double Dy)
	{
		//110540 is constant for latitude
		return Dy/110540; 
	}
	
	 private double getDx(float raius, float f)
	 {
		 return raius*(Math.sin(Math.toRadians(f)));
	 }
	 private double getDy(double distance, float f)
	 {
		 return distance*(Math.cos(Math.toRadians(f)));
	 }
	
	private  ChemicalSpillData parseIncidentDetails(String xmlData)
	{
		ChemicalSpillData style = new ChemicalSpillData();
		if(xmlData==null)
		{
			return style;
		}
		try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader (xmlData));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_TAG) {
        		if(xpp.getName().equals("IsEquipmentWeather")){
        			style.setIsEquipmentWeather(Boolean.parseBoolean(xpp.nextText()));
        		}else if(xpp.getName().equals("WeatherId")){
            		style.setWeatherId(xpp.nextText());
            	}else if(xpp.getName().equals("Guide")){
            		style.setGuideID(xpp.getAttributeValue(null,"ID"));
            	}else if(xpp.getName().equals("IsCompressed")){            	
            		style.setIsCompressed(Boolean.parseBoolean(xpp.nextText()));
            	}else if(xpp.getName().equals("Name")){            		
            		style.setName(xpp.nextText());
            	}else if(xpp.getName().equals("GuideNo")){            		
            		style.setGuideNo(Integer.parseInt(xpp.nextText()));
            	}else if(xpp.getName().equals("SmallIIZM")){            		
            		style.setSmallIIZM(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("SmallPadDayKm")){            		
            		style.setSmallPadDayKm(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("SmallPadNightKm")){            		
            		style.setSmallPadNightKm(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("LargeIIZM")){            		
            		style.setLargeIIZM(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("LargePADDayKm")){            		
            		style.setLargePADDayKm(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("LargePadNightKm")){            		
            		style.setLargePadNightKm(Float.parseFloat(xpp.nextText()));
            	}else if(xpp.getName().equals("FireEvacuation")){            	
            		style.setFireEvacuation(Boolean.parseBoolean(xpp.getAttributeValue(null,"xsi:nil")));
            	}else if(xpp.getName().equals("LastModified")){
            		style.setLastModified(xpp.nextText());
            	}else if(xpp.getName().equals("IsLargeSpill")){            	
            		style.setIsLargeSpill(Boolean.parseBoolean(xpp.nextText()));
            	}else if(xpp.getName().equals("WindDirection")){            		
            		style.setWindDirection(Integer.parseInt(xpp.nextText()));
            	}
        		
        	} 
        	eventType = xpp.next();
        } 
		}catch(Exception e)
		{
			e.printStackTrace();	
		}
        return style;
	}
	
	private class ChemicalLayer extends Circle
	{

		public ChemicalLayer(LatLong latLong, float radius, Paint paintFill,
				Paint paintStroke) {
			super(latLong, radius, paintFill, paintStroke);
			// TODO Auto-generated constructor stub
		}
		
		public boolean contains(Point center, Point point)
		{
			return center.distance(point) < getRadius();
		}
		
	}
	
	
	@Override
	public void registerDangerListener(DangerZone addListener)
	{
		dangerZoneListeners.add(addListener);
	}
	
	@Override
	public void unRegisterDangerListener(DangerZone removeListener)
	{
		if(removeListener!=null){
			// Get the index of the observer to delete
			int listenerIndex = dangerZoneListeners.indexOf(removeListener);
			// Print out message (Have to increment index to match)
			System.out.println("Danger Observer " + (listenerIndex + 1) + " deleted");
			// Removes observer from the ArrayList
			dangerZoneListeners.remove(listenerIndex);
		}
	}
	
	@Override
	public void notifyDangerZone(boolean isDangerZone) {
		// Cycle through all observers and notifies them of
		for (DangerZone listener : dangerZoneListeners) {
			listener.locationIsInDangerZone(isDangerZone);
		}
	}
	@Override
	public void locationHasChange(boolean snapToLocationEnabled,
			Location location) {
		Point center = toPixels(dangerZone.getPosition());
		Point userLocation = toPixels(new LatLong(location.getLatitude(),location.getLongitude()));
		if(dangerZone.contains(center,userLocation) || actionDistance.contains(userLocation,map) )
		{
			notifyDangerZone(true);			
		}else
		{
			notifyDangerZone(false);
		}
		
	}
	
	/**
     * Converts geographic coordinates to view x/y coordinates in the map view.
     * @param in the geographic coordinates
     * @return x/y view coordinates for the given location
     */
    public Point toPixels(LatLong in) {
    	if (in == null || map.getWidth() <= 0 || map.getHeight() <= 0) {
    		return null;
        }
    	MapPosition mapPosition = map.getModel().mapViewPosition.getMapPosition();
    	// calculate the pixel coordinates of the top left corner
    	LatLong latLong = mapPosition.latLong;
    	int tileSize = map.getModel().displayModel.getTileSize();
    	double pixelX = MercatorProjection.longitudeToPixelX(latLong.longitude, mapPosition.zoomLevel, tileSize);
    	double pixelY = MercatorProjection.latitudeToPixelY(latLong.latitude, mapPosition.zoomLevel, tileSize);
    	pixelX -= map.getWidth() >> 1;
    	pixelY -= map.getHeight() >> 1;
        // create a new point and return it
		return new Point(
				(int) (MercatorProjection.longitudeToPixelX(in.longitude, mapPosition.zoomLevel, tileSize) - pixelX),
				(int) (MercatorProjection.latitudeToPixelY(in.latitude, mapPosition.zoomLevel, tileSize) - pixelY));
    }
	
	

}
