package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.trinity.R;

public class NotesLayer {
	public int defaultNotesIcon = R.drawable.notes;
	private Bitmap bubble;
	
	public NotesLayer(LruOSMIconsCache iconsCache,Context context){
		AndroidBitmap bitmap=(AndroidBitmap) AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(defaultNotesIcon));
		bitmap.scaleTo(48, 48);
		iconsCache.addIconToCache(String.valueOf(defaultNotesIcon), bitmap);
	}
	
	public void displayAllNotes(Address notes, LruOSMIconsCache iconsCache, final Context context, Layers layer)
	{
		Log.e("asdads", "this is teh Address count:    == "+notes.getCount());
//		if (notes.getCount()>0)
//		{			
//			notes.moveToFirst();
//			do
//			{
				//Log.e("asdads", "Addres Type:     == "+notes.getTypeID());
		
		
			AndroidBitmap bitmap = iconsCache.getIconFromCache(String.valueOf(defaultNotesIcon));
			bitmap.incrementRefCount();
			
			LatLong dummy = new LatLong(34.052234,-118.243685);//getGeoPointFromWKT(notes);
			NotesMarker note = new NotesMarker(dummy,bitmap,0,0 ){
				@Override
				public boolean onTap(LatLong tapLatLong, Point layerXY,	Point tapXY) {
					if(contains(layerXY, tapXY))
					{
						displayNoteDescription(context, this);
						return true;
					}
					return false;
				}
			};
			note.setDescrption("");
			layer.add(note);				
//			}while(notes.moveToNext());
//		}
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
	
	private void displayNoteDescription(Context context, NotesMarker note) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.note_details_alert_layout);
		dialog.setCancelable(true);
		((TextView) dialog.findViewById(R.id.note_alert_title)).setText("Note Details");
		((TextView) dialog.findViewById(R.id.note_description)).setText(note.getDescrption());
		((Button) dialog.findViewById(R.id.note_alert_button)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}
	
	private class NotesMarker extends BaseClassMarker
	{
		private String Description;
		
		public void setDescrption(String description)
		{
			this.Description = description;
		}
		public String getDescrption()
		{
			return this.Description;
		}
		
		public NotesMarker(LatLong latLong, Bitmap bitmap,
				int horizontalOffset, int verticalOffset) {
			super(latLong, bitmap, horizontalOffset, verticalOffset);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
			// TODO Auto-generated method stub
			if(contains(layerXY, tapXY))
			{
				return true;
			}
			return false;
		}
		
	}

}
