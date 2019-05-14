package com.futureconcepts.ax.trinity.osm;

import java.io.StringReader;
import java.util.ArrayList;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.location.Location;
import android.util.Log;


public class BombLayer implements MyLocationObserver, DangerZoneNotifier{
	private String TAG = BombLayer.class.getSimpleName();
	private  final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	private BombLayerCircle shelter;
	private BombLayerCircle evacuationDistance;
	private MapView map;
	private List<DangerZone> dangerZoneListeners;
	
	public BombLayer()
	{
		dangerZoneListeners = new ArrayList<DangerZone>();
	}
	
	public void displayBombLayer(final Context context,String xmlData, Layers layers,LatLong latLong, MapView map)
	{		
		this.map = map;
		final BombData bombData = parseIncidentDetails(xmlData);
		shelter = new BombLayerCircle(latLong,converFeetsToMeters(bombData.getShelterInPlaceZone()),
				getDefaultCircleFillYellow(),getDefaultCircleStrokeYellow());
		layers.add(shelter);		
		evacuationDistance= new BombLayerCircle(latLong,converFeetsToMeters(bombData.getMandatoryEvacuationDistance()),
				getDefaultCircleFill(),null);
		layers.add(evacuationDistance);
		
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
	  
	 private Paint getDefaultCircleStrokeYellow()
	 {
	   return getPaint(GRAPHIC_FACTORY.createColor(160, 255, 255, 0), 2, Style.STROKE);
	 }
	 
	
	 private float converFeetsToMeters(float feets)
	 {
		 return (float) (feets/3.2808);
	 }
	 
	private  BombData parseIncidentDetails(String xmlData)
	{
		BombData style = new BombData();
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
            	if(xpp.getName().equals("Name")){
            		style.setName(xpp.nextText());
            		//Log.e(TAG, "Name: "+style.getName());
            	}else if(xpp.getName().equals("MandatoryEvacuationDistance")){
            		style.setMandatoryEvacuationDistance(Float.parseFloat(xpp.nextText()));
            		//Log.e(TAG, "MandatoryEvacuationDistance: "+style.getMandatoryEvacuationDistance());
            	}else if(xpp.getName().equals("ShelterInPlaceZone")){            	
            		style.setShelterInPlaceZone(Float.parseFloat(xpp.nextText()));
            		//Log.e(TAG, "ShelterInPlaceZone: "+style.getShelterInPlaceZone());
            	}else if(xpp.getName().equals("PreferredDistance")){            		
            		style.setPreferredDistance(Float.parseFloat(xpp.nextText()));
            		//Log.e(TAG, "PreferredDistance: "+style.getPreferredDistance());
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
	public void locationHasChange(boolean snapToLocationEnabled,Location location) {
		Point center = toPixels(shelter.getPosition());
		Point lo = toPixels(new LatLong(location.getLatitude(),location.getLongitude()));
		if(shelter.contains(center,lo))
		{
			notifyDangerZone(true);
		}else
		{
			notifyDangerZone(false);
			//Log.e(TAG, "Location is not on shelter");
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
	
	private class BombLayerCircle extends Circle
	{
		public BombLayerCircle(LatLong latLong, float radius, Paint paintFill,
				Paint paintStroke) {
			super(latLong, radius, paintFill, paintStroke);
			// TODO Auto-generated constructor stub
		}		
	
		public boolean contains(Point center, Point point)
		{
			return center.distance(point) < getRadius();
		}
		
	}	
}
