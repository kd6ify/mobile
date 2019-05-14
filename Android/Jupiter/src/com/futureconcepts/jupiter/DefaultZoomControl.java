package com.futureconcepts.jupiter;

import com.futureconcepts.jupiter.map.MapComponent;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
//import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class DefaultZoomControl extends LinearLayout
{
	private static final String TAG = "DefaultZoomControl";
	
	private ImageButton _zoomOut;
	private ImageButton _zoomIn;

	private MapComponent _component;
	
	public DefaultZoomControl(Context context, AttributeSet attrs)
    {
	    super(context, attrs);
	    setOrientation(LinearLayout.VERTICAL);
	    setPadding(5, 5, 5, 5);
		_zoomOut = getImageButton(context, R.drawable.zoom_out_selector);
		_zoomOut.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_component != null)
				{
//					Log.d(TAG, Double.toString(_component.getScale()));
					_component.zoomBy(1.5d);
				}
			}
		});
		_zoomIn = getImageButton(context, R.drawable.zoom_in_selector);
		_zoomIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
				if (_component != null)
				{
//					Log.d(TAG, Double.toString(_component.getScale()));
					_component.zoomBy(.5d);
				}
            }
		});
		addView(_zoomIn, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(_zoomOut, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setVisibility(View.GONE);
    }
	
	public void setMapController(MapComponent component)
	{
		_component = component;
	}

	private ImageButton getImageButton(Context context, int resId)
	{
		ImageButton imageButton = new ImageButton(context);
//		imageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		imageButton.setPadding(0, 0, 0, 0);
		imageButton.setImageResource(resId);
		imageButton.setBackgroundColor(Color.TRANSPARENT);
		return imageButton;
	}
}
