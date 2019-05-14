package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

import com.futureconcepts.ax.trinity.Config;
import com.google.android.maps.OverlayItem;

public class EquipmentOverlayItem extends OverlayItem
{
	public EquipmentOverlayItem(Context context, EquipmentViewCursorByCheckIn cursor)
	{
		super(cursor.getGeoPointFromWKT(), cursor.getEquipmentName(), cursor.getEquipmentName());
		boolean markerFound = false;
		byte[] bytes = cursor.getEquipmentTypeIcon();
		if (bytes != null)
		{
			Drawable drawable = new BitmapDrawable(getScaledBitmap(context, bytes));
			setMarker(drawable);
			setState(drawable, 1);
			markerFound = true;
		}
		if (markerFound == false)
		{
			Drawable shapeDrawable = new ShapeDrawable(new OvalShape());
			setMarker(shapeDrawable);
			setState(shapeDrawable, 1);
		}
	}
			
	private Bitmap getScaledBitmap(Context context, byte[] bytes)
	{
		int newWidth = 48;
		int maxHeight = 48;
		Bitmap unscaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		int unscaledWidth = unscaledBitmap.getWidth();
		int unscaledHeight = unscaledBitmap.getHeight();
		int newHeight = unscaledHeight * newWidth / unscaledWidth;
		if (newHeight > maxHeight)
		{
			// Resize with height instead
			newWidth = unscaledBitmap.getWidth() * maxHeight / unscaledBitmap.getHeight();
			newHeight = maxHeight;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		int bump = Config.getMapIconSizeBump(context);
		int scaledWidth = (int)(newWidth * scale + 0.5f) + bump;
		int scaledHeight = (int)(newHeight * scale + 0.5f) + bump;
		return Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
	}
}
