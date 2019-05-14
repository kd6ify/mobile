package com.futureconcepts.customclass;

import com.futureconcepts.database.DatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Purpose: Detect if the user has Internet connection.
 **/

public class NetworkHandler {	
	static NetworkInfo netInfo;
	
	/**
	 * 
	 * @param context: context of the activity.
	 * @return true: if has connection to Internet and is connected.
	 */
	public static  boolean isNetworkAvailable(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) 
	      context.getSystemService(Context.CONNECTIVITY_SERVICE);
	     netInfo = cm.getActiveNetworkInfo();
	    // if no network is available networkInfo will be null
	    // otherwise check if we are connected
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * 
	 * @param context: context of the activity.
	 * @return 0: If there is no Internet access.
	 * @return 1: can send information to server.
	 * @return 2: If they are trying to send more Mb than allowed  over mobile data.
	 */
	public static int sendInformationToServer(Context context)
	{
		int noInternetAccess = 0;
		int send = 1;
		int needWifi = 2;
		double dataToSend = getFilesTotalSize(context);
		if(isNetworkAvailable(context))
		{			
			int netType = netInfo.getType();		
			if (netType == ConnectivityManager.TYPE_WIFI && netInfo.isConnected())
			{
				return send;
			}else if(netType == ConnectivityManager.TYPE_MOBILE && netInfo.isConnected())
			{
				Log.d("NetworkHandler", "singleto size is:  "+ SingletonInformation.getInstance().dataSizeSend);
				if(dataToSend>SingletonInformation.getInstance().dataSizeSend)
				{
					return needWifi;
				}else
				{
					return send;
				}
			}else
			{
				return noInternetAccess;
			}		
		}else
		{
			return noInternetAccess;
		}
				
	}
	
	/**
	 * 
	 * @param context: Context of the activity.
	 * @return Total size of the files to send.
	 */
	 private static double getFilesTotalSize(Context context)
	  {
		 DatabaseHelper dbHelper = new DatabaseHelper(context.getApplicationContext());
		 SQLiteDatabase	newDB = dbHelper.getWritableDatabase();
		  double filesSize = 0;
		  Cursor US = newDB.rawQuery("Select * from Media",null);
			 if (US.getCount()!=0) 		    
				{
		    	 US.moveToFirst();				    	 
				do{ double size = Double.parseDouble(US.getString(US.getColumnIndex("Size")));
				filesSize =filesSize+((size/1024)/1024);
				//Log.d("AnonymousActivity","filesSize so far: "+filesSize);						
			}while(US.moveToNext());
		}
			 US.close();
			 Log.w("NetworkHnadlre", "Sending: "+filesSize+"  Mb");
			 return filesSize;
	  }
	

}
