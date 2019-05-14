package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class TriageOverlayItem extends OverlayItem
{
	private BitmapDrawable _marker;
	
	public TriageOverlayItem(Context context, Triage triage)
	{
		super(getGeoPoint(context, triage), triage.getTrackingID(), triage.getTrackingID());
		if (triage.getColorID().equals(TriageColor.GREEN))
		{
			_marker = new BitmapDrawable(getScaledBitmap(context, R.drawable.triage_green_indicator_icon));
		}
		else if (triage.getColorID().equals(TriageColor.RED))
		{
			_marker = new BitmapDrawable(getScaledBitmap(context, R.drawable.triage_red_indicator_icon));
		}
		else if (triage.getColorID().equals(TriageColor.YELLOW))
		{
			_marker = new BitmapDrawable(getScaledBitmap(context, R.drawable.triage_yellow_indicator_icon));
		}
		else if (triage.getColorID().equals(TriageColor.BLACK))
		{
			_marker = new BitmapDrawable(getScaledBitmap(context, R.drawable.triage_black_indicator_icon));
		}
		setMarker(_marker);
		setState(_marker, 1);
	}
	
	private static GeoPoint getGeoPoint(Context context, Triage triage)
	{
		GeoPoint result = null;
		Address address = triage.getAddress(context);
		if (address != null)
		{
			Point point = address.getWKTAsPoint();
			if (point != null)
			{
				result = new GeoPoint(point.y, point.x);
			}
		}
		if (result == null)
		{
			result = new GeoPoint(0, 0);
		}
		return result;
	}
	
	private Bitmap getScaledBitmap(Context context, int resid)
	{
		int newWidth = 48;
		int maxHeight = 48;
		Bitmap unscaledBitmap = BitmapFactory.decodeResource(context.getResources(), resid);
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
