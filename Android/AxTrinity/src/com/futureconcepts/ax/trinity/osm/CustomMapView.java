package com.futureconcepts.ax.trinity.osm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomMapView extends org.mapsforge.map.android.view.MapView {

	public CustomMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomMapView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		// TODO Auto-generated method stub
		if (!isClickable()) {
		      return false;
		    }
		 this.getMapZoomControls().onMapViewTouchEvent(motionEvent);
		 if (motionEvent.getPointerCount() >= 2) {
		     return false;
		    }		    
		  try{
			  return super.onTouchEvent(motionEvent);
			 }catch(Exception e){e.printStackTrace(); return false;}
	}
	


}
