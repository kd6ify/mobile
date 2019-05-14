package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.model.DisplayModel;

public class BaseClassMarker extends Layer
{
	  private Bitmap bitmap;
	  private int horizontalOffset;
	  private LatLong latLong;
	  private int verticalOffset;
	  
	  public BaseClassMarker(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset)
	  {
	    this.latLong = latLong;
	    this.bitmap = bitmap;
	    this.horizontalOffset = horizontalOffset;
	    this.verticalOffset = verticalOffset;
	  }
	  
	  public synchronized boolean contains(Point center, Point point)
	  {
	    Rectangle r = new Rectangle(center.x - this.bitmap.getWidth() / 2.0F + this.horizontalOffset, center.y - this.bitmap.getHeight() / 2.0F + this.verticalOffset, center.x + this.bitmap.getWidth() / 2.0F + this.horizontalOffset, center.y + this.bitmap.getHeight() / 2.0F + this.verticalOffset);
	    

	    return r.contains(point);
	  }
	  
	  public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint)
	  {
		  try{
	    if ((this.latLong == null) || (this.bitmap == null)) {
	      return;
	    }
	    int tileSize = this.displayModel.getTileSize();
	    double pixelX = MercatorProjection.longitudeToPixelX(this.latLong.longitude, zoomLevel, tileSize);
	    double pixelY = MercatorProjection.latitudeToPixelY(this.latLong.latitude, zoomLevel, tileSize);
	    
	    int halfBitmapWidth = this.bitmap.getWidth() / 2;
	    int halfBitmapHeight = this.bitmap.getHeight() / 2;
	    
	    int left = (int)(pixelX - topLeftPoint.x - halfBitmapWidth + this.horizontalOffset);
	    int top = (int)(pixelY - topLeftPoint.y - halfBitmapHeight + this.verticalOffset);
	    int right = left + this.bitmap.getWidth();
	    int bottom = top + this.bitmap.getHeight();
	    
	    Rectangle bitmapRectangle = new Rectangle(left, top, right, bottom);
	    Rectangle canvasRectangle = new Rectangle(0.0D, 0.0D, canvas.getWidth(), canvas.getHeight());
	    if (!canvasRectangle.intersects(bitmapRectangle)) {
	      return;
	    }
	    canvas.drawBitmap(this.bitmap, left, top);
		  }catch(Exception e){e.printStackTrace();}
	  }
	  
	  public synchronized Bitmap getBitmap()
	  {
	    return this.bitmap;
	  }
	  
	  public synchronized int getHorizontalOffset()
	  {
	    return this.horizontalOffset;
	  }
	  
	  public synchronized LatLong getLatLong()
	  {
	    return this.latLong;
	  }
	  
	  public synchronized LatLong getPosition()
	  {
	    return this.latLong;
	  }
	  
	  public synchronized int getVerticalOffset()
	  {
	    return this.verticalOffset;
	  }
	  
	  public synchronized void onDestroy()
	  {
	    if (this.bitmap != null) {
	      this.bitmap.decrementRefCount();
	    }
	  }
	  
	  public synchronized void setBitmap(Bitmap bitmap)
	  {
	    if ((this.bitmap != null) && (this.bitmap.equals(bitmap))) {
	      return;
	    }
	    if (this.bitmap != null) {
	      this.bitmap.decrementRefCount();
	    }
	    this.bitmap = bitmap;
	  }
	  
	  public synchronized void setHorizontalOffset(int horizontalOffset)
	  {
	    this.horizontalOffset = horizontalOffset;
	  }
	  
	  public synchronized void setLatLong(LatLong latLong)
	  {
	    this.latLong = latLong;
	  }
	  
	  public synchronized void setVerticalOffset(int verticalOffset)
	  {
	    this.verticalOffset = verticalOffset;
	  }
	}
