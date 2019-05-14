package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Asset extends AddressedTable
{
    public static final String CALLSIGN = "Callsign";
    public static final String NOTES = "Notes";
    public static final String IS_LIVE_TRACKING = "IsLiveTracking";
    public static final String IS_PATROL_TRAINED = "IsPatrolTrained";
    public static final String IS_DRAFTED = "IsDrafted";
    public static final String TRANSPORT_PRIORITY = "TransportPriority";
    public static final String ON_DOUBLE_SHIFT = "OnDoubleShift";
    public static final String TYPE = "Type";
    public static final String STATUS = "Status";
    public static final String CHECK_IN_LOCATION = "CheckInLocation";
    public static final String RESOURCE_TYPE = "ResourceType";
    public static final String USER_TYPE = "UserType";
    public static final String USER = "User";
    public static final String EQUIPMENT_TYPE = "EquipmentType";
    public static final String EQUIPMENT = "Equipment";
    public static final String HEALTH = "Health";
    public static final String CONTROLLER_NO = "ControllerNo";
    public static final String OVERTIME_CODE = "OverTimeCode";
    public static final String CHECKIN_TIME = "CheckInTime";
    public static final String CHECKOUT_TIME = "CheckoutTime";
    public static final String CHECKIN_BY_TIME = "CheckInByTime";
    public static final String SHIFT_START = "ShiftStart";
    public static final String SHIFT_END = "ShiftEnd";
    public static final String OVER_TIME_START = "OverTimeStart";
    public static final String OVER_TIME_SLIP_RETURNED = "OverTimeSlipReturned";
    public static final String DRIVE_TIME_HOURS = "DriveTimeHours";
    public static final String START_DISTANCE = "StartDistance";
    public static final String END_DISTANCE = "EndDistance";
    public static final String AUDIT_USER = "AuditUser";
    public static final String AUDIT_DATE = "AuditDate";
    public static final String REASON = "Reason";
    public static final String EXPECTED_RANK = "ExpectedRank";
    public static final String HOURS_USED = "HoursUsed";
    public static final String IS_CHECKED_IN = "IsCheckedIn";
    public static final String IS_CHECKED_IN_BY_TIME = "IsCheckedInByTime";
    
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Asset");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Asset";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Asset";
            
    private AssetType _type;
    private AssetStatus _status;
    private EquipmentType _equipmentType;
    private Equipment _equipment;
    private UserType _userType;
    private User _user;
    private Address _checkInLocation;
    private UserRankType _expectedRank;
    
	public Asset(Context context, Cursor cursor)
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
	
	@Override
	public Uri getContentUri()
	{
		return Uri.withAppendedPath(CONTENT_URI, getID());
	}
	
	private void closeReferences()
	{
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
		if (_equipmentType != null)
		{
			_equipmentType.close();
			_equipmentType = null;
		}
		if (_equipment != null)
		{
			_equipment.close();
			_equipment = null;
		}
		if (_userType != null)
		{
			_userType.close();
			_userType = null;
		}
		if (_user != null)
		{
			_user.close();
			_user = null;
		}
		if (_checkInLocation != null)
		{
			_checkInLocation.close();
			_checkInLocation = null;
		}
		if (_expectedRank != null)
		{
			_expectedRank.close();
			_expectedRank = null;
		}
	}
	
	public String getCallsign()
	{
		return getCursorString(CALLSIGN);
	}

	public String getNotes()
	{
		return getCursorString(NOTES);
	}

	public boolean isLiveTracking()
	{
		return getCursorBoolean(IS_LIVE_TRACKING);
	}

	public boolean isPatrolTrained()
	{
		return getCursorBoolean(IS_PATROL_TRAINED);
	}
	
	public boolean isDrafted()
	{
		return getCursorBoolean(IS_DRAFTED);
	}
	
	public int getTransportPriority()
	{
		return getCursorInt(TRANSPORT_PRIORITY);
	}
	
	public boolean onDoubleShift()
	{
		return getCursorBoolean(ON_DOUBLE_SHIFT);
	}
	
	public String getTypeID()
	{
		return getCursorGuid(TYPE);
	}
	
	public AssetType getType(Context context)
	{
		if (_type == null)
		{
			_type = AssetType.query(context);
		}
		String typeID = getTypeID();
		if (typeID != null)
		{
			_type.moveToPosition(typeID);
			return _type;
		}
		else
		{
			return null;
		}
	}
	
	public String getStatusID()
	{
		return getCursorGuid(STATUS);
	}

	public AssetStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = AssetStatus.query(context);
		}
		String statusID = getStatusID();
		if (statusID != null)
		{
			_status.moveToPosition(statusID);
			return _status;
		}
		else
		{
			return null;
		}
	}

	public String getCheckInLocationID()
	{
		return getCursorGuid(CHECK_IN_LOCATION);
	}
	
	public Address getCheckInLocation(Context context)
	{
		String id = getCheckInLocationID();
		if (_checkInLocation == null && id != null)
		{
			_checkInLocation = Address.query(context, Uri.withAppendedPath(Address.CONTENT_URI, id));
		}
		return _checkInLocation;
	}
	
	public String getEquipmentTypeID()
	{
		return getCursorGuid(EQUIPMENT_TYPE);
	}
	
	public EquipmentType getEquipmentType(Context context)
	{
		String id = getEquipmentTypeID();
		if (_equipmentType == null && id != null)
		{
			_equipmentType = EquipmentType.query(context, Uri.withAppendedPath(EquipmentType.CONTENT_URI, id));
		}
		return _equipmentType;
	}
	
	public String getEquipmentID()
	{
		return getCursorGuid(EQUIPMENT);
	}
	
	public Equipment getEquipment(Context context)
	{
		String id = getEquipmentID();
		if (_equipment == null && id != null)
		{
			_equipment = Equipment.query(context, Uri.withAppendedPath(Equipment.CONTENT_URI, id));
		}
		return _equipment;
	}

	public String getUserTypeID()
	{
		return getCursorGuid(USER_TYPE);
	}
	
	public UserType getUserType(Context context)
	{
		String id = getUserTypeID();
		if (_userType == null && id != null)
		{
			_userType = UserType.query(context, Uri.withAppendedPath(UserType.CONTENT_URI, id));
		}
		return _userType;
	}

	public String getUserID()
	{
		return getCursorGuid(USER);
	}
	
	public User getUser(Context context)
	{
		String id = getUserID();
		if (_user == null && id != null)
		{
			_user = User.query(context, Uri.withAppendedPath(User.CONTENT_URI, id));
		}
		return _user;
	}
	
	public String getControllerNo()
	{
		return getCursorString(CONTROLLER_NO);
	}
	
	public String getOverTimeCode()
	{
		return getCursorString(OVERTIME_CODE);
	}
	
	public DateTime getCheckInTime()
	{
		return getCursorDateTime(CHECKIN_TIME);
	}
	
	public DateTime getCheckOutTime()
	{
		return getCursorDateTime(CHECKOUT_TIME);
	}
	
	public DateTime getCheckInByTime()
	{
		return getCursorDateTime(CHECKIN_BY_TIME);
	}
	
	public DateTime getShiftStart()
	{
		return getCursorDateTime(SHIFT_START);
	}

	public DateTime getShiftEnd()
	{
		return getCursorDateTime(SHIFT_END);
	}
	
	public DateTime getOverTimeStart()
	{
		return getCursorDateTime(OVER_TIME_START);
	}
	
	public boolean overTimeSlipReturnedEnd()
	{
		return getCursorBoolean(OVER_TIME_SLIP_RETURNED);
	}
	
	public double getDriveTimeHours()
	{
		return getCursorDouble(DRIVE_TIME_HOURS);
	}
	
	public int getStartDistance()
	{
		return getCursorInt(START_DISTANCE);
	}
	
	public int getEndDistance()
	{
		return getCursorInt(END_DISTANCE);
	}
	
	public String getAuditUser()
	{
		return getCursorString(AUDIT_USER);
	}
	
	public DateTime getAuditDate()
	{
		return getCursorDateTime(AUDIT_DATE);
	}
	
	public String getReason()
	{
		return getCursorString(REASON);
	}
	
	public String getExpectedRankID()
	{
		return getCursorGuid(EXPECTED_RANK);
	}

	public UserRankType getExpectedRank(Context context)
	{
		String id = getExpectedRankID();
		if (_expectedRank == null && id != null)
		{
			_expectedRank = UserRankType.query(context, Uri.withAppendedPath(UserRankType.CONTENT_URI, id));
		}
		return _expectedRank;
	}

	public int getHoursUsed()
	{
		return getCursorInt(HOURS_USED);
	}
	
	public boolean isCheckedIn()
	{
		return getCursorBoolean(IS_CHECKED_IN);
	}

	public boolean isCheckedInByTime()
	{
		return getCursorBoolean(IS_CHECKED_IN_BY_TIME);
	}
	
	public static Asset query(Context context, Uri uri)
	{
		Asset result = null;
		try
		{
			result = new Asset(context, context.getContentResolver().query(uri, null, null, null, null));
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
