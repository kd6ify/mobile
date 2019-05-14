package com.futureconcepts.mercury.update;

import java.io.File;

import com.futureconcepts.mercury.NotificationBaseID;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.download.Downloads;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DownloadCompleteReceiver extends BroadcastReceiver
{
	private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();
	
	private Context _context;
	private NotificationManager _notificationManager;
	
	@Override
    public void onReceive(Context context, Intent intent)
    {
		Log.d(TAG, "onReceive");
		_context = context;
		_notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
		Uri uri = intent.getData();
		if (uri != null)
		{
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null)
			{
				if (cursor.getCount() == 1)
				{
					cursor.moveToFirst();
					int id = cursor.getInt(cursor.getColumnIndex(Downloads.Impl._ID));
					int status = cursor.getInt(cursor.getColumnIndex(Downloads.Impl.STATUS));
					String filename = cursor.getString(cursor.getColumnIndex(Downloads.Impl._DATA));
					String title = cursor.getString(cursor.getColumnIndex(Downloads.Impl.TITLE));
					if (status == 200 && filename != null)
					{
						notifyInstallReady(id, filename, title);
					}
					else
					{
						notifyDownloadFailed(id, status, title, uri);
					}
				}
				cursor.close();
			}
		}
    }
	
	private void notifyInstallReady(int id, String filename, String title)
	{
		Notification n = new Notification();
		
		if (title == null)
		{
			title = filename;
		}
		
		n.flags = Notification.FLAG_AUTO_CANCEL;

		n.icon = android.R.drawable.stat_sys_download_done;
		n.when = System.currentTimeMillis();
		n.tickerText = title + " ready to install";

		Intent activityIntent = new Intent(Intent.ACTION_VIEW);
		activityIntent.setDataAndType(Uri.fromFile(new File(filename)), "application/vnd.android.package-archive");
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, activityIntent, 0);

		n.setLatestEventInfo(_context, "Download ready to install", title, pendingIntent);

		_notificationManager.notify(NotificationBaseID.DOWNLOAD + id, n);
	}
	
	private void notifyDownloadFailed(int id, int status, String title, Uri uri)
	{
		Notification n = new Notification();
				
		n.flags = Notification.FLAG_AUTO_CANCEL;

		n.icon = R.drawable.alert;
		n.when = System.currentTimeMillis();
		n.tickerText = "Download failed";

		Intent activityIntent = new Intent(Intent.ACTION_VIEW);
		activityIntent.setClass(_context, ViewDownloadStateActivity.class);
		activityIntent.setData(uri);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, activityIntent, 0);

		n.setLatestEventInfo(_context, "Download failed " + status, title, pendingIntent);

		_notificationManager.notify(NotificationBaseID.DOWNLOAD + id, n);
	}
}
