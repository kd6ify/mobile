package com.futureconcepts.anonymous;


import java.util.ArrayList;

import com.futureconcepts.database.DatabaseHelper;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Create extends ListActivity {	
	
	private ArrayList<String> results = new ArrayList<String>();
	private String tableName;
	private SQLiteDatabase newDB;
	int currentVersion;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        Intent intent = getIntent();
        tableName=intent.getStringExtra("Table");
        
        openAndQueryDatabase();        
        displayResultList();
            
    }
	private void displayResultList() {
		TextView tView = new TextView(this);
        tView.setText("This data is retrieved from the database Table "+tableName);
        getListView().addHeaderView(tView);
        
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, results));
        getListView().setTextFilterEnabled(true);
		
	}
	private void openAndQueryDatabase() {
		try {
			
			DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
			newDB = dbHelper.getWritableDatabase();
			Cursor c = newDB.rawQuery("SELECT * FROM "+tableName, null);
			if (c != null ) {
    			if  (c.moveToFirst()) {
    				 if(tableName.matches("AnonymousCategory"))
    				 {
    					  Log.d("entre","entre");
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					results.add("ID: " + id +" -- " +" TYPE: "+type+" --");
    	    				}while (c.moveToNext()); 

    				 }else if(tableName.matches("AnonymousInfo"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String details = c.getString(c.getColumnIndex("Details"));
    	    					String date = c.getString(c.getColumnIndex("Date"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					String school = c.getString(c.getColumnIndex("School"));
    	    					String studentID = c.getString(c.getColumnIndex("StudentID"));
    	    					String deviceID = c.getString(c.getColumnIndex("DeviceID"));
    	    					results.add("ID: " + id +" -- "+" DETAILS: "+ details+" -- " +" DATE: "+date+" -- "+"TYPE: "+type+"   School: "+school+" -- StudentID:  "+studentID+"  -- DeviceID: "+deviceID);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("Schools"))
    				 {
    					 Log.d("entre","al metodo"+tableName);
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					String state = c.getString(c.getColumnIndex("State"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name+" -- " +" States: "+ state+" -- " +"TYPE: "+type);
    	    				}while (c.moveToNext()); 
    					 Log.d("entre","schools termine");
    				 }else if(tableName.matches("SchoolTypes"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name);
    	    				}while (c.moveToNext()); 
    				 }
    				 else if(tableName.matches("States"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("Media"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					String file = c.getString(c.getColumnIndex("File"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					String size = c.getString(c.getColumnIndex("Size"));
    	    					String Status = c.getString(c.getColumnIndex("Status"));
    	    					String PartsSent = c.getString(c.getColumnIndex("PartsSent"));
    	    					String Date = c.getString(c.getColumnIndex("Date"));
    	    					
    	    					
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name+" -- " +" FILE: "+ file+" -- " +" TYPE: "+type+" -- "+"SIZE: "+size+ "Status"+Status+"Parts sent "+PartsSent+ "Date: "+Date);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("MediaType"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("ID"));
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("AnonymousInfoMedia"))
    				 {
    					 do {
    	    					String id = c.getString(c.getColumnIndex("NEXARInfo"));
    	    					String media = c.getString(c.getColumnIndex("Media"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					results.add("ANONYMOUSINFO: " + id +" -- " +" MEDIA: "+ media+ "Type:"+type);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("SchoolFeed"))
    				 {
    					 do {
    	    					String ID = c.getString(c.getColumnIndex("ID"));
    	    					String Details = c.getString(c.getColumnIndex("Details"));
    	    					String Date = c.getString(c.getColumnIndex("Date"));
    	    					String Time = c.getString(c.getColumnIndex("Time"));
    	    					results.add("ID: " + ID +" -- "+"Details:"+Details+"--"+" Date: "+ Date+"--"+"Time:"+Time);
    	    				}while (c.moveToNext()); 
    				 }else if(tableName.matches("TableStatus"))
    				 {
    					 do {
    	    					String ID = c.getString(c.getColumnIndex("ID"));
    	    					String NexarInfo = c.getString(c.getColumnIndex("NexarInfoTable"));
    	    					String NexarInfoMedia = c.getString(c.getColumnIndex("NexarInfoMediaTable"));
    	    					String Media = c.getString(c.getColumnIndex("MediaTable"));
    	    					results.add("ID: " + ID +" -- "+"Status NEXARInfo:"+NexarInfo+"--"+" Status NEXARInfoMedia "+ NexarInfoMedia+"--"+" Status Media:"+Media);
    	    				}while (c.moveToNext()); 
    				 }
    				 c.close();
    				 newDB.close();
    			}
    		}         
		} catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not create or open the database");
        } 
		
	}  
    	    
	
}
