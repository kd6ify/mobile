package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class IncidentAssetView extends Asset
{
	public static final String QUERY = "SELECT DISTINCT Asset.*, OperationalPeriod.Incident AS Incident FROM Asset " +
		"INNER JOIN AssetAttribute ON AssetAttribute.Asset=Asset.ID " +
		"INNER JOIN OperationalPeriod ON OperationalPeriod.ID=AssetAttribute.OperationalPeriod";
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IncidentAssetView");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.IncidentAssetView";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.IncidentAssetView";

	public IncidentAssetView(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public static IncidentAssetView queryMappable(Context context, String currentIncidentID)
	{
		IncidentAssetView result = null;
		try
		{
			result = new IncidentAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "Incident=? AND Address NOT NULL", new String[]{currentIncidentID}, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static IncidentAssetView query(Context context, String currentIncidentID)
	{
		IncidentAssetView result = null;
		try
		{
			result = new IncidentAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "Incident=?", new String[]{currentIncidentID}, null));
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

	public static IncidentAssetView queryType(Context context, String assetType, String currentIncidentID)
	{
		IncidentAssetView result = null;
		try
		{
			result = new IncidentAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "Incident=? AND Type=?", new String[]{currentIncidentID, assetType}, null));
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
}
