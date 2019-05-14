package com.futureconcepts.mercury.main;

import com.futureconcepts.mercury.tracker.ToggleActivity;
import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class StatusWidget extends AppWidgetProvider
{
	private static String TAG = StatusWidget.class.getSimpleName();

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		context.startService(new Intent(context, UpdateService.class));
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    final String action = intent.getAction();
	    if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action))
	    {
	    	Log.d(TAG, "onReceive ACTION_APPWIDGET_DELETED");
	        final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
	        {
	            this.onDeleted(context, new int[] { appWidgetId });
	        }
	    }
	    else
	    {
	        super.onReceive(context, intent);
	    }
	}
	
	public static class UpdateService extends Service
	{
		@Override
		public void onStart(Intent intent, int startId)
		{
			RemoteViews views = buildUpdate();
			ComponentName thisWidget = new ComponentName(this, StatusWidget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, views);
		}

		public static void start(Context context)
		{
			context.startService(new Intent(context, UpdateService.class));
		}

		private RemoteViews buildUpdate()
		{
			RemoteViews views = new RemoteViews(getPackageName(), R.layout.status_widget2);
			setOnClickHandlerMain(views);
			setOnClickHandlerTracker(views);
			return views;
		}
		
		private void setOnClickHandlerMain(RemoteViews views)
		{
			Intent launcherIntent = null;
			launcherIntent = new Intent(this, StatusWidgetDispatchActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launcherIntent, 0);
			views.setOnClickPendingIntent(R.id.status_widget_main, pendingIntent);
		}
		
		private void setOnClickHandlerTracker(RemoteViews views)
		{
			Intent trackerToggleIntent = new Intent(this, ToggleActivity.class);
			PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, trackerToggleIntent, 0);
			Config config = Config.getInstance(this);
			views.setOnClickPendingIntent(R.id.tracker_icon_panel, pendingIntent2);
			String name = config.getDeviceName();
			if (name == null)
			{
				name = config.getDeviceId();
			}
			views.setTextViewText(R.id.username, name);
//			views.setTextViewText(R.id.current_incident_name, config.getCurrentIncidentName());
			if (config.getTrackerEnabled())
			{
				views.setImageViewResource(R.id.tracker_status_icon, R.drawable.tracking_icon_lit);
			}
			else
			{
				views.setImageViewResource(R.id.tracker_status_icon, R.drawable.tracking_icon_dark);
			}
		}
		
		@Override
        public IBinder onBind(Intent intent)
        {
	        // TODO Auto-generated method stub
	        return null;
        }
		
		public static final class Receiver extends BroadcastReceiver
		{
			@Override
		    public void onReceive(Context context, Intent intent)
		    {
				start(context);
		    }
		}
	}
}
