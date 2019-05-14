package com.futureconcepts.ax.trinity.logs.images;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public class ImagesNotification {
	
	private Context _context;
	private static final String TAG = ImagesNotification.class.getSimpleName();	
	private static final int IMAGE_SEND = 1000;
	private static int IMAGE_SEND_COUNTER=0;
	private static final int IMAGE_SEND_FAIL = 2001;
	private static int IMAGE_SEND_FAIL_COUNTER=0;
	private final int IMAGE_PROGRESS = 500;
	
	public ImagesNotification(Context context)
	{
		_context = context;
		//_notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
	}
	
//	public void notifyProgress_deprecated(String message, int position, int max)
//	{
//		Notification n = new Notification();
//
//		n.flags = Notification.FLAG_ONGOING_EVENT;
//		n.icon = android.R.drawable.stat_sys_download;
//		n.when = System.currentTimeMillis();
//		n.tickerText = message;
//
//		Intent intent = new Intent();
//		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
//		
//		RemoteViews views = new RemoteViews(_context.getPackageName(), R.layout.sync_progress_view);
//		views.setTextViewText(R.id.name, message);
//		if (max > 0)
//		{
//			StringBuilder status = new StringBuilder();
//			double percent = (double)position / (double)max * 100.0d;
//			status.append((int)Math.ceil(percent));
//			status.append("%   ");
//			status.append(position);
//			status.append(" / ");
//			status.append(max);
//			views.setTextViewText(R.id.status, status.toString());
//		}
//		views.setProgressBar(R.id.progress, (int)max, (int)position, false);
//		n.contentView = views;
//		n.contentIntent = pendingIntent;
//
//		_notificationManager.notify(ID_PROGRESS, n);
//	}
	
	public void cancelNotificationProgress()
	{
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.cancel(IMAGE_PROGRESS);
		//IMAGE_SEND_COUNTER = 0;
	}
	
	public void cancelAllNotification()
	{
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.cancelAll();
		//IMAGE_SEND_COUNTER = 0;
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
		nm.notify(IMAGE_PROGRESS, n);
	}
	
	public void notifyUploadSuccess(String message)
	{
		IMAGE_SEND_COUNTER++;
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_more;
		n.when = System.currentTimeMillis();
		n.tickerText = message;
		n.setLatestEventInfo(_context, "AX Trinity", message, null);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.notify(IMAGE_SEND+IMAGE_SEND_COUNTER, n);
	}

	public void notifyUploadFail(String message, String ID)
	{		
		IMAGE_SEND_FAIL_COUNTER++;
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_error;
		n.when = System.currentTimeMillis();
		n.tickerText = message;
		Intent intent = new Intent(_context, SendImageToServer.class);
		intent.putExtra("mediaID", ID);
		intent.setAction(Long.toString(System.currentTimeMillis()));
	    PendingIntent retryIntent = PendingIntent.getService(_context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);			
		n.setLatestEventInfo(_context, "Ax Trinity", message, retryIntent);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);		
		nm.notify(IMAGE_SEND_FAIL+IMAGE_SEND_FAIL_COUNTER, n);
		
	}

}
