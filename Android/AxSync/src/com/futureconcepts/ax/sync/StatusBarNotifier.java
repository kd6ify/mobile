package com.futureconcepts.ax.sync;

import com.futureconcepts.ax.sync.app.SqlDownloadErrorActivity;
import com.futureconcepts.ax.sync.app.SqlUploadErrorActivity;
import com.futureconcepts.ax.sync.app.SyncLogActivity;
import com.futureconcepts.ax.sync.config.Config;
import com.futureconcepts.gqueue.OnRetryException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class StatusBarNotifier
{
	private static final String TAG = StatusBarNotifier.class.getSimpleName();
	
	private static final int ID_SYNCING = 1;
	private static final int ID_UPLOAD_ERROR = 2;
	private static final int ID_PROGRESS = 3;
	private static final int ID_SYNCING_RESCHEDULED = 4;
	private static final int ID_DOWNLOAD_ERROR = 5;
			
	private Context _context;
	private NotificationManager _notificationManager;

	public StatusBarNotifier(Context context)
	{
		_context = context;
		_notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
	}
	
	public void notifyProgress_deprecated(String message, int position, int max)
	{
		Notification n = new Notification();

		n.flags = Notification.FLAG_ONGOING_EVENT;
		n.icon = android.R.drawable.stat_sys_download;
		n.when = System.currentTimeMillis();
		n.tickerText = message;

		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
		
		RemoteViews views = new RemoteViews(_context.getPackageName(), R.layout.sync_progress_view);
		views.setTextViewText(R.id.name, message);
		if (max > 0)
		{
			StringBuilder status = new StringBuilder();
			double percent = (double)position / (double)max * 100.0d;
			status.append((int)Math.ceil(percent));
			status.append("%   ");
			status.append(position);
			status.append(" / ");
			status.append(max);
			views.setTextViewText(R.id.status, status.toString());
		}
		views.setProgressBar(R.id.progress, (int)max, (int)position, false);
		n.contentView = views;
		n.contentIntent = pendingIntent;

		_notificationManager.notify(ID_PROGRESS, n);
	}

	public void notifyScheduledRetry(OnRetryException e)
	{
		Notification n = new Notification();

		n.flags = Notification.FLAG_ONGOING_EVENT;
		n.icon = android.R.drawable.stat_notify_error;
		n.when = System.currentTimeMillis();
		n.tickerText = "Synchronizer rescheduled";
		Config.setLastSyncException(_context, e);
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, new Intent(_context, SyncLogActivity.class), 0);
		n.setLatestEventInfo(_context, n.tickerText, e.getMessage(), pendingIntent);
		_notificationManager.notify(ID_SYNCING_RESCHEDULED, n);
	}

	public void notifyUploadError(Uri uri, String message)
	{
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_error;
		n.when = System.currentTimeMillis();
		n.tickerText = message;

		Intent intent = new Intent(_context, SqlUploadErrorActivity.class);
		intent.setData(uri);
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
		n.setLatestEventInfo(_context, "Upload Error", message, pendingIntent);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);
		nm.notify(ID_UPLOAD_ERROR, n);
	}

	public void notifyDownloadError(Uri uri, String message)
	{
		Notification n = new Notification();
		n.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		n.icon = android.R.drawable.stat_notify_error;
		n.when = System.currentTimeMillis();
		n.tickerText = message;

		Intent intent = new Intent(_context, SqlDownloadErrorActivity.class);
		intent.setData(uri);
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
		n.setLatestEventInfo(_context, "Download Error", message, pendingIntent);
		NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);
		nm.notify(ID_DOWNLOAD_ERROR, n);
	}
	
	public void notifySyncing()
	{
		_notificationManager.cancel(ID_SYNCING_RESCHEDULED);
		Config.setLastSyncException(_context, null);
		Notification n = new Notification();
		n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		n.icon = android.R.drawable.stat_notify_sync;
		n.tickerText = null;
		n.when = System.currentTimeMillis();
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, new Intent(_context, SyncLogActivity.class), 0);
		n.setLatestEventInfo(_context, "AntaresX Synchronizer", n.tickerText, pendingIntent);
		_notificationManager.notify(ID_SYNCING, n);
	}
	
	public void cancelSyncing()
	{
		_notificationManager.cancel(ID_SYNCING);
	}
}