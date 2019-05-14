package com.futureconcepts.anonymous;

import java.util.ArrayList;

import com.futureconcepts.dbcommunication.GetSchools;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentList extends ListActivity implements OnClickListener, OnItemClickListener {
	private ListView states;
	private String listName;
	private TextView Header, NoItems;
	ArrayList<DataList> States=new ArrayList<DataList>();
    ArrayList<DataList> arr_sort= new ArrayList<DataList>();
   
    EditText ed;
    String StateID="";
    String SchoolTypeID="";
    int textlength=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Header=new TextView(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_statets_list);		
	
	    states=(ListView)findViewById(android.R.id.list);
	    states.setOnItemClickListener(this);
	    ed=(EditText)findViewById(R.id.search);
	    NoItems=(TextView)findViewById(R.id.noItems);
	    Header=(TextView)findViewById(R.id.selectRegion);
	    Intent intent = getIntent();
	    listName = intent.getStringExtra("listName");
	    Header.setText("Select a "+listName);
		
	    new getSchools().execute();
    	}
	//cancel button
	
		public void cancel(View v) {
			Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			};

		
	
	// get the selected dropdown list value
		public void getData() {
								
		try{
				
			if(States.size()>0){
				NoItems.setVisibility(View.GONE);
  			  	MyArrayAdapter dataAdapterState= new MyArrayAdapter(CurrentList.this,R.layout.simplerow,States);
		        states.setAdapter(dataAdapterState);
				ed.addTextChangedListener(new TextWatcher() {
              
				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}

					public void onTextChanged(CharSequence s, int start, int before,
						int count) {					
						textlength=ed.getText().length();
						arr_sort.clear();
						Log.d("seaerch","ed"+ed);
						for(int i=0;i<States.size();i++)
							{
								String lt=(States.get(i).toString());
								Log.d("seaerch","lt"+lt);
				
								if(textlength<=lt.length())
								{
									if(ed.getText().toString().equalsIgnoreCase((String) States.get(i).toString().subSequence(0, textlength)))	
									{
										Log.d("seaerch","add"+States.get(i));
										arr_sort.add(States.get(i));
									}
								}
							}
						MyArrayAdapter sortDataAdapter= new MyArrayAdapter(CurrentList.this,R.layout.simplerow,arr_sort);
						states.setAdapter(sortDataAdapter);
						}
					});	       
				}
			}catch (Throwable t) {
		    	 Log.d("error","error");
		    }

		}
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
		
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long arg3) {

				String Id = ((DataList)parent.getItemAtPosition(position)).getID();
				String name = ((DataList)parent.getItemAtPosition(position)).toString();
				Boolean selected = ((DataList)parent.getItemAtPosition(position)).isSelected();
				Log.d("testing","ID:"+Id+"name"+name+"selected  "+selected);
                
				saveSelection(Id,name);
				 Intent returnIntent = new Intent();
				 setResult(RESULT_OK,returnIntent); 
				finish();				
				
		}
		
		
		public void saveSelection(String ID, String Name){
			SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);	
            SharedPreferences.Editor editor = settings.edit();            
            editor.putString("IDSchool",ID);
            editor.putString("NameSchool",Name); 
            // Commit the edits!
            editor.commit(); 
		}
		
		public class getSchools extends AsyncTask<Object, Integer, Object> { 
			ProgressDialog dialog;
			@Override
			protected void onPreExecute()
			{
				dialog = new ProgressDialog(CurrentList.this);
				dialog.setTitle("Loading Schools");
				dialog.setCancelable(false);
				dialog.show();
				//dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				//dialog.setMax(100);
            }	
			
			@Override
			protected Object doInBackground(Object... params) {
				try { 
						GetSchools gs = new GetSchools(CurrentList.this);
						States = gs.doInBackground();
						Log.d("","States is returning:  "+States.size());
  	                } catch (Exception e) {
} 	
        			return null;
            }
        	   		  
			@Override
			protected void onPostExecute (Object result){
				if(dialog!=null)
				{
					dialog.dismiss();	
				}
				if(States!=null)
				{
					
					getData();
					
				}else
				{					
					NoItems.setVisibility(View.VISIBLE);
					Toast.makeText(getApplicationContext(), "Cannot connect to server.", Toast.LENGTH_SHORT).show();
				}        			
			}	
			
		}
}
