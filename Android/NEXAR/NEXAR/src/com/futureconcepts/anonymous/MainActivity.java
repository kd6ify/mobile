package com.futureconcepts.anonymous;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;


import com.futureconcepts.database.DatabaseHelper;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		GridView gridview = (GridView) findViewById(R.id.categoryGrid);
		gridview.setOnItemClickListener((OnItemClickListener) new MyOnClickListener());
		gridview.setAdapter(new ButtonAdapter(this));
		
		       
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_choose_school, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
    {
	  Intent tables = new Intent (this,Create.class);
 
        switch (item.getItemId())
        {
        case R.id.menu_1:	           
            tables.putExtra("Table","AnonymousCategory");	        	
            startActivity(tables);
            break;
         case R.id.menu_2:	           
             tables.putExtra("Table","AnonymousInfo");	        	
             startActivity(tables);
             break;
        case R.id.schools:	           
           tables.putExtra("Table","Schools");	        	
           startActivity(tables);
           break;
        case R.id.schooltype:	           
            tables.putExtra("Table","SchoolTypes");	        	
            startActivity(tables);
            break;
        case R.id.states:	           
            tables.putExtra("Table","States");	        	
            startActivity(tables);
            break;
		
        }
        return false;
   }
	

	
	public void exit(View view)
	{
		finish();
	}
	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}
	
}
