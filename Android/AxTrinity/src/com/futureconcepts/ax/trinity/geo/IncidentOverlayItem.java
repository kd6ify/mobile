package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentType;
import com.futureconcepts.ax.trinity.Config;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class IncidentOverlayItem extends OverlayItem
{
	public IncidentOverlayItem(Context context, Incident incident)
	{
		super(getGeoPointFromWKT(incident.getAddress(context)), getTitle(incident), getSnippet(incident));
		boolean markerFound = false;
		IncidentType type = incident.getType(context);
		if (type != null)
		{
			Icon icon = type.getIcon(context);
			if (icon != null)
			{
				if (icon != null)
				{
					Drawable drawable = new BitmapDrawable(getScaledBitmap(context, icon));
					setMarker(drawable);
					setState(drawable, 1);
					markerFound = true;
				}
			}
		}
		if (markerFound == false)
		{
			Drawable shapeDrawable = new ShapeDrawable(new OvalShape());
			setMarker(shapeDrawable);
			setState(shapeDrawable, 1);
		}
	}
	private static GeoPoint getGeoPointFromWKT(Address address)
	{
		GeoPoint result = null;
		if (address != null && address.getWKT() != null)
		{
			Point point = address.getWKTAsPoint();
			result = new GeoPoint(point.y, point.x);
		}
		else
		{
			result = new GeoPoint(0,0);
		}
		return result;
	}
	private static String getTitle(Incident incident)
	{
		return incident.getName();
	}
	private static String getSnippet(Incident incident)
	{
		return null;
	}
	private Bitmap getScaledBitmap(Context context, Icon icon)
	{
		int newWidth = 48;
		int maxHeight = 48;
		byte[] bytes = icon.getImage();
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
