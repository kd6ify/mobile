package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.OverlayItem;

public class TacticOverlayItem extends OverlayItem
{
	public TacticOverlayItem(Context context, Tactic tactic)
	{
//		super(tactic.getGeoPoint(), "name", "desc");
		super(null, "name", "desc");
		Drawable drawable = context.getResources().getDrawable(R.drawable.blue_marker_t); 
		setMarker(drawable);
		setState(drawable, 1);
	}
}
