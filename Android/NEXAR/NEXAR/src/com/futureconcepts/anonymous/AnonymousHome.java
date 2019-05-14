package com.futureconcepts.anonymous;

import com.futureconcepts.database.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AnonymousHome extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";
	private SQLiteDatabase newDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.anonymous_home);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		final boolean isFirstLaunch = settings.getBoolean("firstLaunch", true);

		if (isFirstLaunch == true) {
			launch();
			Intent ChooseSchool = new Intent(this,ChooseSchool.class);
			startActivity(ChooseSchool);
		}

		ListView listView = (ListView) findViewById(R.id.schoolFeed);
		String[] values = new String[] {
				"School will be closed Monday, February 18 for the observation of George Washington's birthday. ",
				"The drinking fountains located in the 400 quad are currently out of service.",
				"Students will have an early out day Tuesday, February 5 for a staff meeting.",
				"There will be an Assembly on Friday, February 1 for ASB elections." };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_layout, R.id.notice, values);
		listView.setAdapter(adapter);

	}
	public void launch()
	{
		 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);		         
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean("firstLaunch", false);
         // Commit the edits!
         editor.commit(); 
         
         //create database
   		DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
   		newDB = dbHelper.getWritableDatabase(); 
   		newDB.close();
   		Log.d("Main Activity","Database created");
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.anonymous_home, menu);
		return true;
	}

	public void report(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);

	}
	public void planner(View view) {
		Intent intent = new Intent(this, SimpleCalendarViewActivity.class);
		startActivity(intent);

	}
	
	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}
}
