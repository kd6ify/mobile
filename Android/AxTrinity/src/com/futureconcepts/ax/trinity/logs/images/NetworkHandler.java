package com.futureconcepts.ax.trinity.logs.images;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
}
