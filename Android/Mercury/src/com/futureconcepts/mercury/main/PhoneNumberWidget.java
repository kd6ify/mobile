package com.futureconcepts.mercury.main;

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

public class PhoneNumberWidget extends AppWidgetProvider
{
	private static String TAG = "trinity.PhoneNumberWidget";

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
		private Config mConfig = null;
		
		@Override
		public void onStart(Intent intent, int startId)
		{
			if (mConfig == null)
			{
				mConfig = Config.getInstance(this);
			}
			RemoteViews views = buildUpdate();
			ComponentName thisWidget = new ComponentName(this, PhoneNumberWidget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, views);
		}

		public static void start(Context context)
		{
			context.startService(new Intent(context, UpdateService.class));
		}

		private RemoteViews buildUpdate()
		{
			RemoteViews views = new RemoteViews(getPackageName(), R.layout.phone_number_widget);
			Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
			ComponentName componentName = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
			launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			launcherIntent.setComponent(componentName);
			PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, launcherIntent, 0);
			views.setOnClickPendingIntent(R.id.phone_number_widget_main, pendingIntent1);
			String number = mConfig.getPhoneNumber();
			if (number != null)
			{
				if (number.length() == 10)
				{
					String areaCode = number.substring(0, 3);
					String prefix = number.substring(3, 6);
					String suffix = number.substring(6, 10);
					views.setTextViewText(R.id.phone_number, "(" + areaCode + ") " + prefix + "-" + suffix);
				}
				else
				{
					views.setTextViewText(R.id.phone_number, number);
				}
			}
			return views;
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
