package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class OperationalPeriodUserAssetView extends Asset
{
	public static final String QUERY = "SELECT DISTINCT Asset.*, User.*, UserType.*, Person.*, OperationalPeriod.ID AS OperationalPeriod, OperationalPeriod.Incident AS Incident, Person.Name as Name FROM Asset " +
		"INNER JOIN AssetAttribute ON (AssetAttribute.Asset=Asset.ID) " +
		"INNER JOIN OperationalPeriod ON (OperationalPeriod.ID=AssetAttribute.OperationalPeriod) " +
		"INNER JOIN User ON (Asset.User=User.ID) " +
		"INNER JOIN UserType ON (User.Type=UserType.ID) " +
		"INNER JOIN Person ON (User.Person=Person.ID)";
	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/OperationalPeriodUserAssetView");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.OperationalPeriodUserAssetView";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.OperationalPeriodUserAssetView";

	public OperationalPeriodUserAssetView(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getPersonName()
	{
		return getCursorString("PersonName");
	}
	
	public static OperationalPeriodUserAssetView queryMappableForOperationalPeriod(Context context, String currentOperationalPeriodID)
	{
		OperationalPeriodUserAssetView result = null;
		try
		{
			result = new OperationalPeriodUserAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "OperationalPeriod=? AND Address NOT NULL", new String[]{currentOperationalPeriodID}, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
		
	public static OperationalPeriodUserAssetView query(Context context, String id, String operationalPeriodID)
	{
		OperationalPeriodUserAssetView result = null;
		try
		{
			result = new OperationalPeriodUserAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "ID=? AND OperationalPeriod=?", new String[]{id, operationalPeriodID}, null));
			if (result.getCount() == 1)
			{
				result.moveToFirst();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static OperationalPeriodUserAssetView query(Context context, String operationalPeriodID)
	{
		OperationalPeriodUserAssetView result = null;
		try
		{
			result = new OperationalPeriodUserAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "OperationalPeriod=? AND Type=?", new String[]{operationalPeriodID, AssetType.USER}, "Name ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
