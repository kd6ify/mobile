package com.futureconcepts.drake.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings
{
    /** controls whether this provider should show the offline contacts */
    public static final String KEY_SHOW_OFFLINE_CONTACTS = "show_offline_contacts";

    /** controls whether the GTalk service automatically connect to server. */
    public static final String KEY_AUTOMATICALLY_CONNECT_GTALK = "gtalk_auto_connect";

    /** controls whether the IM service will be automatically started after boot */
    public static final String KEY_AUTOMATICALLY_START_SERVICE = "auto_start_service";

    /** controls whether the offline contacts will be hided */
    public static final String KEY_HIDE_OFFLINE_CONTACTS = "pref_hide_offline_contacts";

    /** controls whether enable the IM notification */
    public static final String KEY_ENABLE_NOTIFICATION = "pref_enable_notification";

    /** specifies whether to vibrate */
    public static final String KEY_NOTIFICATION_VIBRATE = "pref_notification_vibrate";

    /** specifies whether to play a sound */
    public static final String KEY_NOTIFICATION_SOUND = "pref_notification_sound";

    /** specifies the Uri string of the ringtone */
    public static final String KEY_NOTIFICATION_RINGTONE = "ringtone";

    /** specifies the Uri of the default ringtone */
    public static final String RINGTONE_DEFAULT = "content://settings/system/notification_sound";

    /** specifies whether to show mobile indicator to friends */
    public static final String KEY_SHOW_MOBILE_INDICATOR = "mobile_indicator";

    /** specifies whether to show as away when device is idle */
    public static final String KEY_SHOW_AWAY_ON_IDLE = "show_away_on_idle";

    /** specifies whether to upload heartbeat stat upon login */
    public static final String KEY_UPLOAD_HEARTBEAT_STAT = "upload_heartbeat_stat";

    /** specifies the last heartbeat interval received from the server */
    public static final String KEY_HEARTBEAT_INTERVAL = "heartbeat_interval";
    
    /** How should the OTR engine operate: auto, force, requested, disabled */
    public static final String KEY_OTR_MODE = "pref_security_otr_mode";
	
	private Settings()
	{
		// all methods static
	}

	public static SharedPreferences getPreferences(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences;
	}

	public static boolean hasKey(Context context, String key)
	{
		return getPreferences(context).contains(key);
	}
	
	public static boolean getBoolean(Context context, String key, boolean defaultValue)
	{
		return getPreferences(context).getBoolean(key, defaultValue);
	}

	public static long getLong(Context context, String key, long defaultValue)
	{
		return getPreferences(context).getLong(key, defaultValue);
	}

	public static String getString(Context context, String key, String defaultValue)
	{
		return getPreferences(context).getString(key, defaultValue);
	}
	
	public static SharedPreferences.Editor getPreferencesEditor(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		return editor;
	}

	public static void putBoolean(Context context, String key, boolean value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void putLong(Context context, String key, long value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putLong(key, value);
		editor.commit();
	}

	public static void putString(Context context, String key, String value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
     * Set whether or not the offline contacts should be hided.
     *
     * @param hideOfflineContacts Whether or not the offline contacts should be hided.
     */
    public static void setHideOfflineContacts(Context context, boolean value)
    {
    	putBoolean(context, KEY_HIDE_OFFLINE_CONTACTS, value);
    }

    /**
     * Check if the offline contacts should be hided.
     *
     * @return Whether or not the offline contacts should be hided.
     */
    public static boolean getHideOfflineContacts(Context context)
    {
        return getBoolean(context, KEY_HIDE_OFFLINE_CONTACTS, false);
    }

    /**
     * Set whether or not enable the IM notification.
     *
     * @param enable Whether or not enable the IM notification.
     */
    public static void setEnableNotification(Context context, boolean value)
    {
    	putBoolean(context, KEY_ENABLE_NOTIFICATION, value);
    }

    /**
     * Check if the IM notification is enabled.
     *
     * @return Whether or not enable the IM notification.
     */
    public static boolean getEnableNotification(Context context)
    {
    	return getBoolean(context, KEY_ENABLE_NOTIFICATION, true);
    }

    /**
     * Set whether or not to vibrate on IM notification.
     *
     * @param vibrate Whether or not to vibrate.
     */
    public static void setVibrate(Context context, boolean value)
    {
        putBoolean(context, KEY_NOTIFICATION_VIBRATE, value);
    }

    /**
     * Gets whether or not to vibrate on IM notification.
     *
     * @return Whether or not to vibrate.
     */
    public static boolean getVibrate(Context context)
    {
        return getBoolean(context, KEY_NOTIFICATION_VIBRATE, false /* by default disable vibrate */);
    }
    
    /**
     * Set whether or not to play a sound on IM notification.
     *
     * @param vibrate Whether or not to vibrate.
     */

    public static void setSound(Context context, boolean value)
    {
        putBoolean(context, KEY_NOTIFICATION_SOUND, value);
    }

    /**
     * Gets whether or not to play a sound on IM notification.
     *
     * @return Whether or not to vibrate.
     */
    public static boolean getSound(Context context)
    {
        return getBoolean(context, KEY_NOTIFICATION_SOUND, false /* by default disable sound */);
    }

    /**
     * Set the Uri for the ringtone.
     *
     * @param ringtoneUri The Uri of the ringtone to be set.
     */
    public static void setRingtoneURI(Context context, String ringtoneUri)
    {
    	putString(context, KEY_NOTIFICATION_RINGTONE, ringtoneUri);
    }

    /**
     * Get the Uri String of the current ringtone.
     *
     * @return The Uri String of the current ringtone.
     */
    public static String getRingtoneURI(Context context)
    {
        return getString(context, KEY_NOTIFICATION_RINGTONE, RINGTONE_DEFAULT);
    }

    /**
     * Set whether or not to show mobile indicator to friends.
     *
     * @param showMobile whether or not to show mobile indicator.
     */
    public static void setShowMobileIndicator(Context context, boolean value)
    {
    	putBoolean(context, KEY_SHOW_MOBILE_INDICATOR, value);
    }

    /**
     * Gets whether or not to show mobile indicator.
     *
     * @return Whether or not to show mobile indicator.
     */
    public static boolean getShowMobileIndicator(Context context)
    {
        return getBoolean(context, KEY_SHOW_MOBILE_INDICATOR, true /* by default show mobile indicator */);
    }

    /**
     * Set whether or not to show as away when device is idle.
     *
     * @param showAway whether or not to show as away when device is idle.
     */
    public static void setShowAwayOnIdle(Context context, boolean value)
    {
    	putBoolean(context, KEY_SHOW_AWAY_ON_IDLE, value);
    }

    /**
     * Get whether or not to show as away when device is idle.
     *
     * @return Whether or not to show as away when device is idle.
     */
    public boolean getShowAwayOnIdle(Context context)
    {
        return getBoolean(context, KEY_SHOW_AWAY_ON_IDLE, true /* by default show as away on idle*/);
    }

    /**
     * Set whether or not to upload heartbeat stat.
     *
     * @param uploadStat whether or not to upload heartbeat stat.
     */
    public static void setUploadHeartbeatStat(Context context, boolean value)
    {
    	putBoolean(context, KEY_UPLOAD_HEARTBEAT_STAT, value);
    }

    /**
     * Get whether or not to upload heartbeat stat.
     *
     * @return Whether or not to upload heartbeat stat.
     */
    public static boolean getUploadHeartbeatStat(Context context)
    {
        return getBoolean(context, KEY_UPLOAD_HEARTBEAT_STAT, false /* by default do not upload */);
    }

    /**
     * Set the last received heartbeat interval from the server.
     *
     * @param interval the last received heartbeat interval from the server.
     */
    public static void setHeartbeatInterval(Context context, long value)
    {
    	putLong(context, KEY_HEARTBEAT_INTERVAL, value);
    }

    /**
     * Get the last received heartbeat interval from the server.
     *
     * @return the last received heartbeat interval from the server.
     */
    public static long getHeartbeatInterval(Context context)
    {
        return getLong(context, KEY_HEARTBEAT_INTERVAL, 0L /* an invalid default interval */);
    }
    
    /**
     * A convenience method to set the mode of operation for the OTR Engine
     * 
     * @param cr The ContentResolver to use to access the settings table
     * @param providerId used to identify the set of settings for a given provider
     * @param otrMode OTR Engine mode (force, auto, requested, disabled)
     */
    public static void setOtrMode(Context context, String otrMode)
    {
        putString(context, KEY_OTR_MODE, otrMode);
    }

    public static String getOtrMode(Context context)
    {
        return getString(context, KEY_OTR_MODE, "auto" /* by default, try to use OTR */);
    }
}
