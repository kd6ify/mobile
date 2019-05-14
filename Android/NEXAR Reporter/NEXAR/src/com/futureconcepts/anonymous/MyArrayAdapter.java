package com.futureconcepts.anonymous;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class MyArrayAdapter extends ArrayAdapter<DataList> {
	  public final Context context;
	  private final ArrayList<DataList> values;
	  private final  LayoutInflater li = LayoutInflater.from(this.getContext());
	  //private final  LayoutInflater li = null;
	  int ViewResourcesId;

	  public MyArrayAdapter(Context context, int textViewResourceId,ArrayList<DataList> values) {
	    super(context,textViewResourceId, values);
	    this.context = context;
	    this.values = values;
	    ViewResourcesId=textViewResourceId;
	  }
	  
	  @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
		  ViewHolder holder=null;
		    if(convertView==null){
		    	convertView=li.inflate( ViewResourcesId,parent,false);
		    	holder=new ViewHolder();
		    	holder.text=(TextView) convertView.findViewById(R.id.labelOptions);
		    	convertView.setTag(holder);
		    }else{
		    	holder=(ViewHolder)convertView.getTag();
		   }

		    holder.text.setText(values.get(position).toString());
		    //convertView.setTag(R.id.label, holder.text);
	        return convertView;
	    }
	  
	  static class ViewHolder {
		 TextView text;
		 // CheckBox check;

		}

	
}
