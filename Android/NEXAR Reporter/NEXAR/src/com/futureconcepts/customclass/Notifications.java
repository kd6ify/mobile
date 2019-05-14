package com.futureconcepts.customclass;

import com.futureconcepts.anonymous.PendingReports;
import com.futureconcepts.anonymous.R;
import com.futureconcepts.database.DatabaseHelper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Notifications extends BroadcastReceiver {
	 public static final String TAG = "Notifications";

	 @Override
	  public void onReceive(Context context, Intent paramIntent) {
		     Log.d(TAG, "test receiver 0");
            boolean pending= verifyData(context);
            if(pending){
            	WakeLocker.acquire(context);
		     NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		  	 
		  	 // Create a new intent which will be fired if you click on the notification
		  	 Intent intent = new Intent(context,PendingReports.class);

		  	 // Attach the intent to a pending intent
		  	 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		  	 
		  	 // Create the notification
		  	 Notification notification = new Notification(R.drawable.ic_launcher32x32 , "You have Pending Reports to send", System.currentTimeMillis());
		  	 // Hide the notification after its selected
		  	 notification.flags= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
		  	 notification.setLatestEventInfo(context, "You have Pending Reports to send", "Tap for review.",pendingIntent);
 		  	 // Fire the notification
		  	 notificationManager.notify(10, notification);
            }else{
            	SchedulerNotifications temp= new SchedulerNotifications(); temp.cancelAlarm(context);	
            }

	  }
	 
	 private boolean verifyData(Context con){
		 DatabaseHelper dbHelper = new DatabaseHelper(con);
		 SQLiteDatabase newDB = dbHelper.getWritableDatabase();  		
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
				return true;
			}else{
				return false;
			}
	 }
	 
		
}
