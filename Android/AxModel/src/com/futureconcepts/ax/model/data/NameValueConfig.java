package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class NameValueConfig extends BaseTable
{
    public static final String AUTHORITY = "com.futureconcepts.provider.config";
    
    /* Name & ID fields */
    
    public static final String NAME = "Name";
    public static final String VALUE = "Value";
    
    public static final String KEY_CURRENT_INCIDENT_ID = "current_incident_id";
    public static final String KEY_CURRENT_INCIDENT_NAME = "current_incident_name";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_WEB_SERVICE_ADDRESS = "web_service_address";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TRACKER_MODE = "tracker_mode";
    public static final String KEY_PUSH_SERVICE_ADDRESS = "push_service_address";
    public static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    public static final String KEY_MY_EQUIPMENT_ID = "my_equipment_id";
    public static final String KEY_FULL_SYNC_IN_PROGRESS = "full_sync_in_progress";
    public static final String KEY_ALERT_MODE = "alert_mode";
    public static final String KEY_VIBRATE_ALERT = "vibrate_alert";
    public static final String KEY_SPEAK_ALERT = "speak_alert";
    public static final String KEY_TONE_ALERT = "tone_alert";
    public static final String KEY_MAP_ICON_SIZE_BUMP = "map_icon_size_bump";
    public static final String KEY_ALERT_INCIDENT_ONLY = "alert_incident_only";
    public static final String KEY_ALERT_TONE_URI = "alert_tone_uri";
    public static final String KEY_LAYER_SELECTED = "layer_selected";
    public static final String KEY_LAST_MAP_SCALE = "last_map_scale";
    public static final String KEY_LAST_MAP_LATITUDE = "last_map_latitude";
    public static final String KEY_LAST_MAP_LONGITUDE = "last_map_longitude";
    public static final String KEY_MAP_THEME = "map_theme";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/namevalue");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.NameValue";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.NameValue";

	public NameValueConfig(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	public String getName()
	{
		return getString(getColumnIndex(NAME));
	}
	
	public String getValueAsString()
	{
		int idx = getColumnIndex(VALUE);
		assert(idx != -1);
		return getString(idx);
	}

	public int getValueAsInt()
	{
		int idx = getColumnIndex(VALUE);
		assert(idx != -1);
		return getInt(idx);
	}

	public long getValueAsLong()
	{
		int idx = getColumnIndex(VALUE);
		assert(idx != -1);
		return getLong(idx);
	}

	public boolean getValueAsBoolean()
	{
		int idx = getColumnIndex(VALUE);
		assert(idx != -1);
		return Boolean.parseBoolean(getString(idx));
	}

	public float getValueAsFloat()
	{
		int idx = getColumnIndex(VALUE);
		assert(idx != -1);
		return getFloat(idx);
	}
}
