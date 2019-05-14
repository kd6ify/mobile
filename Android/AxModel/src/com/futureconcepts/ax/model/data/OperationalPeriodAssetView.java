package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class OperationalPeriodAssetView extends Asset
{
	public static final String QUERY = "SELECT DISTINCT Asset.*, OperationalPeriod.ID AS OperationalPeriod, OperationalPeriod.Incident AS Incident FROM Asset " +
		"INNER JOIN AssetAttribute ON AssetAttribute.Asset=Asset.ID " +
		"INNER JOIN OperationalPeriod ON OperationalPeriod.ID=AssetAttribute.OperationalPeriod";
	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/OperationalPeriodAssetView");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.OperationalPeriodAssetView";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.OperationalPeriodAssetView";

	public OperationalPeriodAssetView(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public static OperationalPeriodAssetView queryMappableForOperationalPeriod(Context context, String currentOperationalPeriodID)
	{
		OperationalPeriodAssetView result = null;
		try
		{
			result = new OperationalPeriodAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "OperationalPeriod=? AND Address NOT NULL", new String[]{currentOperationalPeriodID}, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
		
	public static OperationalPeriodAssetView query(Context context, String id)
	{
		OperationalPeriodAssetView result = null;
		try
		{
			result = new OperationalPeriodAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "ID=?", new String[]{id}, null));
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
	
	public static OperationalPeriodAssetView queryEquipmentAssetForOperationalPeriod(Context context, String currentOperationalPeriodID)
	{
		OperationalPeriodAssetView result = null;
		try
		{
			result = new OperationalPeriodAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "OperationalPeriod=? AND Type=?", new String[]{currentOperationalPeriodID, AssetType.EQUIPMENT}, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static OperationalPeriodAssetView queryEquipmentAssetForIncident(Context context, String currentIncidentID)
	{
		OperationalPeriodAssetView result = null;
		try
		{
			result = new OperationalPeriodAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "Incident=? AND Type=?", new String[]{currentIncidentID, AssetType.EQUIPMENT}, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}	
}
