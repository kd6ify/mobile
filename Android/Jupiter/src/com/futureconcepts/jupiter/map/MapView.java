package com.futureconcepts.jupiter.map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
//import android.util.Log;

import com.osa.android.droyd.map.DroydMapView;

public class MapView extends DroydMapView
{
	private long _downTime;
	
	public MapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	

//	@Override
//	protected void dispatchDraw(Canvas canvas)
//	{
//		Log.d(TAG, "dispatchDraw");
//		if (_config.isLayerEnabled(CoordinatesLayer.LAYER_NAME))
//		{
//			if (_mapActivity != null)
//			{
//				_mapActivity.updateCoords();
//			}
//		}
//		super.dispatchDraw(canvas);
//	}
}
