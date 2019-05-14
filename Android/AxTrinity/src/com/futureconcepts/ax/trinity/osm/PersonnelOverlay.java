package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
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

import com.futureconcepts.ax.model.data.Asset;

public class PersonnelOverlay extends BaseClassMarker  {

	private static String TAG = IncidentOverlay.class.getSimpleName();
		
	private String ID; 
	private Context context;

	public PersonnelOverlay(LatLong latLong, Bitmap bitmap,	int horizontalOffset, int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
		// TODO Auto-generated constructor stub
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

	public static void createPersonnelItems(Context context, Layers layers,PersonnelViewCursorByCheckIn _personnel, LruOSMIconsCache iconsCache  )
	{
		if(_personnel.getCount()>0){
			_personnel.moveToFirst();
		  do{
			  LatLong point = getGeoPointFromWKT(_personnel.getCursorString("AddressWKT"));
			 AndroidBitmap bitmap;
			  if(iconsCache.getIconFromCache(_personnel.getIconID())!=null)
			  {
				  bitmap = iconsCache.getIconFromCache(_personnel.getIconID());
				  bitmap.incrementRefCount();
			  }else{
				  bitmap= getBitmap(_personnel.getUserTypeIcon());
				  bitmap.scaleTo(50, 50);
				  iconsCache.addIconToCache(_personnel.getIconID(), bitmap);
			  }		
			  PersonnelOverlay item = new PersonnelOverlay(point,bitmap,0, -bitmap.getHeight() / 2);
			  
			  item.setId( _personnel.getID());
			  item.setContext(context);
			  layers.add(item);
		  }while(_personnel.moveToNext());
		    
		}
	}
	
	public static AndroidBitmap getBitmap(byte [] icon)
	{
		if (icon != null)
		{
			Drawable image = null;
			image =  new BitmapDrawable(BitmapFactory.decodeByteArray(icon, 0, icon.length));
			return (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(image);
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
