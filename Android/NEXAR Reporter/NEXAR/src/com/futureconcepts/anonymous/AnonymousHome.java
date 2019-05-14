package com.futureconcepts.anonymous;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.futureconcepts.customclass.SingletonInformation;
import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.MediaTable;
import com.futureconcepts.database.TableStatus;
import com.futureconcepts.dbcommunication.GetAnnouncements;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AnonymousHome extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";
	private SQLiteDatabase newDB;
	//AdView adView;
	ArrayList<DataList> list;
	public boolean isFirstLaunch;
	//int NewVersion=1;
	//int OldVersion;
	int DBVersion; 
	ListView listView;	
	AlertDialog pendingAlert;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.anonymous_home);
		checkDevice();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		 isFirstLaunch = settings.getBoolean("firstLaunch",true);			
	  	 listView = (ListView) findViewById(R.id.schoolFeed); 
	  	 DBVersion = DatabaseHelper.DATABASE_VERSION;
	  	 //createAdd();
//		 Get Announcements.
//	 	 checkUpdates();
	  	 Log.d("","ON CREATTE=======");
	  	 //If first launch create DataBase.
		if (isFirstLaunch == true) {
	         launch();
			Intent ChooseSchool = new Intent(this,ChooseSchool.class);
			startActivity(ChooseSchool);
		}else{			
			int currentVersion = settings.getInt("DBVersion",0);
			if(currentVersion!=DBVersion){
				//Update the database
				DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
		   		newDB = dbHelper.getWritableDatabase();		   		
		   		newDB.close();
		   	 SharedPreferences.Editor editor = settings.edit();
		   	 editor.putInt("DBVersion",DBVersion);
	         editor.commit();          
			}
			boolean haveKey = settings.getBoolean("isKeyValid", false);
			if(!haveKey)
			{
				Intent ChooseSchool = new Intent(this,ChooseSchool.class);
				startActivity(ChooseSchool);
			}
		}
		//Get the current context to display the AlertDialog for not send it reports.
		SingletonInformation.getInstance().currentCon=this;
	}
	
//	 @Override
//	  public void onDestroy() {
//	    if (adView != null) {
//	      adView.destroy();
//	    }
//	    super.onDestroy();
//	  }

