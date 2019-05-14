package com.futureconcepts.ax.trinity.logs.images;

import java.util.ArrayList;

import com.futureconcepts.ax.trinity.R;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


	 public class ImageAdapter extends BaseAdapter {
		    private  Context mContext;
			private ArrayList<EntryImageObject> images= new ArrayList<EntryImageObject>();		    
						
			public ImageAdapter(Context c, ArrayList<EntryImageObject> images2) {
		      mContext = c;
		      if(images2!=null){
		    	  images =  images2;
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
		        imageView.setLayoutParams(new GridView.LayoutParams(80, 80));
		        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		        imageView.setPadding(0, 0, 0, 0);
		      } else {
		        imageView = (ImageView) convertView;
		      }
		      imageView.setBackgroundColor(Color.DKGRAY);	
		      if(images.get(position).needDownload==EntryImageObjectManager.NO_NEED_DOWNLOAD)
		    	  imageView.setImageBitmap(GetImageBitmap.lessResolution(images.get(position).getImagePath(),50,50));
		      else
				{
		    	  imageView.setBackgroundResource( R.drawable.image_icon);
				}
		      return imageView;
		    }		   
		  }

