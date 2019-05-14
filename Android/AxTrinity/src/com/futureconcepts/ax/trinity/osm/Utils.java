package com.futureconcepts.ax.trinity.osm;

import java.io.File;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public final class Utils {
	
	  static Marker createMarker(Context c, int resourceIdentifier, LatLong latLong)
	  {
	    Drawable drawable = c.getResources().getDrawable(resourceIdentifier);
	    org.mapsforge.core.graphics.Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
	    return new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
	  }
	  
	  static Paint createPaint(int color, int strokeWidth, Style style)
	  {
	    Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
	    paint.setColor(color);
	    paint.setStrokeWidth(strokeWidth);
	    paint.setStyle(style);
	    return paint;
	  }
	  
	  static  int createARGBColorFromString(String argbColor)
	  {
			  String [] colorStyle = new String[4];
			  for(int i = 0; i <4;i++){
				  colorStyle[i] = argbColor.substring((i*3), (i*3)+3);
			  }
			  return  AndroidGraphicFactory.INSTANCE.createColor(Integer.valueOf(colorStyle[0]),
				 Integer.valueOf(colorStyle[1]),
				 Integer.valueOf(colorStyle[2]),
				 Integer.valueOf(colorStyle[3]));
	  }
	  
	  static Marker createTappableMarker(Context c, int resourceIdentifier, LatLong latLong)
	  {
	    Drawable drawable = c.getResources().getDrawable(resourceIdentifier);
	    org.mapsforge.core.graphics.Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
	  return new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2)
	    {
	      public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint)
	      {
	        if (contains(viewPosition, tapPoint))
	        {
	          Log.w("Tapp", "The Marker was touched with onTap: " + getLatLong().toString());
	          
	          return true;
	        }
	        return false;
	      }
	    };
	  }
	
	static TileRendererLayer createTileRendererLayer(TileCache tileCache, MapViewPosition mapViewPosition, File mapFile, XmlRenderTheme renderTheme, boolean hasAlpha)
	  {
	    TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapViewPosition, hasAlpha, AndroidGraphicFactory.INSTANCE);
	    
	    tileRendererLayer.setMapFile(mapFile);
	    tileRendererLayer.setXmlRenderTheme(renderTheme);
	    tileRendererLayer.setTextScale(1.5F);
	    return tileRendererLayer;
	  }
	
	static org.mapsforge.core.graphics.Bitmap viewToBitmap(Context c, View view)
	  {
	    view.measure(View.MeasureSpec.getSize(view.getMeasuredWidth()), View.MeasureSpec.getSize(view.getMeasuredHeight()));
	    
	    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
	    view.setDrawingCacheEnabled(true);
	    Drawable drawable = new BitmapDrawable(c.getResources(), android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
	    
	    view.setDrawingCacheEnabled(false);
	    return AndroidGraphicFactory.convertToBitmap(drawable);
	  }
	  
	  private Utils()
	  {
	    throw new IllegalStateException();
	  }

}
