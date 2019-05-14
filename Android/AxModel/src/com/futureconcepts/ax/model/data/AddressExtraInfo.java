package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AddressExtraInfo extends BaseTable {

	public static final String NAME = "Name";
//	public static final String BEARING = "Bearing";
//	public static final String HEADING = "Heading";
//	public static final String COURSE = "Course";
//	public static final String ALTITUDE = "Altitude";
//	public static final String ELEVATION = "Elevation";
//	public static final String PITCH = "Pitch";
//	public static final String YAW = "yaw";
//	public static final String ROLL = "Roll";
//	public static final String SPEED = "Speed";
	public static final String STYLE = "Style";
//	public static final String RECORDTIME = "RecordTime";
//	public static final String HORIZONTAL_ACCURACY = "HorizontalAccuracy";
//	public static final String VERTICAL_ACCURACY = "VerticalAccuracy";
	/**
	* The content:// style URL for this table
	*/
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AddressExtraInfo");
    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AddressExtraInfo";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AddressExtraInfo";
	
	public AddressExtraInfo(Context context, Cursor cursor) {
		super(context, cursor);
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return getModelString(NAME);
	}

	public String getStyle() {
		return getModelString(STYLE);
	}
	
	public static AddressExtraInfo queryAddressExtraInfo(Context context)
	{
		AddressExtraInfo result = null;
		try
		{
			result = new AddressExtraInfo(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
		
	}
	
	public static AddressExtraInfo getAddressExtraInfoCursor(Context context, String id)
	{
		AddressExtraInfo result = null;
		try
		{
			result = new AddressExtraInfo(context, context.getContentResolver().query(AddressExtraInfo.CONTENT_URI, null,"ID='"+id+"'", null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

}