//	private void createAdd()
//	{	 // Crear la adView
//		 // Buscar la AdView como un recurso y cargar una solicitud.
//	    adView = (AdView)this.findViewById(R.id.ad);
//	    adView.loadAd(new AdRequest());
//	   // adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
//	}

	 public void checkDevice(){
		TelephonyManager manager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE){
            //tablet
        }else{
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //mobile
        }
	}
	
	@Override
	protected void onResume() {
	  super.onResume();
	 if( SingletonInformation.getInstance().activityCategoryVisible||SingletonInformation.getInstance().activityPendingVisible
			 ||SingletonInformation.getInstance().activitySettingsVisible){
		 SingletonInformation.getInstance().activityCategoryVisible=false;
		 SingletonInformation.getInstance().activityPendingVisible=false;
		 SingletonInformation.getInstance().activitySettingsVisible=false;
	 }else{
		
		 pendingReports();
		 SingletonInformation.getInstance().activityCategoryVisible=false;
		 SingletonInformation.getInstance().activityPendingVisible=false;
		 SingletonInformation.getInstance().activitySettingsVisible=false;
	 }
//	 Get Announcements.
 	 checkUpdates();	
	}
	

	@Override
	public void onBackPressed() {
		SingletonInformation.getInstance().activityCategoryVisible=false;
	    SingletonInformation.getInstance().activityPendingVisible=false;
	    SingletonInformation.getInstance().activitySettingsVisible=false;	
	    finish();
	}

	/**
	 * @param pending: number of rows founded.
	 */
	private void pendingReports()
	{
		if(SingletonInformation.getInstance().reports.size()==0){ 
			DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
			newDB = dbHelper.getWritableDatabase();  		
			Cursor CAI = newDB.rawQuery("Select * from AnonymousInfo",null);
			Cursor CAIM = newDB.rawQuery("Select * from AnonymousInfoMedia",null);
			//AnonymousTable.onUpgrade(newDB, 6, 7);
			int pendingMedia = CAIM.getCount();
			int pendingData = CAI.getCount();
			CAIM.close();
			CAI.close();
			newDB.close();
			if(pendingMedia>0 || pendingData>0)
			{
				showDialog(this);
		 	}
		}
	}
	
	public void showDialog(Context context) {
        if( pendingAlert!= null && pendingAlert.isShowing() ) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(AnonymousHome.this);
    	builder.setTitle("You have pending reports.")
    		.setMessage("Pending Reports will be deleted 10 days after being stored. Would you like to check your Pending Reports?")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   //start pending reports activity
            	   Intent pendingReports = new Intent(AnonymousHome.this,PendingReports.class);
           		   startActivity(pendingReports);
               }
            })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   // User cancelled the dialog
            	   dialog.dismiss();
               }
           });

    	 pendingAlert=  builder.create();
    	 pendingAlert.show();
}
	
	/*
	 * Create Data Base.
	 * */
	public void launch()
	{
		 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);		         
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean("firstLaunch", false);
         editor.putInt("DBVersion",DBVersion);
         // Commit the edits!
         editor.commit(); 
         
         //create database
   		DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
   		newDB = dbHelper.getWritableDatabase();
   		//AnonymousTable.onUpgrade(newDB, 6, 7);
   		newDB.close();
   		Log.d("Main Activity","Database created");
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.anonymous_home, menu);
		getMenuInflater().inflate(R.menu.anonymous_homesad, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
    {
		 switch (item.getItemId())
	        {
	        case R.id.menu_settings:
	        	Intent settings = new Intent(this, ChooseSchool.class);
	        	startActivity(settings);
	        	break;
	        case R.id.menu_moreMB:
	        	    displayMbAlert();	        	
	        	break;
	        }
		return false;
		
    }
	
	/**
	 * Display an alert to increase Mb allowed to send.
	 * This is only for test.
	 */
	private void displayMbAlert()
	{
		String [] list = {"5","10","15","20","25","30"};
		for(int i=0;i<list.length;i++)
		{
			if(Integer.parseInt(list[i]) == SingletonInformation.getInstance().dataSizeSend)
			{
				list[i] = list[i]+ " Current Seleccion";
			}
			
		}		
		try{
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("Select Size( Mb )")
		           .setItems(list, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		               // The 'which' argument contains the index position
		               // of the selected items
		            	   SingletonInformation.getInstance().dataSizeSend = (which+1)*5;
		            	   dialog.dismiss();
		           }
		    });
		   AlertDialog dialog = builder.create();
		   dialog.show();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void report(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);

	}
	
	/**
	 * Display pending report activity if there is not report being sent.
	 * @param v: view that call this method.
	 */
	
	public void pendingReports(View v) {
		if(SingletonInformation.getInstance().reports.size()==0){ 
			Intent pendingReports = new Intent(this,PendingReports.class);
			startActivity(pendingReports);
		}else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(AnonymousHome.this);
        	builder.setTitle("Information")
        		.setMessage("We are sending your previous report please wait. You will receive a notification when we have finished sending the report.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //start pending reports activity
                	  dialog.dismiss();
                   }
                });
                       
        	AlertDialog alert =  builder.create();
        	alert.show();
		}
		
	}
	public void planner(View view) {
		Intent intent = new Intent(this, SimpleCalendarViewActivity.class);
		startActivity(intent);

	}
	
	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}

	/**
	 * Instanciate GetAnnouncements if there is internet connection.
	 * */
	@SuppressLint("SimpleDateFormat")
	public void checkUpdates(){
		 ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		 NetworkInfo connection= connManager.getActiveNetworkInfo();
		 int netType=-100;
		 if(connection!=null){
		  netType = connection.getType();
		 }
		 
		 if ((netType == ConnectivityManager.TYPE_WIFI || netType == ConnectivityManager.TYPE_MOBILE ) && connection.isConnected()) {
		         new GetAnnouncements(this,listView).execute();
				 }else{//Else verifies if already have announcements for the current week
					 
					 Calendar calendar = Calendar.getInstance();
					 Calendar calendarEnd = Calendar.getInstance();
					 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

					 int day = calendar.get(Calendar.DAY_OF_WEEK);

			        if(day==7 || day==1){//saturday and sunday
			   			calendarEnd.add(Calendar.DAY_OF_MONTH, (0));
			   		}else{
			   			calendarEnd.add(Calendar.DAY_OF_MONTH, (6-day));
			   			calendar.add(Calendar.DAY_OF_MONTH, (2-day));
			   		}
			   		String endUpdate = df.format(calendarEnd.getTime());
					String formattedDate = df.format(calendar.getTime());

			    	list=new ArrayList<DataList>();
			    	list.clear();
			    	DatabaseHelper dbHelper = new DatabaseHelper(this);
			    	newDB = dbHelper.getWritableDatabase(); 
			        Cursor CSF = newDB.rawQuery("Select * from SchoolFeed where Date BETWEEN"+"'"+formattedDate+"'"+"AND"+"'"+endUpdate+"'"+"ORDER BY Date ASC",null);
			    		
			    	if(CSF.getCount()!=0){
			    		CSF.moveToFirst();			
			    			do{ 
			    				Log.d("SchoolFeed","HAVE something");
			    				list.add(new DataList(CSF.getString(CSF.getColumnIndex("Details")),"Posted: "+CSF.getString(CSF.getColumnIndex("Date"))));
			    				Log.d("add","to list");
			    			}while(CSF.moveToNext());	
 
			    		}else{
							
			    			list.add(new DataList("No WIFI acces: You need a WIFI connection to update the Announcement.","")); 
			    		 }
			    	 ArrayAdapterAnnouncements adapter= new ArrayAdapterAnnouncements(this,R.layout.list_layout,list);
			    	 listView.setAdapter(adapter);
			    	 CSF.close();
				 }
			verifyOldData();
	}
	
	/**
	 * Verifies if there is pending data stored for more than 10 days.
	 * */
	@SuppressLint("SimpleDateFormat")
	public void verifyOldData(){

		LinkedList<DataList> oldData=new LinkedList<DataList>();
		 HashMap<String,String> tempData = new HashMap<String,String>();
		
		 Calendar calendar = Calendar.getInstance();
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		// String end = df.format(calendar.getTime());//current date
		 calendar.add(Calendar.DAY_OF_MONTH, ((10)*-1));
		 String start = df.format(calendar.getTime());//10 DAYS before
		 

		DatabaseHelper dbHelper = new DatabaseHelper(this);

    	newDB = dbHelper.getWritableDatabase(); 
        Cursor CSFA = newDB.rawQuery("Select * from AnonymousInfo where Date="+"'"+start+"'",null);
    		
    	if(CSFA.getCount()!=0){
    		CSFA.moveToFirst();			
    			do{ 
    				oldData.add(new DataList(CSFA.getString(CSFA.getColumnIndex("ID")),""));
    				//Log.d("value","ID "+CSF.getString(CSF.getColumnIndex("ID"))+"Date "+CSF.getString(CSF.getColumnIndex("Date")));
    			}while(CSFA.moveToNext());
    	    	deleteOldData(oldData,1);
    		}
    	CSFA.close();
    	
    	Cursor CNIM = newDB.rawQuery("Select * from AnonymousInfoMedia AS AIM INNER JOIN Media ON " +
    			"AIM.Media=Media.ID AND Media.Date="+"'"+start+"'",null);
    	if( CNIM.getCount()!=0){
    		 CNIM.moveToFirst();			
    			do{ 
    				oldData.add(new DataList(CNIM.getString(CNIM.getColumnIndex("NEXARInfo")),""));
    				tempData.put(CNIM.getString(CNIM.getColumnIndex("NEXARInfo")), "");
    				//Log.d("value","ID "+CSF.getString(CSF.getColumnIndex("ID"))+"Date "+CSF.getString(CSF.getColumnIndex("Date")));
    			}while(CNIM.moveToNext());	
    	    	deleteOldData(oldData,3);
    	    	deleteTableStatus(tempData);
    		}

    	CNIM.close();
    	
       	Cursor  CSF = newDB.rawQuery("Select * from Media where Date="+"'"+start+"'",null);
    	if(CSF.getCount()!=0){
    		CSF.moveToFirst();			
    			do{ 
    				oldData.add(new DataList(CSF.getString(CSF.getColumnIndex("ID")),""));
    				//Log.d("value","ID "+CSF.getString(CSF.getColumnIndex("ID"))+"Date "+CSF.getString(CSF.getColumnIndex("Date")));
    			}while(CSF.moveToNext());	
    	    	deleteOldData(oldData,2);
    		}

    	CSF.close();
    	newDB.close();
	}
	
	/**
	 * Delete all the pending data.
	 * */
	public void deleteOldData(LinkedList<DataList> data, int tables){
		 Log.d("deleteolddata","deletedata");
		if(tables==1){
			for(DataList t: data){
				getContentResolver().delete(AnonymousTable.CONTENT_URI,
						"ID=?", new String[] { t.ID });

				getContentResolver().delete(TableStatus.CONTENT_URI,
						"ID=?", new String[] { t.ID });
			}
			}else if(tables==2){
				for(DataList t: data){
					getContentResolver().delete(MediaTable.CONTENT_URI,
						"ID=?", new String[] { t.ID });
					getContentResolver().delete(MediaTable.CONTENT_URI,
					"ID=?", new String[] { t.ID });
				}
			
			}else if(tables==3){
				for(DataList t: data){
				getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI,
						"NEXARInfo=?", new String[] { t.ID });
			
				}
			}
	}
	
	public void deleteTableStatus(HashMap<String,String> data){
		DatabaseHelper dbHelper = new DatabaseHelper(this);

    	newDB = dbHelper.getWritableDatabase(); 
		
		Cursor CTS = newDB.rawQuery("Select * from TableStatus",null);
    	if( CTS.getCount()!=0){		
    		 for (String key :data.keySet()) {
    			 getContentResolver().delete(TableStatus.CONTENT_URI,
 						"ID=?", new String[] { key });
    			}
		
    	}
	}
    	
}
