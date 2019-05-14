package com.futureconcepts.jupiter.data;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

public class Placemark extends Folder
{
    public static final String AUTHORITY = "com.futureconcepts.jupiter.provider.placemark";

    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String ADDRESS = "Address";
	public static final String MEDIA_URL = "MediaUrl";
    public static final String TIME = "Time";
    public static final String ACCURACY = "Accuracy";
    public static final String ALTITUDE = "Altitude";
    public static final String BEARING = "Bearing";
    public static final String SPEED = "Speed";
    public static final String STYLE = "Style";
        
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placemark");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.jupiter.Placemark";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.jupiter.Placemark";
    
    public Placemark(Cursor cursor)
    {
    	super(cursor);
    }
        
    /**
     * Converts this placemark into a Location object
     * @return a Location in the same position as the placemark
     */
    public Location toLocation()
    {
    	Location l = new Location("com.futureconcepts.jupiter");
    	l.setAccuracy(getAccuracy());
    	l.setAltitude(getAltitude());
    	l.setBearing(getBearing());
    	l.setLatitude(getLatitude());
    	l.setLongitude(getLongitude());
    	l.setSpeed(getSpeed());
    	return l;
    }
    
	public double getLatitude()
	{
		return getDouble(getColumnIndex(LATITUDE));
	}
	
	public double getLongitude()
	{
		return getDouble(getColumnIndex(LONGITUDE));
	}

	public String getLabel()
	{
		return null;
	}
	
	public String getType()
	{
		return null;
	}
	
    public String getAddress()
    {
    	return getString(getColumnIndex(ADDRESS));
    }
    
    public String getMediaUrl()
    {
    	return getString(getColumnIndex(MEDIA_URL));
    }

	public long getTime()
	{
		return getLong(getColumnIndex(TIME));
	}
	
	public int getAccuracy()
	{
		return getInt(getColumnIndex(ACCURACY));
	}
	
	public int getAltitude()
	{
		return getInt(getColumnIndex(ALTITUDE));
	}
	
	public int getBearing()
	{
		return getInt(getColumnIndex(BEARING));
	}
	
	public int getSpeed()
	{
		return getInt(getColumnIndex(SPEED));
	}
	
	public String getStyle()
	{
		return getString(getColumnIndex(STYLE));
	}
	
    public static Placemark getPlacemarkById(Context context, String id)
    {
    	Placemark placemark = new Placemark(context.getContentResolver().query(CONTENT_URI, null, ID + "='" + id + "'", null, null));
    	if (placemark != null)
    	{
	    	if (placemark.getCount() == 1)
	    	{
	    		placemark.moveToFirst();
	    		return placemark;
	    	}
	    	else
	    	{
	    		placemark.close();
	    		return null;
	    	}
    	}
    	else
    	{
    		return null;
    	}
    }
	
    public static Placemark getPlacemarksByParentId(Context context, String parentId)
    {
    	Placemark placemark = new Placemark(context.getContentResolver().query(CONTENT_URI, null, PARENT_ID + "='" + parentId + "'", null, null));
    	if (placemark != null)
    	{
	    	if (placemark.getCount() == 0)
	    	{
	    		placemark.close();
	    		placemark = null;		
	    	}
    	}
    	return placemark;
    }

    public static Placemark getPlacemarkByUri(Context context, Uri uri)
    {
    	Placemark placemark = new Placemark(context.getContentResolver().query(uri, null, null, null, null));
    	if (placemark != null)
    	{
	    	if (placemark.getCount() == 1)
	    	{
	    		placemark.moveToFirst();
	    		return placemark;
	    	}
	    	else
	    	{
	    		placemark.close();
	    		return null;
	    	}
    	}
    	else
    	{
    		return null;
    	}
    }
}
