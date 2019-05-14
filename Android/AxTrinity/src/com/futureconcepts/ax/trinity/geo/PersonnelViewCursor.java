package com.futureconcepts.ax.trinity.geo;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;

public class PersonnelViewCursor extends Asset
{
	private static final String COLUMN_USER_TYPE_ICON = "UserTypeIcon";
	private static final String COLUMN_ADDRESS_WKT = "AddressWKT";
	
	public PersonnelViewCursor(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static PersonnelViewCursor query(Context context)
	{
		PersonnelViewCursor result = null;
		Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_mappable_user_list");
		String[] projection = {
				"Asset._id",
				"Asset.ID",
				"Asset.Callsign",
				"OperationalPeriod.ID as OperationalPeriod",
				"Asset.Type as AssetType",
				"Address.WKT as " + COLUMN_ADDRESS_WKT,
				"Icon.Image as " + COLUMN_USER_TYPE_ICON };
		result = new PersonnelViewCursor(context,
				context.getContentResolver().query(uri,
						projection,
						"OperationalPeriod=? AND AssetType=? AND AddressWKT NOT NULL",
						new String[]{MercurySettings.getCurrentOperationalPeriodId(context), AssetType.USER},
						null));
		return result;
	}
	
	public byte[] getUserTypeIcon()
	{
		return getCursorBlob(COLUMN_USER_TYPE_ICON);
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
}
