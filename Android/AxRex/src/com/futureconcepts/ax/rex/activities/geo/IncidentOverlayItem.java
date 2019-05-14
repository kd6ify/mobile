package com.futureconcepts.ax.rex.activities.geo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.futureconcepts.ax.rex.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class IncidentOverlayItem extends OverlayItem
{
	public IncidentOverlayItem(Context context, GeoPoint geoPoint)
	{
		super(geoPoint, "mark", "mark");
		Drawable drawable = context.getResources().getDrawable(R.drawable.placemark);
		setMarker(drawable);
		setState(drawable, 1);
	}
}
