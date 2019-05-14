package com.futureconcepts.anonymous;

import android.content.Context;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
        return 0;
    }

    public long getItemId(int position) {
        return 0;
    }

    // new ImageViews
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView ImageViews;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	ImageViews = new ImageView(mContext);
        	ImageViews.setFocusable(false);
        	ImageViews.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	ImageViews.setScaleType(ImageView.ScaleType.FIT_CENTER);        	
        	ImageViews.setPadding(3, 3, 3, 3);
        } else {
        	ImageViews = (ImageView) convertView;
        	ImageViews.setFocusable(false);
        }

        ImageViews.setImageResource(mThumbIds[position]);
        return ImageViews;
    }

    // images
    private Integer[] mThumbIds = {
            R.drawable.weapons, R.drawable.drugs,
            R.drawable.bullying, R.drawable.violence,
            R.drawable.theft, R.drawable.safety,
            R.drawable.vandalism, R.drawable.threats,
            R.drawable.other, R.drawable.suggestions,
    };
}
