package com.futureconcepts.anonymous;

import com.futureconcepts.dbcommunication.sendInformation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;



public class NetworkReceiver extends BroadcastReceiver {
	 public static final String TAG = "NetworkReceiver";
	 public SQLiteDatabase newDB;
	  @Override
	  public void onReceive(Context context, Intent intent) {

	    boolean isNetworkDown = intent.getBooleanExtra(
	        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);  //
	   
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
       // NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(     ConnectivityManager.TYPE_MOBILE );
      

	    if (isNetworkDown) {
	      Log.d(TAG, "onReceive: NOT connected");
	      //context.stopService(new Intent(context, UpdaterService.class)); // 
	    } else {
	      Log.d(TAG, "onReceive: connected");
	      //context.startService(new Intent(context, UpdaterService.class)); // 
	      
	      if ( activeNetInfo.getTypeName().equals("WIFI"))
	        { 
	    	  sendInformation send=new sendInformation();
	    	  
	    	 Log.d(TAG, "onReceive: connected"+activeNetInfo.getTypeName());
	    	 String saveMedia = "http://172.16.21.187/TheObserver/saveMedia.php";
	    	 //Upload data after upload media
	    	 
	    	 newDB = SQLiteDatabase.openDatabase("/data/data/com.futureconcepts.anonymous/databases/anonymous.db",null,SQLiteDatabase.CONFLICT_NONE);
	    	 Cursor CA = newDB.rawQuery("Select * from AnonymousInfo",null);
	    	 
	    	 
	    	 try{
	    		  if(CA.getCount()!=0){
	    	    	 send.getValues("172.16.21.187","",context);
	    		 }
	    		  
	    	  }catch (Exception e) {
	    		 e.printStackTrace();
			    }
	    	 CA.close(); 
	    	  newDB.close();
	           
	        }

	    }
	  }
	 


}
