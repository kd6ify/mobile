package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentType;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;


public class IncidentOverlay
{
	private static String TAG = IncidentOverlay.class.getSimpleName();
	private String ID; 
	private Context context;
	private  LatLong incidentLocation;
	
	public IncidentOverlay ()
	{
		
	}
//	public IncidentOverlay(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
//		super(latLong, bitmap,horizontalOffset,verticalOffset);
//	} 
	
	public IncidentOverlayMarker creteIncidentItem(Incident _incident, Context context)
	{	
		AndroidBitmap bitmap =  getBitmap(_incident,context);
		bitmap.scaleTo(65, 70);		
		//bitmap.setBackgroundColor(Color.BLUE);
		incidentLocation =  getGeoPointFromWKT(_incident.getAddress(context));
		IncidentOverlayMarker item = new IncidentOverlayMarker(incidentLocation,bitmap,0,0);
		setId( _incident.getAddressID());
		setContext(context);
		return item;
	}
	

	
	public static AndroidBitmap getBitmap(Incident incident, Context context)
	{
		IncidentType type = incident.getType(context);
		if (type != null)
		{
			Icon icon = type.getIcon(context);
			if (icon != null)
			{
				byte[] bytes = icon.getImage();
				Drawable image = null;
				image =  new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
				return (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(image);
			}
		}else 
		{
			Log.w(TAG, "Icon Not Found");
		}
		return null;
	}

	public static LatLong getGeoPointFromWKT(Address address)
	{
		LatLong result = null;
		if (address != null && address.getWKT() != null)
		{
			String a =  address.getWKT().replaceAll("[()]", "");
			String [] g = a.replaceAll("POINT", "").split(" ");
			result = new LatLong(Double.parseDouble(g[1]), Double.parseDouble(g[0]));
		}
		else
		{
			result = new LatLong(0,0);
		}
		return result;
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
	
	public LatLong getIncidentLocation()
	{
		return this.incidentLocation;
	}
	
	public class IncidentOverlayMarker extends BaseClassMarker
	{
		public IncidentOverlayMarker(LatLong latLong, Bitmap bitmap,
				int horizontalOffset, int verticalOffset) {
			super(latLong, bitmap, horizontalOffset, verticalOffset);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean onTap(LatLong tapLatLong, Point viewPosition, Point tapPoint) {
			// TODO Auto-generated method stub
			if (contains(viewPosition, tapPoint))
	        {
				Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Address.CONTENT_URI, getID()));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);
	            return true;
	        }
	        return false;
		}
		
	}
	
	

}
