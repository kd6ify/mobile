package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentType;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;

public class EquipmentOverlay extends BaseClassMarker  {

	private static String TAG = IncidentOverlay.class.getSimpleName();
		
	private String ID; 
	private Context context;

	public EquipmentOverlay(LatLong latLong, Bitmap bitmap,	int horizontalOffset, int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
		// TODO Auto-generated constructor stub
	}
	
 @Override
public synchronized void draw(BoundingBox boundingBox, byte zoomLevel,
		Canvas canvas, Point topLeftPoint) {
	// TODO Auto-generated method stub
	super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
}
	
	
	public boolean onTap(LatLong tapLatLong, Point viewPosition, Point tapPoint) {
		// TODO Auto-generated method stub
		if (contains(viewPosition, tapPoint))
        {
			Uri uri = Uri.withAppendedPath(Asset.CONTENT_URI, getID());
			Log.d(TAG, "tapped on " + uri.toString());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
          
          return true;
        }
        return false;
	}
	
	public String getID()
	{
		return ID;
	}
	
	public void setId (String ID)
	{
		this.ID = ID;
	}
	
	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}

	public static void createEquipmentItems(Context context, Layers layers,EquipmentViewCursorByCheckIn _equipment, LruOSMIconsCache iconsCache )
	{
		if(_equipment.getCount()>0){
		  _equipment.moveToFirst();
		  do{
			  LatLong point = getGeoPointFromWKT(_equipment.getCursorString("AddressWKT"));
			  AndroidBitmap bitmap;
			  if(iconsCache.getIconFromCache(_equipment.getIconID())!=null)
			  {
				  bitmap = iconsCache.getIconFromCache(_equipment.getIconID());
				  bitmap.incrementRefCount();
			  }else{
				  bitmap= getBitmap(_equipment.getEquipmentTypeIcon());
				  bitmap.scaleTo(50, 50);
				  iconsCache.addIconToCache(_equipment.getIconID(), bitmap);
			  }			 
			  EquipmentOverlay item = new EquipmentOverlay(point,bitmap,0, -bitmap.getHeight() / 2);
			  item.setId( _equipment.getID());
			  item.setContext(context);
			  layers.add(item);
		  }while(_equipment.moveToNext());		    
		}
	}
	
	public static AndroidBitmap getBitmap(byte [] icon)
	{
		if (icon != null)
		{
			Drawable image = null;
			image =  new BitmapDrawable(BitmapFactory.decodeByteArray(icon, 0, icon.length));
			return  (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(image);
		}else 
		{
			Log.w(TAG, "Icon Not Found");
		}
		return null;
	}
		
	private static LatLong getGeoPointFromWKT(String address)
	{
		LatLong result = null;
		if (address != null )
		{
			String a =  address.replaceAll("[()]", "");
			String [] g = a.replaceAll("POINT", "").split(" ");
			result = new LatLong(Double.parseDouble(g[1]), Double.parseDouble(g[0]));
		}
		else
		{
			result = new LatLong(0,0);
		}
		return result;
	}
}
