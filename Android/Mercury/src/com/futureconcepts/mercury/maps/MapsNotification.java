package com.futureconcepts.mercury.maps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public class MapsNotification {
	
	private Context _context;
	private static final String TAG = MapsNotification.class.getSimpleName();	
	private static final int MAP_SEND = 1000;
	private static int MAP_SEND_COUNTER=0;
	private static final int MAP_SEND_FAIL = 2001;
	private static int MAP_SEND_FAIL_COUNTER=0;
	private final int MAP_PROGRESS = 500;
	
	public MapsNotification(Context context)
	{
		_context = context;
		//_notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
	}
	
	public void cancelNotificationProgress()
	{
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.cancel(MAP_PROGRESS);
		//MAP_SEND_COUNTER = 0;
	}
	
	public void notifyUpdateProgress(String title,String message)
	{
	
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_more;
		n.when = System.currentTimeMillis();
		n.tickerText = message;
		n.setLatestEventInfo(_context, title, message, null);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.notify(MAP_PROGRESS, n);
	}
	
	public void notifyDownloadSuccess(String message)
	{
		MAP_SEND_COUNTER++;
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.ic_menu_mapmode;
		n.when = System.currentTimeMillis();
		n.tickerText = message;
		n.setLatestEventInfo(_context, "Mercury", message, null);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.notify(MAP_SEND+MAP_SEND_COUNTER, n);
	}

	public void notifyDownloadFail(String message, String ID)
	{		
		MAP_SEND_FAIL_COUNTER++;
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_error;
		n.when = System.currentTimeMillis();
		n.tickerText = message;
		Intent intent = new Intent(_context, DownloadMapFileActivity.class);
		//intent.putExtra("mediaID", ID);
		intent.setAction(Long.toString(System.currentTimeMillis()));
	    PendingIntent retryIntent = PendingIntent.getService(_context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);			
		n.setLatestEventInfo(_context, "Mercury", message, retryIntent);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.notify(MAP_SEND_FAIL+MAP_SEND_FAIL_COUNTER, n);
		
	}

}
