package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.trinity.R;

public class TriageOverlay extends BaseClassMarker {

	private Context context;
	private String triageId;
	
	public TriageOverlay(LatLong latLong, Bitmap bitmap, int horizontalOffset,
			int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
		// TODO Auto-generated constructor stub
	}
	
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		// TODO Auto-generated method stub
		if(contains(layerXY,tapXY))
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Triage.CONTENT_URI, getTriageId()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
			return true;
		}
		return false;
	}
	
	public static void createTriageItems(Context context, Layers layers,Triage triage,LruOSMIconsCache iconsCache)
	{
	//	Log.e("asd","Tactic vcount: "+);
		int count = triage.getCount();
		if(triage.getCount()>0){
			for (int i = 0; i < count; i++)
			{
				triage.moveToPosition(i);
				Address address = triage.getAddress(context);
				if (address != null)
				{
					String wkt = address.getWKT();
					{
						if (wkt != null)
						{
							if (wkt.contains("POINT") || wkt.contains("POLYGON"))
							{
								//Log.e("dasdsa", wkt);
								layers.add(createTriageMarker(triage,context,iconsCache));
							}
						}
					}
				}
			}
		}
	}

	private static Layer createTriageMarker(Triage triage, Context context,LruOSMIconsCache iconsCache) {
		// TODO Auto-generated method stub
		LatLong point = getGeoPointFromWKT(triage.getAddress(context));
		Bitmap bitmap = getTriageIcon(triage.getColorID(),context,iconsCache);
		bitmap.scaleTo(50, 50);
		TriageOverlay item = new TriageOverlay(point,bitmap,0,-bitmap.getHeight()/2);
		item.setContext(context);
		item.setTriageId(triage.getID());
		return item;
	}
	
	private static Bitmap getTriageIcon(String triageColotID,Context context,LruOSMIconsCache iconsCache)
	{	
		AndroidBitmap bitmap;
		if(iconsCache.getIconFromCache(triageColotID)!=null)
		{
			bitmap = iconsCache.getIconFromCache(triageColotID);
			bitmap.incrementRefCount();
		}else{
			if (triageColotID.equals(TriageColor.GREEN))
			{	
				bitmap=(AndroidBitmap) AndroidGraphicFactory.convertToBitmap(
					context.getResources().getDrawable(R.drawable.triage_green_indicator_icon));
				
			}
			else if (triageColotID.equals(TriageColor.RED))
			{
				bitmap= (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(
					context.getResources().getDrawable( R.drawable.triage_red_indicator_icon));
			}
			else if (triageColotID.equals(TriageColor.YELLOW))
			{
				bitmap= (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(
					context.getResources().getDrawable( R.drawable.triage_yellow_indicator_icon));
			}
			else
			{
				bitmap= (AndroidBitmap)AndroidGraphicFactory.convertToBitmap(
					context.getResources().getDrawable(R.drawable.triage_black_indicator_icon));
			}	
			iconsCache.addIconToCache(triageColotID, bitmap);
		}
		return bitmap;
	}
	
	
	
	private static LatLong getGeoPointFromWKT(Address address)
	{
		LatLong result = null;
			if(address.getWKT().contains("POINT"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POINT", "").split(" ");
				result = new LatLong(Double.parseDouble(g[1]), Double.parseDouble(g[0]));
			}else if(address.getWKT().contains("POLYGON"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POLYGON", "").split(",");
				String [] f = g[0].split(" ");
				result = new LatLong(Double.parseDouble(f[1]), Double.parseDouble(f[0]));
			}
		return result;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getTriageId() {
		return triageId;
	}

	public void setTriageId(String triageId) {
		this.triageId = triageId;
	}

}
