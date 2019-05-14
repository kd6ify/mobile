package com.futureconcepts.customclass;

import java.util.LinkedList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


	 public class ImageAdapter extends BaseAdapter {
		    public  Context mContext;
			public LinkedList<Bitmap> images= new LinkedList<Bitmap>();
		    
						
			public ImageAdapter(Context c, LinkedList<Bitmap> arrayImages) {
		      mContext = c;
		      if(arrayImages!=null){
		    	  images =  arrayImages;
		      }
		    }

		    public int getCount() {
		      return  images.size();
		    }

		    public Object getItem(int position) {
		      return position;
		    }

		    public long getItemId(int position) {
		      return position;
		    }
			
		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		      ImageView imageView;
		      if (convertView == null) {
		        imageView = new ImageView(mContext);
		        imageView.setLayoutParams(new GridView.LayoutParams(110, 110));
		        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		        imageView.setPadding(0, 0, 0, 0);
		      } else {
		        imageView = (ImageView) convertView;
		      }
		      imageView.setBackgroundColor(Color.DKGRAY);   
		      imageView.setImageBitmap( images.get(position));

		      return imageView;
		    }



		   
		  }

