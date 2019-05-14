package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class OperationalPeriodEquipmentAssetView extends OperationalPeriodAssetView
{
	public static final String QUERY = "SELECT Asset.*, AssetAttribute.Role as Role, OperationalPeriod.ID AS OperationalPeriod, Equipment.Name as Name, Equipment.SerialNo as SerialNo FROM Asset " +
		"INNER JOIN AssetAttribute ON AssetAttribute.Asset=Asset.ID " +
		"INNER JOIN OperationalPeriod ON OperationalPeriod.ID=AssetAttribute.OperationalPeriod " +
		"INNER JOIN Equipment ON Asset.Equipment=Equipment.ID";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/OperationalPeriodEquipmentAssetView");

	public OperationalPeriodEquipmentAssetView(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static OperationalPeriodEquipmentAssetView queryEquipmentAsset(Context context, String currentOperationalPeriodID)
	{
		OperationalPeriodEquipmentAssetView result = null;
		try
		{
			result = new OperationalPeriodEquipmentAssetView(context, context.getContentResolver().query(CONTENT_URI, null, "OperationalPeriod=? AND Type=?", new String[]{currentOperationalPeriodID, AssetType.EQUIPMENT}, "Name ASC, SerialNo ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
