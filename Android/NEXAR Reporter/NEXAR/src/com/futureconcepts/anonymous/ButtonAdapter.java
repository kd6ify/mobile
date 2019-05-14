package com.futureconcepts.anonymous;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class ButtonAdapter extends BaseAdapter {
    private Context mContext;

    public ButtonAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // new ImageViews
    public View getView(int position, View convertView, ViewGroup parent) {
        //ImageView ImageViews;
    	Button ImageViews;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	//ImageViews = new ImageView(mContext);
        	ImageViews = new Button(mContext);
        	ImageViews.setFocusable(false);
        	ImageViews.setClickable(false);
        	ImageViews.setLayoutParams(new GridView.LayoutParams(LayoutParams.FILL_PARENT, 110));
        	//ImageViews.setScaleType(ImageView.ScaleType.FIT_CENTER);        	
        	//ImageViews.setPadding(3, 3, 3, 3);
        } else {
        	//ImageViews = (ImageView) convertView;
        	ImageViews = (Button) convertView;
        	//ImageViews.setFocusable(false);
        	
        	//ImageViews.setFocusableInTouchMode(false);
        }

        //ImageViews.setImageResource(mThumbIds[position]);
        ImageViews.setBackgroundResource(R.drawable.button_selector_long);
        ImageViews.setText(mThumbIds[position]);
        ImageViews.setGravity(Gravity.CENTER);
        ImageViews.setTextColor(Color.WHITE);
        ImageViews.setTextSize(43);
        
        ImageViews.setTypeface(Typeface.DEFAULT_BOLD);
        return ImageViews;
    }

    // images
    private String[] mThumbIds = {
           "Weapons","Drugs",
            "Bullying","Violence",
           "Theft", "Safety",
            "Vandalism", "Threats",
            "Other", "Suggestions"
    };
}
