package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.view.MapView;

public class CustomLatLong extends LatLong
{	
	private Point latLongAsPoint;
	
	public Point getAsPoint()
	{
		return latLongAsPoint;
	}
	
	public void updateXYPoint(MapView map)
	{
		latLongAsPoint = toPixels(this, map);
	}
	public CustomLatLong(double latitude, double longitude, MapView map) {
		super(latitude, longitude);
		// TODO Auto-generated constructor stub
		if(map!=null)
		latLongAsPoint = toPixels(this,map);
	}
	
	/**
     * Converts geographic coordinates to view x/y coordinates in the map view.
     * @param in the geographic coordinates
     * @return x/y view coordinates for the given location
     */
    public Point toPixels(LatLong in, MapView map) {
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