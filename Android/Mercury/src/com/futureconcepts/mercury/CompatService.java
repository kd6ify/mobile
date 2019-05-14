package com.futureconcepts.mercury;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class CompatService extends Service
{
	@SuppressWarnings("unchecked")
	private static final Class[] _startForegroundSignature = new Class[] { int.class, Notification.class };
	@SuppressWarnings("unchecked")
	private static final Class[] _stopForegroundSignature = new Class[] { boolean.class };

	private Method _startForeground;
	private Method _stopForeground;
	private Object[] _startForegroundArgs = new Object[2];
	private Object[] _stopForegroundArgs = new Object[1];
	private NotificationManager _notificationManager;
						
	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void startForegroundCompat(int id, Notification notification)
	{
		 try
		 {
	        _startForeground = getClass().getMethod("startForeground", _startForegroundSignature);
	        _stopForeground = getClass().getMethod("stopForeground", _stopForegroundSignature);
		 }
		 catch (NoSuchMethodException e)
		 {
	        // Running on an older platform.
	        _startForeground = null;
	        _stopForeground = null;
		 }
	    // If we have the new startForeground API, then use it.
	    if (_startForeground != null)
	    {
	        _startForegroundArgs[0] = Integer.valueOf(id);
	        _startForegroundArgs[1] = notification;
	        try
	        {
	            _startForeground.invoke(this, _startForegroundArgs);
	        }
	        catch (InvocationTargetException e)
	        {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        }
	        catch (IllegalAccessException e)
	        {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.
	    setForeground(true);
	    getNotificationManager().notify(id, notification);
	}

	protected void updateNotification(int id, Notification notification)
	{
		getNotificationManager().notify(id, notification);
	}
	
	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void stopForegroundCompat(int id)
	{
	    // If we have the new stopForeground API, then use it.
	    if (_stopForeground != null)
	    {
	        _stopForegroundArgs[0] = Boolean.FALSE;
	        try
	        {
	            _stopForeground.invoke(this, _stopForegroundArgs);
	        }
	        catch (InvocationTargetException e)
	        {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        }
	        catch (IllegalAccessException e)
	        {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
//	    getNotificationManager().cancel(id);
	    setForeground(false);
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	
	protected NotificationManager getNotificationManager()
	{
		if (_notificationManager == null)
		{
			_notificationManager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
		}
		return _notificationManager;
	}
}
