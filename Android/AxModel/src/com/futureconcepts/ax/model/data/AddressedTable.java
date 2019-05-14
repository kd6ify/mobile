package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;

public class AddressedTable extends BaseTable
{
    public static final String ADDRESS = "Address";

    private Address _address;
    
	public AddressedTable(Context context, Cursor cursor)
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
		if (_address != null)
		{
			_address.close();
			_address = null;
		}
	}
	
	public String getAddressID()
	{
		return getCursorGuid(ADDRESS);
	}
	
	public Address getAddress(Context context)
	{
		String id = getAddressID();
		if (_address == null && id != null)
		{
			_address = Address.query(context, Uri.withAppendedPath(Address.CONTENT_URI, id));
		}
		return _address;
	}
	
	public void setAddressID(String value)
	{
		setModel(ADDRESS, value);
	}
	
	public boolean isMappable(Context context)
	{
		boolean result = false;
		try
		{
			Address address = getAddress(context);
			if (address != null)
			{
				result = address.isMappable();
			}
		}
		catch (Exception e) {}
		return result;
	}
	
	public Point getGeoPoint(Context context)
	{
		Point result = null;
		Address address = getAddress(context);
		if (address != null)
		{
			result = address.getWKTAsPoint();
		}
		return result;
	}
}
