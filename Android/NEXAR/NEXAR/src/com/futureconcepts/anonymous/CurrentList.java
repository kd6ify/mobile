package com.futureconcepts.anonymous;

import java.util.ArrayList;
import java.util.LinkedList;


import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CurrentList extends ListActivity implements OnClickListener, OnItemClickListener {
	private SQLiteDatabase newDB;
	private ListView states;
	private String listName;
	ArrayList<DataList> States=new ArrayList<DataList>();
    ArrayList<DataList> arr_sort= new ArrayList<DataList>();
    EditText ed;
    int textlength=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statets_list);
	    states=(ListView)findViewById(android.R.id.list);
	    states.setOnItemClickListener(this);
	    ed=(EditText)findViewById(R.id.search);
	    Intent intent = getIntent();
	    listName = intent.getStringExtra("listName");
	    getData();
    	}
	// get the selected dropdown list value
		public void getData() {
			
			newDB = SQLiteDatabase.openDatabase("/data/data/com.futureconcepts.anonymous/databases/anonymous.db",null,SQLiteDatabase.CONFLICT_NONE);
			Cursor CS=null;
			//ArrayList<DataList> States = new ArrayList<DataList>();
			//spinner1.setOnItemSelectedListener((OnItemSelectedListener) this);
			try{
			//Spinner1States
			if(listName.equals("states")){
			CS = newDB.rawQuery("Select ID,Name from States",null);
			}else if(listName.equals("schoolType")){
			CS = newDB.rawQuery("Select ID,Name from SchoolTypes",null);
			}else if(listName.equals("schoolName")){
			CS = newDB.rawQuery("Select ID,Name from Schools",null);
			}
			CS.moveToFirst();
			do{ 				
				States.add(new DataList(CS.getString(CS.getColumnIndex("ID")),CS.getString(CS.getColumnIndex("Name"))));
			}while(CS.moveToNext());
			
			 
			MyArrayAdapter dataAdapterState= new MyArrayAdapter(this,R.layout.simplerow,States);
			states.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        states.setAdapter(dataAdapterState);
			/*****************************/
			
			ed.addTextChangedListener(new TextWatcher() {
              
				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
				}

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
				//lv1.setAdapter(new ArrayAdapter<String>(searchsort.this,android.R.layout.simple_list_item_1 , arr_sort));
				}
				});
				
			
			/*******************************/
 
	        CS.close();
	        newDB.close();
			}catch (Throwable t) {
		    	 Log.d("error","error");
		    }

		}
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			Log.d("in","in the method1");
			
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long arg3) {
			boolean flag=false;
			CheckedTextView check = (CheckedTextView) v.getTag(R.id.label);
			check.setChecked(!check.isChecked());
			if(check.isChecked()){
				((DataList)parent.getItemAtPosition(position)).setSelected(true);	
			}else if(!check.isChecked()){
				((DataList)parent.getItemAtPosition(position)).setSelected(false);
			}
				String Id = ((DataList)parent.getItemAtPosition(position)).getID();
				String name = ((DataList)parent.getItemAtPosition(position)).toString();
				Boolean selected = ((DataList)parent.getItemAtPosition(position)).isSelected();
				Log.d("testing","name"+name+"selected  "+selected);
                
				saveSelection(Id,name);
				 Intent returnIntent = new Intent();
				 setResult(RESULT_OK,returnIntent); 
				finish();
				
				
		}
              public void saveSelection(String ID, String Name){
            	  
            	  SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);	
                  SharedPreferences.Editor editor = settings.edit();
                  
                  if(listName.equals("states")){
                  editor.putString("IDState",ID);
                  editor.putString("StateName",Name);
                  }else if(listName.equals("schoolType")){
                	  editor.putString("IDSchoolType",ID);
                      editor.putString("NameSchoolType",Name); 
                  }else if(listName.equals("schoolName")){
                	  editor.putString("IDSchool",ID);
                      editor.putString("NameSchool",Name); 
                }
                  // Commit the edits!
                  editor.commit(); 
              }
}
