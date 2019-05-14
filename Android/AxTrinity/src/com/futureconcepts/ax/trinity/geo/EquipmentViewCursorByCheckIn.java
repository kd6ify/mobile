package com.futureconcepts.ax.trinity.geo;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetStatus;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Point;
import android.net.Uri;

public class EquipmentViewCursorByCheckIn extends Asset
{
	private static final String COLUMN_EQUIPMENT_TYPE_NAME = "EquipmentTypeName";
	private static final String COLUMN_EQUIPMENT_TYPE_ICON = "EquipmentTypeIcon";
	private static final String COLUMN_EQUIPMENT_TYPE_IS_ACCESSORY = "IsAccessory";
	private static final String COLUMN_EQUIPMENT_NAME = "EquipmentName";
	private static final String COLUMN_ADDRESS_WKT = "AddressWKT";
	private static final String COLUMN_CHECK_IN_TIME = "AssetCheckInTime";
	private static final String COLUMN_CHECK_OUT_TIME = "AssetCheckOutTime";
	private static final String COLUMN_ASSET_STATUS = "AssetStatus";
	
	public EquipmentViewCursorByCheckIn(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static EquipmentViewCursorByCheckIn query(Context context)
	{
		EquipmentViewCursorByCheckIn result = null;
		Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_mappable_equipment_list");
		String[] projection = {
				"Asset._id",
				"Asset.ID",
				"Asset.Callsign",
				"Asset.Status as " + COLUMN_ASSET_STATUS,
				"Asset.CheckInTime as " + COLUMN_CHECK_IN_TIME,
				"Asset.CheckoutTime as " + COLUMN_CHECK_OUT_TIME,
				"OperationalPeriod.Incident as Incident",
				"Asset.Type as AssetType",
				"EquipmentType.Name as " + COLUMN_EQUIPMENT_TYPE_NAME,
				"EquipmentType.IsAccessory as " + COLUMN_EQUIPMENT_TYPE_IS_ACCESSORY,
				"Equipment.Name as " + COLUMN_EQUIPMENT_NAME,
				"Address.WKT as " + COLUMN_ADDRESS_WKT,
				"Icon.Image as " + COLUMN_EQUIPMENT_TYPE_ICON };
		result = new EquipmentViewCursorByCheckIn(context,
				context.getContentResolver().query(uri,
						projection,
						"Incident=? " +
						"AND AssetType=? " +
						"AND ((AssetCheckInTime<? " +
						"AND (AssetCheckOutTime IS NULL OR AssetCheckOutTime>?)) " +
						"OR AssetStatus =?) " +
						"AND AddressWKT NOT NULL "+ 
						"AND IsAccessory=0",
						getSelectionArguments(context),
						null));
		return result;
	}
		
	private static String[] getSelectionArguments(Context context)
	{
		String now = DateTime.now().toString();
		return 	new String[] {
				MercurySettings.getCurrentIncidentId(context),
				AssetType.EQUIPMENT,
				now.toString(),
				now.toString(),
				AssetStatus.UNKNOWN 
				};		
	}
	
	public String getEquipmentTypeName()
	{
		return getCursorString(COLUMN_EQUIPMENT_TYPE_NAME);
	}
	
	public byte[] getEquipmentTypeIcon()
	{
		return getCursorBlob(COLUMN_EQUIPMENT_TYPE_ICON);
	}
	
	public String getEquipmentName()
	{
		return getCursorString(COLUMN_EQUIPMENT_NAME);
	}
	
	public String getAddressWKT()
	{
		return getCursorString(COLUMN_ADDRESS_WKT);
	}

	public GeoPoint getGeoPointFromWKT()
	{
		GeoPoint result = null;
		Point point = getPointFromWKT();
		if (point != null)
		{
			result = new GeoPoint(point.y, point.x);
		}
		else
		{
			result = new GeoPoint(0,0);
		}
		return result;
	}
	
	public Point getPointFromWKT()
	{
		Point result = null;
		String wkt = getCursorString(COLUMN_ADDRESS_WKT);
		if (wkt != null)
		{
			String[] parts = wkt.split("\\(");
			if (parts.length == 2)
			{
				if (parts[0].equals("POINT"))
				{
					String[] parts2 = parts[1].split("\\)");
					if (parts2.length == 1)
					{
						String[] parts3 = parts2[0].split(" ");
						float lon = Float.parseFloat(parts3[0]);
						float lat = Float.parseFloat(parts3[1]);
						result = new Point((int)(lon * 1e6), (int)(lat * 1e6));
					}
				}
			}
			else if (parts.length == 3)
			{
				if (parts[0].equals("POLYGON"))
				{
					// punt for now -- mantis bug#
					// how do we draw a polygon on the map?
					// use first point in polygon
					String[] parts2 = parts[2].split(",");
					if (parts2.length > 0)
					{
						String[] parts3 = parts2[0].split(" ");
						float lon = Float.parseFloat(parts3[0]);
						float lat = Float.parseFloat(parts3[1]);
						result = new Point((int)(lat * 1e6), (int)(lon * 1e6));
					}
				}
			}
		}
		return result;
	}

	public boolean isMappable()
	{
		boolean result = false;
		try
		{
			String wkt = getCursorString(COLUMN_ADDRESS_WKT);
			if (wkt != null)
			{
				if (wkt.contains("POINT") || wkt.contains("POLYGON"))
				{
					result = true;
				}
			}
		}
		catch (Exception e) {}
		return result;
	}
}
