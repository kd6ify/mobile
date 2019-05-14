package com.futureconcepts.customclass;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SchedulerNotifications {
	
	public void createScheduledNotification(Context con, long time)
	 {
	  // in milliseconds 480 = 8 Hours
	 // Retrieve alarm manager from the system
	 AlarmManager alarmManager = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
	 // Every scheduled intent needs a different ID, else it is just executed once
	 int id =10;// (int) System.currentTimeMillis();
	 long recurring = (time * 60000); 
	 // Prepare the intent which should be launched at the date
	 Intent intent = new Intent(con, Notifications.class);
	 // Prepare the pending intent
	 PendingIntent pendingIntent = PendingIntent.getBroadcast(con, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	 // Register the alert in the system. You have the option to define if the device has to wake up on the alert or not
	 alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),recurring, pendingIntent);
	 WakeLocker.release();
	 
	 }
	
	public void cancelAlarm(Context con){
		// Retrieve alarm manager from the system
		 AlarmManager alarmManager = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
		 int id = 10;//(int) System.currentTimeMillis();
		 Intent intent = new Intent(con, Notifications.class);
		 // Prepare the pending intent
		 PendingIntent pendingIntent = PendingIntent.getBroadcast(con, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager.cancel(pendingIntent);
	}
}
