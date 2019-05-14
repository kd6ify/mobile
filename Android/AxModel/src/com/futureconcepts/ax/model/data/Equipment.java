package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Equipment extends BaseTable
{
	public static final String FUEL = "Fuel";
    public static final String TYPE = "Type";
    public static final String NAME = "Name";
    public static final String AGENCY = "Agency";
    public static final String DIMENSION = "Dimension";
    public static final String STORAGE_ADDRESS = "StorageAddress";
    public static final String SERIAL_NO = "SerialNo";
    public static final String MILEAGE = "Mileage";
    public static final String MAKE = "Make";
    public static final String MODEL = "Model";
    public static final String VIN = "VIN";
    public static final String LICENSE_NO = "LicenseNo";
    public static final String RIG_NO = "RigNo";
    public static final String OPERATING_HOURS = "OperatingHours";
    public static final String CAPACITY = "Capacity";
    public static final String WEIGHT = "Weight";
    public static final String MAINTENANCE_CYCLE = "MaintenanceCycle";
    public static final String MAX_FUEL_CAPACITY = "MaxFuelCapacity";
    public static final String DEVICE_ID = "DeviceID";
    public static final String YEAR = "Year";
    public static final String EXTRA_INFO = "ExtraInfo";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Equipment");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Equipment";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Equipment";

    private EquipmentType _type;
    private Agency _agency;
    
	public Equipment(Context context, Cursor cursor)
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
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
		if (_agency != null)
		{
			_agency.close();
			_agency = null;
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	public String getName()
	{
		return getCursorString(NAME);
	}
	
	public String getTypeID()
	{
		return getCursorString(TYPE);
	}

	public EquipmentType getType(Context context)
	{
		String id = getTypeID();
		if (_type == null && id != null)
		{
			_type = EquipmentType.query(context, Uri.withAppendedPath(EquipmentType.CONTENT_URI, id));
		}
		return _type;
	}
		
	public String getAgencyID()
	{
		return getCursorGuid(AGENCY);
	}
	
	public Agency getAgency(Context context)
	{
		String id = getAgencyID();
		if (_agency == null && id != null)
		{
			_agency = Agency.query(context, Uri.withAppendedPath(Agency.CONTENT_URI, id));
		}
		return _agency;
	}
	
	public double getWeight()
	{
		return getCursorDouble(WEIGHT);
	}
	
	public static Equipment query(Context context)
	{
		Equipment result = null;
		try
		{
			result = new Equipment(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static Equipment query(Context context, Uri uri)
	{
		Equipment result = null;
		try
		{
			result = new Equipment(context, context.getContentResolver().query(uri, null, null, null, null));
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
