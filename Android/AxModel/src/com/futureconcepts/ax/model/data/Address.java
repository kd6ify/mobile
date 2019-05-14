package com.futureconcepts.ax.model.data;


import java.io.File;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class Address extends BaseTable
{
    public static final String NAME = "Name";
    public static final String STREET1 = "Street1";
    public static final String STREET2 = "Street2";
    public static final String STREET3 = "Street3";
    public static final String CITY = "City";
    public static final String TYPE = "Type";
    public static final String STATE = "State";
    public static final String ZIP = "ZIP";
    public static final String ZIP4 = "ZIP4";
    public static final String POSTAL_CODE = "PostalCode";
    public static final String COUNTRY = "Country";
    public static final String PROVINCE = "Province";
    public static final String WKT = "WKT";
    public static final String ULLAT = "UlLat";
    public static final String ULLON = "UlLon";
    public static final String LRLAT = "LrLat";
    public static final String LRLON = "LrLon";
    public static final String EXTRA_INFO = "ExtraInfo";
    public static final String NOTES = "Notes";

    private INCITS38200x _state;
    private AddressType _type;
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Address");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Address";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Address";
   
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = LAST_MODIFIED + " DESC";

    public Address(Context context, Cursor cursor)
    {
    	super(context, cursor);
    }
    
    @Override
    public void close()
    {
    	closeReferences();
    	super.close();
    }
    
    @Override
    public boolean requery()
    {
    	closeReferences();
    	return super.requery();
    }
    
    @Override
    public boolean moveToPosition(int position)
    {
    	closeReferences();
    	return super.moveToPosition(position);
    }
    
    private void closeReferences()
    {
		if (_state != null)
		{
			_state.close();
			_state = null;
		}
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
    }
    
    public String getName()
    {
    	return getCursorString(NAME);
    }
    
    public String getStreet1()
    {
    	return getCursorString(STREET1);
    }
    
    public String getStreet2()
    {
    	return getCursorString(STREET2);
    }

    public String getStreet3()
    {
    	return getCursorString(STREET3);
    }

    public String getCity()
    {
    	return getCursorString(CITY);
    }
    
    public String getTypeID()
    {
    	return getCursorGuid(TYPE);
    }

    public AddressType getType(Context context)
    {
    	String id = getTypeID();
		if (_type == null && id != null)
		{
			_type = AddressType.query(context, Uri.withAppendedPath(AddressType.CONTENT_URI, id));
		}
		return _type;
    }
    
    public String getStateID()
    {
    	return getCursorGuid(STATE);
    }
    
    public INCITS38200x getState(Context context)
    {
    	String id = getStateID();
		if (_state == null && id != null)
		{
			_state = INCITS38200x.query(context, Uri.withAppendedPath(INCITS38200x.CONTENT_URI, id));
		}
		return _state;
    }

    public String getStateCode(Context context)
    {
    	String result = null;
    	INCITS38200x state = getState(context);
    	if (state != null)
    	{
    		result = state.getStateCode();
    	}
    	return result;
    }
    
    public int getZIP()
    {
    	return getCursorInt(ZIP);
    }
    
    public int getZIP4()
    {
    	return getCursorInt(ZIP4);
    }
    
    public String getWKT()
    {
    	return getCursorString(WKT);
    }
    
    public double getUlLat()
    {
    	return getCursorDouble(ULLAT);
    }
    public double getUlLon()
    {
    	return getCursorDouble(ULLON);
    }
    public double getLrLat()
    {
    	return getCursorDouble(LRLAT);
    }
    public double getLrLon()
    {
    	return getCursorDouble(LRLON);
    }
    public String getExtraInfo()
    {
    	return getCursorString(EXTRA_INFO);
    }
    
    public String getMailingLabel(Context context)
    {
    	StringBuilder sb = new StringBuilder();
		appendLine(sb, getName());
		appendLine(sb, getStreet1());
		appendLine(sb, getStreet2());
		appendLine(sb, getStreet3());
		appendLine(sb, getCity());
		appendLine(sb, getStateCode(context));
		int zip = getZIP();
		if (zip != 0)
		{
			appendLine(sb, zip);
			int zip4 = getZIP4();
			if (zip4 != 0)
			{
				sb.append("-");
				sb.append(zip4);
			}
		}
		if (sb.length() == 0)
		{
			String wkt = getWKT();
			if (wkt != null)
			{
				sb.append(wkt);
			}
		}
		if (sb.length() == 0)
		{
			sb.append("None");
		}
		return sb.toString();
	}
	
	private void appendLine(StringBuilder sb, int num)
	{
		if (sb.length() > 0)
		{
			sb.append("\n");
		}
		sb.append(num);
	}
	
	private void appendLine(StringBuilder sb, String str)
	{
		if (str != null)
		{
			if (sb.length() > 0)
			{
				sb.append("\n");
			}
			sb.append(str);
		}
	}
    
	public Point getWKTAsPoint()
	{
		Point result = new Point(0,0);
		String wkt = getWKT();
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
			String wkt = getWKT();
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
	
	public static Address query(Context context, Uri uri)
	{
		Address result = null;
		try
		{
			result = new Address(context, context.getContentResolver().query(uri, null, null, null, null));
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
	
	public static Address queryWhere(Context context, Uri uri, String where)
	{
		Address result = null;
		try
		{
			result = new Address(context, context.getContentResolver().query(uri, null, where, null, null));
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
	
	
	public static void setWKTFromLocation(ContentValues values, Location location)
	{
		if (location != null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("POINT(");
			sb.append(location.getLongitude());
			sb.append(" ");
			sb.append(location.getLatitude());
			sb.append(")");
			values.put(WKT, sb.toString());
		}
		else
		{
			values.putNull(WKT);
		}
	}
	
	public static Uri insertAddress(Context context,Location location,String id, String action)
	{		
		Uri result =null;
		Address address = new Address(context,null);
		address.beginEdit();			
		address.setID(id);
		StringBuilder sb = new StringBuilder();
		sb.append("POINT(");
		if(location!=null){
			sb.append(location.getLongitude());
			sb.append(" ");
			sb.append(location.getLatitude());
		}else{
			sb.append("0");
			sb.append(" ");
			sb.append("0");
		}
			
		sb.append(")");
		address.setModel(WKT, sb.toString());
		address.setModel(TYPE,AddressType.MOBILE);
		try
		{
			result = context.getContentResolver().insert(CONTENT_URI, address.endEditAndUpload(action));
		}catch (Exception e)
		{
			e.printStackTrace();
			//Log.i("Address Insertion error",""+e.toString());
		}
		return result;

		
	}
	
	public static class Content extends ContentBase
	{
		public Content(Context context, String deviceID)
		{
			super(context);
			initialize(deviceID);
			setTypeID(AddressType.MOBILE);
		}

		public Content(Context context, Address address)
		{
			super(context);
			if (address != null && address.getCount() == 1)
			{
				address.moveToFirst();
				setID(address.getID());
				setName(address.getName());
				setStreet1(address.getStreet1());
				setStreet2(address.getStreet2());
				setStreet3(address.getStreet3());
				setCity(address.getCity());
				setTypeID(address.getTypeID());
				setStateID(address.getStateID());
				setZIP(address.getZIP());
				setZIP4(address.getZIP4());
				setWKT(address.getWKT());
				setUlLat(address.getUlLat());
				setUlLon(address.getUlLon());
				setLrLat(address.getLrLat());
				setLrLon(address.getLrLon());
			}
		}
		
		public void setID(String value)
		{
			_values.put(ID, value);
		}
		
		public void setName(String value)
		{
			_values.put(NAME, value);
		}
		
		public void setStreet1(String value)
		{
			_values.put(STREET1, value);
		}

		public void setStreet2(String value)
		{
			_values.put(STREET2, value);
		}

		public void setStreet3(String value)
		{
			_values.put(STREET3, value);
		}

		public void setCity(String value)
		{
			_values.put(CITY, value);
		}
		
		public void setTypeID(String value)
		{
			_values.put(TYPE, value);
		}
		
		public void setStateID(String value)
		{
			_values.put(STATE, value);
		}
		
		public void setZIP(Integer value)
		{
			_values.put(ZIP, value);
		}
		
		public void setZIP4(Integer value)
		{
			_values.put(ZIP4, value);
		}
		
		public void setPostalCode(String value)
		{
			_values.put(POSTAL_CODE, value);			
		}
		
		public void setCountryID(String value)
		{
			_values.put(COUNTRY, value);			
		}

		public void setProvice(String value)
		{
			_values.put(PROVINCE, value);
		}
		
		public void setWKT(String value)
		{
			_values.put(WKT, value);
		}

		public void setWKT(PointF value)
		{
			if (value != null)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("POINT(");
				sb.append(value.x);
				sb.append(" ");
				sb.append(value.y);
				sb.append(")");
				_values.put(WKT, sb.toString());
			}
			else
			{
				_values.putNull(WKT);
			}
		}
		
		public void setWKT(Location value)
		{
			if (value != null)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("POINT(");
				sb.append(value.getLongitude());
				sb.append(" ");
				sb.append(value.getLatitude());
				sb.append(")");
				_values.put(WKT, sb.toString());
			}
			else
			{
				_values.putNull(WKT);
			}
		}
		
		public void setUlLat(Double value)
		{
			_values.put(ULLAT, value);
		}

		public void setUlLon(Double value)
		{
			_values.put(ULLON, value);
		}
		
		public void setLrLat(Double value)
		{
			_values.put(LRLAT, value);
		}
		
		public void setLrLon(Double value)
		{
			_values.put(LRLON, value);
		}
				
		public void setExtraInfo(String value)
		{
			_values.put(EXTRA_INFO, value);			
		}
		
		public void setNotes(String value)
		{
			_values.put(NOTES, value);
		}
		
		public Uri insert(Context context)
		{
			_values.put(LAST_MODIFIED, System.currentTimeMillis());
			return context.getContentResolver().insert(CONTENT_URI, _values);
		}
	}
}
