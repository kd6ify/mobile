package com.futureconcepts.anonymous;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<DataList> {
	private final Context context;
	  private final ArrayList<DataList> values;
	  private final  LayoutInflater li = LayoutInflater.from(this.getContext());
	  //private final  LayoutInflater li = null;

	  public MyArrayAdapter(Context context, int textViewResourceId,ArrayList<DataList> values) {
	    super(context,textViewResourceId, values);
	    this.context = context;
	    this.values = values;
	  }
	  
	  @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
		  ViewHolder holder=null;
		 
		    if(convertView==null){
		    	convertView=li.inflate(R.layout.simplerow,parent,false);
		    	holder=new ViewHolder();
		    	holder.text=(CheckedTextView) convertView.findViewById(R.id.label);
		    	//holder.check=(CheckBox) convertView.findViewById(R.id.checkData);
		    	//holder.check.setTag(position);
		    	convertView.setTag(holder);
		    }else{
		    	holder=(ViewHolder)convertView.getTag();
		   }

		    holder.text.setText(values.get(position).toString());

		    //convertView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		   // holder.check.setChecked(values.get(position).isSelected());
		    
		  /*  holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		    	 @Override
                 public void onCheckedChanged(CompoundButton buttonView,
                         boolean isChecked) {
                     int getPosition = (Integer) buttonView.getTag();
                     values.get(getPosition).setSelected(buttonView.isChecked());
                     Log.d("testing","test check");
                 }
	        });*/
		    
		    convertView.setTag(R.id.label, holder.text);
	        //convertView.setTag(R.id.checkData, holder.check);
	        return convertView;
	    }
	  
	  static class ViewHolder {
		  CheckedTextView text;
		 // CheckBox check;

		}

	
}
