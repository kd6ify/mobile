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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        Intent intent = getIntent();
        tableName=intent.getStringExtra("Table");
        
        openAndQueryDatabase();        
        displayResultList();
        
        /*SQLiteDatabase db = Create.DatabaseHelper.getReadableDatabase();
        String query = yourLongQuery;
        Cursor c = db.rawQuery(query, null);
        YourActivity.startManagingCursor(c);
        c.setNotificationUri(YourActivity.getContentResolver(), YourContentProvider.CONTENT_URI);*/
            
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
    	    					String name = c.getString(c.getColumnIndex("Name"));
    	    					String details = c.getString(c.getColumnIndex("Details"));
    	    					String date = c.getString(c.getColumnIndex("Date"));
    	    					String type = c.getString(c.getColumnIndex("Type"));
    	    					String school = c.getString(c.getColumnIndex("School"));
    	    					results.add("ID: " + id +" -- " +" NAME: "+ name+" -- " +" DETAILS: "+ details+" -- " +" DATE: "+date+" -- "+"TYPE: "+type+"School"+school);
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
    				 }
    			}
    		}         
		} catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not create or open the database");
        } 
		
	}  
    	    
	
}
